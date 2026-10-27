package com.example.rent_it.Auth;

import com.example.rent_it.Entity.Role;
import com.example.rent_it.Entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CurrentUserService {

    public User requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (!(principal instanceof User user)
                || !Boolean.TRUE.equals(user.getActive())
                || !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AccessDeniedException("Please sign in with a verified account");
        }

        return user;
    }

    public void requireSelfOrAdmin(Long userId) {
        User currentUser = requireUser();
        if (currentUser.getRole() != Role.ADMIN && !Objects.equals(currentUser.getId(), userId)) {
            throw new AccessDeniedException("You can only act on behalf of your own account");
        }
    }

    public void requireResourceOwner(User resourceOwner) {
        User currentUser = requireUser();
        if (currentUser.getRole() != Role.ADMIN
                && (resourceOwner == null || !Objects.equals(currentUser.getId(), resourceOwner.getId()))) {
            throw new AccessDeniedException("You do not own this resource");
        }
    }

    public void requireAdmin() {
        if (requireUser().getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Administrator access is required");
        }
    }
}
