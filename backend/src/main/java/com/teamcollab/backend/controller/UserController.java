package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.UpdateProfileRequest;
import com.teamcollab.backend.dto.UpdateRoleRequest;
import com.teamcollab.backend.dto.UserResponse;
import com.teamcollab.backend.security.UserPrincipal;
import com.teamcollab.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(principal.getUser());
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(userService.updateProfile(principal.getUser(), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadAvatar(@RequestParam("file") MultipartFile file,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(userService.updateAvatar(principal.getUser(), file));
    }

    @DeleteMapping("/me/avatar")
    public UserResponse removeAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(userService.removeAvatar(principal.getUser()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return userService.listAll().stream().map(UserResponse::from).toList();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(userService.updateRole(id, request.getRole(), principal.getUser()));
    }
}
