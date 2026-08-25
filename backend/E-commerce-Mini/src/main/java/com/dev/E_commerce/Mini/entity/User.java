package com.dev.E_commerce.Mini.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true)
    String username;
    @Column(nullable = false, unique = true)
    String email;
    @Column(nullable = false)
    String password;

    String fullName;
    LocalDate dob;
    String phone;
    String address;

    @ManyToMany
            @JoinTable(
                    name = "user_roles",
                    joinColumns = @JoinColumn(name = "user_id"),
                    inverseJoinColumns = @JoinColumn(name = "role_name")
            )
    Set<Role> roles;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    Cart cart;

    @OneToMany(mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Order> orders = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    LocalDateTime createdAt;
}
