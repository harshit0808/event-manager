package com.example.event_manager.domain;

import jakarta.persistence.*;
import com.example.event_manager.domain.Registration;
import com.example.event_manager.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.EqualsAndHashCode; 
import lombok.ToString; 

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.ArrayList; 
import java.util.HashSet; 

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "users") 
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password; 

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude 
    private List<Event> organizedEvents = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude 
    private Set<Registration> registrations = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}