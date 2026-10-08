package com.teamcollab.backend.service;

import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.InvalidRoleChangeException;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    /** Admin-only role change. Self-changes are blocked so an admin can never accidentally lock themselves out. */
    public User updateRole(Long id, Role newRole, User requester) {
        if (id.equals(requester.getId())) {
            throw new InvalidRoleChangeException("You cannot change your own role");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
