package com.hotel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.model.Role;
import com.hotel.model.User;
import com.hotel.service.UserService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin("*")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns all customers and hotel owners.
     * Administrator accounts are excluded.
     */
    @GetMapping
    public Resp<?> getUsers() {

        List<User> users = userService.getAllUsers()
                .stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .peek(user -> user.setPassword(null))
                .toList();

        return Resp.success(users);
    }

    /**
     * Allows an administrator to delete a customer or hotel owner.
     */
    @DeleteMapping("/{userId}")
    public Resp<?> deleteUser(
            @PathVariable Integer userId) {

        User user = userService.findById(userId)
                .orElse(null);

        if (user == null) {
            return Resp.error("User not found");
        }

        if (user.getRole() == Role.ADMIN) {
            return Resp.error(
                    "Administrator accounts cannot be deleted"
            );
        }

        userService.deleteUser(userId);

        return Resp.success(
                "Account deleted successfully"
        );
    }
}