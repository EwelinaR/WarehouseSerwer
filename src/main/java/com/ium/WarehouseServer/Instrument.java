package com.ium.WarehouseServer;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
class Instrument {
    @Id
    @GeneratedValue
    private long id;
    private String manufacturer;
    private String model;
    private float price;
    private int quantity;

    void increaseQuantity(int amount) {
        quantity += amount;
    }

    void decreaseQuantity(int amount) {
        if (quantity > amount) {
            quantity -= amount;
        } else {
            quantity = 0;
        }
    }
}
