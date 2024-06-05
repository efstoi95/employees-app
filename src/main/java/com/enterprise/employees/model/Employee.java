package com.enterprise.employees.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Size(max = 20, message = "Full name exceeds the maximum length of 20 characters")
    @NotBlank(message = "Full name is mandatory")
    private String fullName;

    @Size(max = 100, message = "Password exceeds the maximum length of 50 characters")
    @NotBlank(message = "Password is mandatory")
    private String password;

    @Size(max = 50, message = "Email exceeds the maximum length of 50 characters")
    @Email @NotBlank(message = "Email is mandatory")
    private String email;

    @Size(max = 20, message = "Username exceeds the maximum length of 20 characters")
    @NotBlank(message = "Username is mandatory")
    private String username;

    @Enumerated(EnumType.STRING)
    private EmployeeRoles role;
    private boolean verified = false;

    private String verifiedCode;

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
