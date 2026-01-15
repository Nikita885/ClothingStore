package com.mobl.clothingmarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobl.clothingmarket.adapter.CartAdapter;
import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.CartItem;
import com.mobl.clothingmarket.model.Product;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView totalTextView;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;
    private List<CartItem> cartItems;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        database = AppDatabase.getInstance(this);
        prefsHelper = new SharedPreferencesHelper(this);

        recyclerView = findViewById(R.id.cart_recycler_view);
        totalTextView = findViewById(R.id.cart_total);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        cartItems = new ArrayList<>();
        products = new ArrayList<>();
        
        adapter = new CartAdapter(cartItems, products, new CartAdapter.OnItemClickListener() {
            @Override
            public void onRemoveClick(CartItem cartItem) {
                database.cartDao().deleteCartItem(cartItem);
                loadCartItems();
            }

            @Override
            public void onQuantityChange(CartItem cartItem, int newQuantity) {
                cartItem.setQuantity(newQuantity);
                database.cartDao().updateCartItem(cartItem);
                loadCartItems();
            }

            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(CartActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        loadCartItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }

    private void loadCartItems() {
        int userId = prefsHelper.getUserId();
        if (userId != -1) {
            cartItems = database.cartDao().getCartItemsByUserId(userId);
            products = new ArrayList<>();
            
            for (CartItem item : cartItems) {
                Product product = database.productDao().getProductById(item.getProductId());
                if (product != null) {
                    products.add(product);
                }
            }
            
            adapter.updateCartItems(cartItems, products);
            calculateTotal();
        }
    }

    private void calculateTotal() {
        double total = 0;
        for (int i = 0; i < cartItems.size(); i++) {
            if (i < products.size()) {
                total += products.get(i).getPrice() * cartItems.get(i).getQuantity();
            }
        }
        totalTextView.setText(String.format("Итого: %.2f ₽", total));
    }
}

