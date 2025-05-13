package com.maut.core.modules.auth.service;

import com.maut.core.modules.role.model.AdminRole;
import com.maut.core.modules.team.model.TeamMembership;
import com.maut.core.modules.team.repository.TeamMembershipRepository;
import com.maut.core.modules.user.model.User;
import com.maut.core.modules.user.repository.UserRepository;
import com.maut.core.modules.user.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeamMembershipRepository teamMembershipRepository;

    @Override
    @Transactional(readOnly = true) // Important for lazy loading if team memberships or roles are fetched lazily elsewhere
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is inactive: " + email);
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getUserType() == UserType.ADMIN) {
            authorities.addAll(user.getAdminRoles().stream()
                    .map(AdminRole::getName)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        } else if (user.getUserType() == UserType.CLIENT) {
            List<TeamMembership> teamMemberships = teamMembershipRepository.findByUser(user);
            authorities.addAll(teamMemberships.stream()
                    .map(teamMembership -> teamMembership.getTeamRole().getName())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }
        // Add a default role if no specific roles are found, or handle as an error/specific logic
        if (authorities.isEmpty()) {
            // This could be a default role like "ROLE_USER" or an indication of a configuration issue
            // For now, let's ensure every authenticated user has at least one role for security configurations.
            // Depending on application requirements, this might need adjustment.
            // For example, if ADMIN users might not have explicit AdminRole entries but are still admins,
            // or CLIENT users might not be part of any team yet.
            // Consider adding a log warning here if authorities are empty and it's unexpected.
             authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Default fallback role
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities);
    }
}
