package com.mobl.clothingmarket.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.mobl.clothingmarket.model.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    long insertProduct(Product product);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :id")
    Product getProductById(int id);

    @Query("SELECT * FROM products WHERE name LIKE :searchQuery OR description LIKE :searchQuery OR category LIKE :searchQuery")
    List<Product> searchProducts(String searchQuery);

    @Query("SELECT * FROM products WHERE " +
           "(:category IS NULL OR category = :category) AND " +
           "(:minPrice IS NULL OR price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR price <= :maxPrice) AND " +
           "(:inStock IS NULL OR CASE WHEN :inStock = 1 THEN stockQuantity > 0 ELSE stockQuantity = 0 END) AND " +
           "(:size IS NULL OR size = :size)")
    List<Product> getFilteredProducts(String category, Double minPrice, Double maxPrice, Integer inStock, String size);

    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);
}

