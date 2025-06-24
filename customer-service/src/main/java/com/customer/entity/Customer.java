package com.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    private String customerName;
    private String phoneNumber;

    @Transient
    private boolean sameAddress; // Used only for request payload, not stored in DB

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> billingAddress = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> shippingAddress = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return sameAddress == customer.sameAddress
                && Objects.equals(customerId, customer.customerId)
                && Objects.equals(customerName, customer.customerName)
                && Objects.equals(phoneNumber, customer.phoneNumber)
                && Objects.equals(billingAddress, customer.billingAddress)
                && Objects.equals(shippingAddress, customer.shippingAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, customerName, phoneNumber, sameAddress, billingAddress, shippingAddress);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", sameAddress=" + sameAddress +
                '}';
    }


}
