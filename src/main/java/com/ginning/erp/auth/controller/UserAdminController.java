package com.ginning.erp.auth.controller;

import com.ginning.erp.auth.dto.UpdateUserRolesRequest;
import com.ginning.erp.auth.dto.UserDto;
import com.ginning.erp.auth.service.UserService;
import com.ginning.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserDto>> list() {
        return ApiResponse.success(userService.listUsers());
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<UserDto> updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ApiResponse.success(
                userService.updateRoles(id, request.getRoles()),
                "User roles updated successfully"
        );
    }
}
