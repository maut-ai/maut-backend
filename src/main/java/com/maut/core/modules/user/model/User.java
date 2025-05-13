package com.maut.core.modules.user.model;

import com.maut.core.modules.user.enums.UserType;
import com.maut.core.modules.role.model.AdminRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents a user account for accessing Maut's dashboards (e.g., admin dashboard, client portal).
 * These users typically authenticate via email and password and are managed by Spring Security.
 * This is distinct from MautUser, which represents end-users of Maut's client applications.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true; // Default to true

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- Relationships ---

    @ManyToMany(fetch = FetchType.EAGER) // Eager fetch roles for security checks
    @JoinTable(
        name = "user_admin_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "admin_role_id")
    )
    @Builder.Default
    private Set<AdminRole> adminRoles = new HashSet<>();

    @Transient // Mark as transient so JPA ignores it
    private Collection<? extends GrantedAuthority> resolvedAuthorities;

    // --- UserDetails Implementation ---

    // Setter for UserDetailsServiceImpl to provide the resolved authorities
    public void setResolvedAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.resolvedAuthorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This method will now return the authorities resolved and set by UserDetailsServiceImpl.
        // If resolvedAuthorities is null or empty (e.g., if User object is obtained not via UserDetailsServiceImpl
        // or if no authorities were applicable), provide a minimal default.
        if (this.resolvedAuthorities == null || this.resolvedAuthorities.isEmpty()) {
            // Consider logging a warning if this state is unexpected for an authenticated user.
            // e.g., log.warn("User {} has no resolved authorities, falling back to default ROLE_USER_MINIMAL", this.email);
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER_FALLBACK"));
        }
        return this.resolvedAuthorities;
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // Matches UserDetailsServiceImpl logic
    }

    @Override
    public String getUsername() {
        return this.email; // Matches UserDetailsServiceImpl logic
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Default implementation
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Default implementation
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Default implementation
    }

    @Override
    public boolean isEnabled() {
        return this.isActive; // Uses existing isActive field
    }
}
