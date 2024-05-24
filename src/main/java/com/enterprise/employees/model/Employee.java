package com.enterprise.employees.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(exclude = "department")
@Entity
public class Employee implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is mandatory")
    private String fullName;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @Email @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "Username is mandatory")
    private String username;

    @Enumerated(EnumType.STRING)
    private EmployeeRoles role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;


    /**
     * Returns a collection of granted authorities for the current user.
     *
     * @return a collection of GrantedAuthority objects representing the user's roles
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    /**
     * Checks if the account is not expired.
     *
     * @return true if the account is not expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    /**
     * Checks if the account is not locked.
     *
     * @return true if the account is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    /**
     * Checks if the credentials are not expired.
     *
     * @return true if the credentials are not expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    /**
     * Returns whether the object is enabled or not.
     *
     * @return  true if the object is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
