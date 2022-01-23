package com.example.audiozicapp.Model;

public class Product {
    public String id;
    public String name;
    public int price;
    public String photo;
    public String details;

    public Product() {
    }

    public Product(String id, String name, int price, String photo, String details) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.photo = photo;
        this.details = details;
    }
}
