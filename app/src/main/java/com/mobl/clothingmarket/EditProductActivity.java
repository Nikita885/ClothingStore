package com.mobl.clothingmarket;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.Product;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

public class EditProductActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private MaterialAutoCompleteTextView categoryAutoComplete;
    private MaterialAutoCompleteTextView sizeAutoComplete;
    private EditText colorEditText;
    private EditText stockEditText;
    private Button saveButton;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        database = AppDatabase.getInstance(this);
        prefsHelper = new SharedPreferencesHelper(this);

        if (!prefsHelper.isAdmin()) {
            Toast.makeText(this, R.string.admin_only, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Ошибка: товар не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        product = database.productDao().getProductById(productId);
        if (product == null) {
            Toast.makeText(this, "Товар не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.widget.TextView titleTextView = findViewById(R.id.text_title);
        if (titleTextView != null) {
            titleTextView.setText(R.string.edit_product);
        }

        nameEditText = findViewById(R.id.edit_product_name);
        descriptionEditText = findViewById(R.id.edit_product_description);
        priceEditText = findViewById(R.id.edit_product_price);
        categoryAutoComplete = findViewById(R.id.edit_product_category);
        sizeAutoComplete = findViewById(R.id.edit_product_size);
        colorEditText = findViewById(R.id.edit_product_color);
        stockEditText = findViewById(R.id.edit_product_stock);
        saveButton = findViewById(R.id.btn_save_product);

        String[] categories = getResources().getStringArray(R.array.product_categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(categoryAdapter);

        String[] sizes = getResources().getStringArray(R.array.product_sizes);
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, sizes);
        sizeAutoComplete.setAdapter(sizeAdapter);

        loadProductData();

        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void loadProductData() {
        nameEditText.setText(product.getName());
        descriptionEditText.setText(product.getDescription());
        priceEditText.setText(String.valueOf(product.getPrice()));
        categoryAutoComplete.setText(product.getCategory(), false);
        sizeAutoComplete.setText(product.getSize(), false);
        colorEditText.setText(product.getColor());
        stockEditText.setText(String.valueOf(product.getStockQuantity()));
    }

    private void saveProduct() {
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String category = categoryAutoComplete.getText().toString().trim();
        String size = sizeAutoComplete.getText().toString().trim();
        String color = colorEditText.getText().toString().trim();
        String stockStr = stockEditText.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || 
            category.isEmpty() || size.isEmpty() || color.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                Toast.makeText(this, "Цена должна быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            int stockQuantity = Integer.parseInt(stockStr);
            if (stockQuantity < 0) {
                Toast.makeText(this, "Количество не может быть отрицательным", Toast.LENGTH_SHORT).show();
                return;
            }

            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            product.setSize(size);
            product.setColor(color);
            product.setStockQuantity(stockQuantity);

            database.productDao().updateProduct(product);
            Toast.makeText(this, "Товар успешно обновлен!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Неверный формат цены или количества", Toast.LENGTH_SHORT).show();
        }
    }
}

