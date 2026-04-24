package com.wonwire.wonwire.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Optimistic locking: prevents balance corruption when two transfers arrive simultaneously.
     * JPA increments this field on every update and throws an exception if two transactions modify the same row at the same time.
     */
    @Version
    private Long version;
}