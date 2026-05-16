package com.wonwire.wonwire.domain;

import com.wonwire.wonwire.domain.enums.Currency;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    /**
     * The currency of this wallet.
     * A wallet operates in a single currency only.
     * Multi-currency support requires multiple wallets per user.
     */
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Optimistic locking: prevents balance corruption when two transfers arrive simultaneously.
     * JPA increments this field on every update and throws an exception if two transactions modify the same row at the same time.
     * We assume that there is no conflict, we check at the end of the transaction.
     */
    @Version
    private Long version;
}