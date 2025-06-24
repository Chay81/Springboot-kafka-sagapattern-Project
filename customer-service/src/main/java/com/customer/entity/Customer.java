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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    private String customerName;
    private String phoneNumber;

    private boolean sameAddress; // Used to copy billing to shipping if true

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    // Convenience method
    public void addAddress(Address address) {
        address.setCustomer(this);
        addresses.add(address);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", sameAddress=" + sameAddress +
                ", addresses=" + addresses +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return sameAddress == customer.sameAddress && Objects.equals(customerId, customer.customerId) && Objects.equals(customerName, customer.customerName) && Objects.equals(phoneNumber, customer.phoneNumber) && Objects.equals(addresses, customer.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, customerName, phoneNumber, sameAddress, addresses);
    }
}
