package com.ium.WarehouseServer;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

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
    private long manufacturerTimestamp;
    private String model;
    private long modelTimestamp;
    private float price;
    private long priceTimestamp;
    private int quantity;
    private int category;
    private long categoryTimestamp;

    public Instrument(int id, String manufacturer, String model, float price, int quantity, int category) {
        this(id, manufacturer, model, price, quantity);
        this.category = category;
    }

    public Instrument(int id, String manufacturer, String model, float price, int quantity) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.price = price;
        this.quantity = quantity;
        this.manufacturerTimestamp = new Date().getTime();
        this.modelTimestamp = new Date().getTime();
        this.priceTimestamp = new Date().getTime();
    }

    void increaseQuantity(int amount) {
        quantity += amount;
    }

    boolean decreaseQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
}
