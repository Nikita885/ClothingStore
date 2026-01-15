package com.mobl.clothingmarket.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobl.clothingmarket.ProductDetailActivity;
import com.mobl.clothingmarket.R;
import com.mobl.clothingmarket.model.CartItem;
import com.mobl.clothingmarket.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private Map<Integer, Product> productsMap;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onRemoveClick(CartItem cartItem);
        void onQuantityChange(CartItem cartItem, int newQuantity);
        void onItemClick(Product product);
    }

    public CartAdapter(List<CartItem> cartItems, List<Product> products, OnItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.productsMap = new HashMap<>();
        for (Product product : products) {
            productsMap.put(product.getId(), product);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = productsMap.get(cartItem.getProductId());
        if (product != null) {
            holder.bind(cartItem, product);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newCartItems, List<Product> products) {
        this.cartItems = newCartItems;
        this.productsMap.clear();
        for (Product product : products) {
            productsMap.put(product.getId(), product);
        }
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView priceTextView;
        private TextView quantityTextView;
        private TextView totalTextView;
        private Button decreaseButton;
        private Button increaseButton;
        private View cardView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView;
            nameTextView = itemView.findViewById(R.id.cart_product_name);
            priceTextView = itemView.findViewById(R.id.cart_product_price);
            quantityTextView = itemView.findViewById(R.id.cart_quantity);
            totalTextView = itemView.findViewById(R.id.cart_item_total);
            decreaseButton = itemView.findViewById(R.id.btn_decrease);
            increaseButton = itemView.findViewById(R.id.btn_increase);
        }

        public void bind(CartItem cartItem, Product product) {
            nameTextView.setText(product.getName());
            priceTextView.setText(String.format("%.2f ₽", product.getPrice()));
            quantityTextView.setText(String.valueOf(cartItem.getQuantity()));
            double total = product.getPrice() * cartItem.getQuantity();
            totalTextView.setText(String.format("%.2f ₽", total));

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(product);
                }
            });

            decreaseButton.setOnClickListener(v -> {
                if (cartItem.getQuantity() > 1) {
                    int newQuantity = cartItem.getQuantity() - 1;
                    if (listener != null) {
                        listener.onQuantityChange(cartItem, newQuantity);
                    }
                } else {
                    if (listener != null) {
                        listener.onRemoveClick(cartItem);
                    }
                }
            });

            increaseButton.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChange(cartItem, newQuantity);
                }
            });
        }
    }
}

