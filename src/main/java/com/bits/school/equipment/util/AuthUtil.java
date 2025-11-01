package com.bits.school.equipment.util;

import com.bits.school.equipment.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AuthUtil {
    private AuthUtil() {}

    public static void requireRole(User user, User.Role... allowedRoles) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        for (User.Role role : allowedRoles) {
            if (user.getRole() == role) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Required role not found.");
    }

    public static void requireAdmin(User user) {
        requireRole(user, User.Role.ADMIN);
    }

    public static void requireStaffOrAdmin(User user) {
        requireRole(user, User.Role.STAFF, User.Role.ADMIN);
    }

    public static boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    public static boolean isStaff(User user) {
        return user != null && user.getRole() == User.Role.STAFF;
    }

    public static boolean isStudent(User user) {
        return user != null && user.getRole() == User.Role.STUDENT;
    }
}

