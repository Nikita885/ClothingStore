package com.mobl.clothingmarket.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobl.clothingmarket.ProductDetailActivity;
import com.mobl.clothingmarket.R;
import com.mobl.clothingmarket.adapter.CartAdapter;
import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.CartItem;
import com.mobl.clothingmarket.model.Product;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView totalTextView;
    private TextView emptyCartTextView;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;
    private List<CartItem> cartItems;
    private List<Product> products;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        database = AppDatabase.getInstance(requireContext());
        prefsHelper = new SharedPreferencesHelper(requireContext());

        recyclerView = view.findViewById(R.id.cart_recycler_view);
        totalTextView = view.findViewById(R.id.cart_total);
        emptyCartTextView = view.findViewById(R.id.empty_cart_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
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
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        
        if (!prefsHelper.isLoggedIn()) {
            emptyCartTextView.setText("Войдите, чтобы увидеть корзину");
            emptyCartTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            totalTextView.setVisibility(View.GONE);
        } else {
            loadCartItems();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefsHelper.isLoggedIn()) {
            loadCartItems();
        }
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
            
            if (cartItems.isEmpty()) {
                emptyCartTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                totalTextView.setVisibility(View.GONE);
            } else {
                emptyCartTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                totalTextView.setVisibility(View.VISIBLE);
                adapter.updateCartItems(cartItems, products);
                calculateTotal();
            }
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

