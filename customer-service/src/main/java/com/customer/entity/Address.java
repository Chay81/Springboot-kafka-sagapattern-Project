package com.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    private String apartmentName;
    private String addLine1;
    private String addLine2;
    private String city;
    private String state;
    private String zipCode;

    private String addressType; // "BILLING" or "SHIPPING"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    @Override
    public String toString() {
        return "Address{" +
                "addressId=" + addressId +
                ", apartmentName='" + apartmentName + '\'' +
                ", addLine1='" + addLine1 + '\'' +
                ", addLine2='" + addLine2 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", addressType='" + addressType + '\'' +
                ", customer=" + customer +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(addressId, address.addressId) && Objects.equals(apartmentName, address.apartmentName) && Objects.equals(addLine1, address.addLine1) && Objects.equals(addLine2, address.addLine2) && Objects.equals(city, address.city) && Objects.equals(state, address.state) && Objects.equals(zipCode, address.zipCode) && Objects.equals(addressType, address.addressType) && Objects.equals(customer, address.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressId, apartmentName, addLine1, addLine2, city, state, zipCode, addressType, customer);
    }
}
