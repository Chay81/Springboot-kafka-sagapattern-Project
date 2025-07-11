package com.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Encrypted refresh token (sent to client)
    @Column(name = "token", nullable = false, length = 2048)
    @Lob
    private String token;

    // Raw UUID (used for lookup)
    @Column(name = "raw_token", nullable = false, unique = true, length = 100)
    private String rawToken; // plain UUID version used for lookup

    @Column(nullable = false)
    private String username; // email address

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "refresh_token_roles", joinColumns = @JoinColumn(name = "token_id"))
    @Column(name = "role")
    private Set<String> roles;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean expired = false;

    public RefreshToken(Long id, String token, String username, Set<String> roles,
                        Instant issuedAt, Instant expiresAt, boolean expired) {
        this.id = id;
        this.token = token;
        this.username = username;
        this.roles = roles != null ? roles : new HashSet<>();
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.expired = expired;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        RefreshToken that = (RefreshToken) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

