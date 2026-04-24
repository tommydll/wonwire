package com.wonwire.wonwire.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Wallet wallet;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist // automatically called by JPA before insert
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}