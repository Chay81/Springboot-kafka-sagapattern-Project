package com.customer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$", // Assumes Indian phone format, update as needed
            message = "Phone number must be valid"
    )
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String emailAddress;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Column(nullable = false, unique = true)
    private String password;

    @Transient
    private boolean sameAddress; // Used only for request payload, not stored in DB

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> billingAddress = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> shippingAddress = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "customer_roles", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return sameAddress == customer.sameAddress
                && Objects.equals(customerId, customer.customerId)
                && Objects.equals(customerName, customer.customerName)
                && Objects.equals(phoneNumber, customer.phoneNumber)
                && Objects.equals(roles, customer.roles)
                && Objects.equals(billingAddress, customer.billingAddress)
                && Objects.equals(shippingAddress, customer.shippingAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, customerName, phoneNumber, roles, sameAddress, billingAddress, shippingAddress);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", roles='" + roles + '\'' +
                ", sameAddress=" + sameAddress +
                '}';
    }


}