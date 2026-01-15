package com.mobl.clothingmarket.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.mobl.clothingmarket.model.CartItem;

import java.util.List;

@Dao
public interface CartDao {
    @Insert
    long insertCartItem(CartItem cartItem);

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    List<CartItem> getCartItemsByUserId(int userId);

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId")
    CartItem getCartItem(int userId, int productId);

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    void clearCart(int userId);

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    void deleteCartItemsByProductId(int productId);

    @Delete
    void deleteCartItem(CartItem cartItem);

    @Update
    void updateCartItem(CartItem cartItem);
}

