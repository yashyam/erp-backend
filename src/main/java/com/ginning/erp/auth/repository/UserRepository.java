package com.ginning.erp.auth.repository;

import com.ginning.erp.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    Optional<User> findByAuthProviderAndProviderSubjectAndDeletedAtIsNull(String authProvider, String providerSubject);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}
