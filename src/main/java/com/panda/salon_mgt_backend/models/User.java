package com.panda.salon_mgt_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    @NotBlank
    @Size(max=20)
    @Column(name = "username")
    private String userName;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(name = "email")
    private String email;

    @NotBlank
    @Size(max = 120)
    @Column(name = "password")
    private String password;

    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    private boolean enabled;

    public User(String userName, String email, String password, boolean enabled) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToOne(mappedBy = "owner")
    @ToString.Exclude
    private Salon salon;

    // in User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_salon_id")
    private Salon staffSalon;

    @ManyToMany
    @JoinTable(
            name = "staff_services",
            joinColumns = @JoinColumn(name = "staff_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"),
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"staff_id", "service_id"})
            }
    )
    private Set<Services> services = new HashSet<>();

    public boolean hasRole(String role) {
        return roles.stream()
                .anyMatch(r -> r.getRoleName().name().equals(role));
    }

}
