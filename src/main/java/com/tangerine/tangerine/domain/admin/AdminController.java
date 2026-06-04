package com.tangerine.tangerine.domain.admin;

import com.tangerine.tangerine.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(adminService.getUsers(pageable));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> changeUserRole(
            @PathVariable Long id,
            @RequestBody String role) {

        adminService.changeUserRole(id, role.trim().toUpperCase());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {

        adminService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {

        adminService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
