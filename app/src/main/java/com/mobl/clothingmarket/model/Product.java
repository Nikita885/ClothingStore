package com.mobl.clothingmarket.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String size;
    private String color;
    private String imageUrl;
    private int stockQuantity;

    public Product() {
        this.stockQuantity = 0;
    }

    @Ignore
    public Product(String name, String description, double price, String category, String size, String color, int stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public List<String> getImageUrls() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(imageUrl.split(",")));
    }

    public void setImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            this.imageUrl = null;
        } else {
            this.imageUrl = String.join(",", imageUrls);
        }
    }

    public void addImageUrl(String imageUrl) {
        List<String> urls = getImageUrls();
        if (!urls.contains(imageUrl)) {
            urls.add(imageUrl);
            setImageUrls(urls);
        }
    }
}

