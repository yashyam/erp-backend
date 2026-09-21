package com.ginning.erp.auth.service;

import com.ginning.erp.audit.entity.AuditLog;
import com.ginning.erp.audit.repository.AuditLogRepository;
import com.ginning.erp.auth.dto.JwtResponse;
import com.ginning.erp.auth.dto.LoginRequest;
import com.ginning.erp.auth.dto.RegisterRequest;
import com.ginning.erp.auth.dto.UserDto;
import com.ginning.erp.auth.entity.User;
import com.ginning.erp.auth.repository.UserRepository;
import com.ginning.erp.common.exception.ApplicationException;
import com.ginning.erp.role.entity.Role;
import com.ginning.erp.role.repository.RoleRepository;
import com.ginning.erp.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static final String DEFAULT_ROLE_NAME = "ROLE_WORKER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtProvider jwtProvider;
    private final RestClient restClient = RestClient.create();
    private final String googleClientId;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationConfiguration authenticationConfiguration,
            JwtProvider jwtProvider,
            @Value("${app.oauth.google.client-id:}") String googleClientId
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtProvider = jwtProvider;
        this.googleClientId = googleClientId;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        User user = findActiveUser(emailOrUsername).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + emailOrUsername)
        );

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .accountLocked(!Boolean.TRUE.equals(user.getAccountNonLocked()))
                .accountExpired(!Boolean.TRUE.equals(user.getAccountNonExpired()))
                .credentialsExpired(!Boolean.TRUE.equals(user.getCredentialsNonExpired()))
                .authorities(buildAuthorities(user))
                .build();
    }

    @Transactional
    public UserDto register(RegisterRequest request, HttpServletRequest servletRequest) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedUsername = normalizeText(request.getUsername());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ApplicationException("EMAIL_ALREADY_EXISTS", "Email is already registered", 409);
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ApplicationException("USERNAME_ALREADY_EXISTS", "Username is already taken", 409);
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseGet(() -> roleRepository.save(new Role(
                        DEFAULT_ROLE_NAME,
                        "Default application role for ERP users"
                )));

        User user = new User(normalizedEmail, normalizedUsername, passwordEncoder.encode(request.getPassword()));
        user.setFirstName(normalizeNullableText(request.getFirstName()));
        user.setLastName(normalizeNullableText(request.getLastName()));
        user.setPhone(normalizeNullableText(request.getPhone()));
        user.setStatus("ACTIVE");
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.addRole(defaultRole);

        User savedUser = userRepository.save(user);
        saveAuditLog(savedUser, "REGISTER", "USER", savedUser.getId().toString(), servletRequest);

        return UserDto.from(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getDeletedAt() == null)
                .map(UserDto::from)
                .toList();
    }

    @Transactional
    public UserDto updateRoles(UUID userId, Set<String> requestedRoles) {
        User user = userRepository.findById(userId)
                .filter(candidate -> candidate.getDeletedAt() == null)
                .orElseThrow(() -> new ApplicationException("USER_NOT_FOUND", "User not found", 404));

        Set<String> normalizedNames = requestedRoles == null ? Set.of() : requestedRoles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.trim().toUpperCase(Locale.ROOT))
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedNames.isEmpty()) {
            throw new ApplicationException("ROLE_REQUIRED", "At least one role is required", 400);
        }

        Set<Role> roles = normalizedNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ApplicationException(
                                "ROLE_NOT_FOUND", "Unknown role: " + roleName, 400)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public JwtResponse authenticate(LoginRequest request, HttpServletRequest servletRequest) {
        try {
            Authentication authentication = getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizeText(request.getEmailOrUsername()),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = findByCanonicalUsername(authentication.getName());
            saveAuditLog(user, "LOGIN", "AUTH", user.getId().toString(), servletRequest);

            return new JwtResponse(
                    jwtProvider.generateAccessToken(authentication),
                    jwtProvider.generateRefreshToken(authentication),
                    user.getUsername(),
                    user.getEmail()
            );
        } catch (BadCredentialsException ex) {
            throw new ApplicationException("INVALID_CREDENTIALS", "Invalid email/username or password", 401);
        } catch (DisabledException ex) {
            throw new ApplicationException("ACCOUNT_DISABLED", "User account is disabled", 403);
        } catch (LockedException ex) {
            throw new ApplicationException("ACCOUNT_LOCKED", "User account is locked", 423);
        } catch (AccountExpiredException ex) {
            throw new ApplicationException("ACCOUNT_EXPIRED", "User account has expired", 403);
        } catch (CredentialsExpiredException ex) {
            throw new ApplicationException("CREDENTIALS_EXPIRED", "User credentials have expired", 403);
        } catch (AuthenticationException ex) {
            throw new ApplicationException("AUTHENTICATION_FAILED", "Authentication failed", 401, ex);
        }
    }

    @Transactional
    public JwtResponse authenticateGoogle(String idToken, HttpServletRequest servletRequest) {
            if (googleClientId.isBlank()) {
                throw new ApplicationException("GOOGLE_OAUTH_NOT_CONFIGURED", "Google OAuth is not configured", 503);
            }
            Map<String, Object> claims;
            try {
                claims = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .scheme("https").host("oauth2.googleapis.com").path("/tokeninfo")
                                .queryParam("id_token", idToken).build())
                        .retrieve().body(Map.class);
            } catch (RuntimeException ex) {
                throw new ApplicationException("INVALID_GOOGLE_TOKEN", "Google token could not be verified", 401, ex);
            }
            if (claims == null || !googleClientId.equals(claims.get("aud"))
                    || !"true".equals(String.valueOf(claims.get("email_verified")))) {
                throw new ApplicationException("INVALID_GOOGLE_TOKEN", "Google token is not valid for this application", 401);
            }
            String subject = String.valueOf(claims.get("sub"));
            String email = normalizeEmail(String.valueOf(claims.get("email")));
            User user = userRepository.findByAuthProviderAndProviderSubjectAndDeletedAtIsNull("GOOGLE", subject)
                    .orElseGet(() -> userRepository.findByEmailAndDeletedAtIsNull(email).map(existing -> {
                        existing.setAuthProvider("GOOGLE");
                        existing.setProviderSubject(subject);
                        return existing;
                    }).orElseGet(() -> createGoogleUser(claims, email, subject)));
            validateUserState(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null, buildAuthorities(user));
            saveAuditLog(user, "LOGIN", "AUTH_GOOGLE", user.getId().toString(), servletRequest);
            return new JwtResponse(jwtProvider.generateAccessToken(authentication),
                    jwtProvider.generateRefreshToken(authentication), user.getUsername(), user.getEmail());
        }

    private User createGoogleUser(Map<String, Object> claims, String email, String subject) {
            String base = normalizeText(String.valueOf(claims.getOrDefault("name", email.substring(0, email.indexOf('@')))))
                    .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", ".");
            String username = base;
            int suffix = 1;
            while (userRepository.existsByUsername(username)) username = base + suffix++;
            Role worker = roleRepository.findByName(DEFAULT_ROLE_NAME)
                    .orElseThrow(() -> new ApplicationException("ROLE_NOT_FOUND", "Default worker role is missing", 500));
            User user = new User(email, username, passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setFirstName(normalizeNullableText(String.valueOf(claims.getOrDefault("given_name", ""))));
            user.setLastName(normalizeNullableText(String.valueOf(claims.getOrDefault("family_name", ""))));
            user.setAuthProvider("GOOGLE");
            user.setProviderSubject(subject);
            user.setStatus("ACTIVE");
            user.addRole(worker);
            return userRepository.save(user);
        }

    @Transactional
    public JwtResponse refreshToken(String refreshToken, HttpServletRequest servletRequest) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApplicationException("REFRESH_TOKEN_REQUIRED", "Refresh token is required", 400);
        }

        String token = stripBearerPrefix(refreshToken);
        if (!jwtProvider.validateToken(token)) {
            throw new ApplicationException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired", 401);
        }

        User user = findByCanonicalUsername(jwtProvider.getUsernameFromToken(token));
        validateUserState(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                buildAuthorities(user)
        );

        saveAuditLog(user, "TOKEN_REFRESH", "AUTH", user.getId().toString(), servletRequest);

        return new JwtResponse(
                jwtProvider.generateAccessToken(authentication),
                jwtProvider.generateRefreshToken(authentication),
                user.getUsername(),
                user.getEmail()
        );
    }

    @Transactional
    public void logout(String username, HttpServletRequest servletRequest) {
        if (username != null && !username.isBlank()) {
            findActiveUser(username).ifPresent(user ->
                    saveAuditLog(user, "LOGOUT", "AUTH", user.getId().toString(), servletRequest)
            );
        }

        SecurityContextHolder.clearContext();
    }

    private User findByCanonicalUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ApplicationException("USER_NOT_FOUND", "User not found", 404));
    }

    private java.util.Optional<User> findActiveUser(String emailOrUsername) {
        String normalizedInput = normalizeText(emailOrUsername);
        if (normalizedInput.contains("@")) {
            return userRepository.findByEmailAndDeletedAtIsNull(normalizedInput.toLowerCase(Locale.ROOT));
        }
        return userRepository.findByUsernameAndDeletedAtIsNull(normalizedInput);
    }

    private void validateUserState(User user) {
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ApplicationException("ACCOUNT_DISABLED", "User account is disabled", 403);
        }
        if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
            throw new ApplicationException("ACCOUNT_LOCKED", "User account is locked", 423);
        }
        if (!Boolean.TRUE.equals(user.getAccountNonExpired())) {
            throw new ApplicationException("ACCOUNT_EXPIRED", "User account has expired", 403);
        }
        if (!Boolean.TRUE.equals(user.getCredentialsNonExpired())) {
            throw new ApplicationException("CREDENTIALS_EXPIRED", "User credentials have expired", 403);
        }
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        Set<String> authorityNames = new LinkedHashSet<>();
        user.getRoles().forEach(role -> {
            authorityNames.add(role.getName());
            role.getPermissions().forEach(permission -> authorityNames.add(permission.getName()));
        });

        return authorityNames.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private void saveAuditLog(
            User user,
            String action,
            String entityType,
            String entityId,
            HttpServletRequest servletRequest
    ) {
        AuditLog auditLog = new AuditLog(user, action, entityType, entityId);
        if (servletRequest != null) {
            auditLog.setIpAddress(resolveClientIp(servletRequest));
            auditLog.setUserAgent(servletRequest.getHeader("User-Agent"));
        }
        auditLogRepository.save(auditLog);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private org.springframework.security.authentication.AuthenticationManager getAuthenticationManager() {
        try {
            return authenticationConfiguration.getAuthenticationManager();
        } catch (Exception ex) {
            throw new ApplicationException("AUTHENTICATION_CONFIGURATION_ERROR", "Authentication manager is unavailable", 500, ex);
        }
    }

    private String stripBearerPrefix(String token) {
        String trimmed = token.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7);
        }
        return trimmed;
    }

    private String normalizeEmail(String email) {
        return normalizeText(email).toLowerCase(Locale.ROOT);
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

}
