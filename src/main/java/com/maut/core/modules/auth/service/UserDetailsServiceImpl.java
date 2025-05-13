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

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Default fallback role
        }

        user.setResolvedAuthorities(authorities); // Set the resolved authorities on the User entity

        return user; // Return the User entity itself, which now implements UserDetails
    }
}
