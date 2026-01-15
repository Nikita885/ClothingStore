package com.mobl.clothingmarket.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobl.clothingmarket.R;
import com.mobl.clothingmarket.ProductDetailActivity;
import com.mobl.clothingmarket.adapter.FilterChipAdapter;
import com.mobl.clothingmarket.adapter.ProductAdapter;
import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.Product;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView filtersChipsRecycler;
    private ProductAdapter adapter;
    private FilterChipAdapter filterChipAdapter;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;
    private List<Product> allProducts;
    private com.google.android.material.textfield.TextInputEditText searchEditText;
    private com.google.android.material.textfield.TextInputLayout searchInputLayout;
    private com.google.android.material.button.MaterialButton filtersButton;
    
    private String selectedCategory = null;
    private Double minPrice = null;
    private Double maxPrice = null;
    private Boolean inStock = null;
    private String selectedSize = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(false);

        database = AppDatabase.getInstance(requireContext());
        prefsHelper = new SharedPreferencesHelper(requireContext());

        searchInputLayout = view.findViewById(R.id.search_input_layout);
        searchEditText = view.findViewById(R.id.search_edit_text);
        filtersButton = view.findViewById(R.id.btn_filters);
        
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchInputLayout.setBoxStrokeColor(getResources().getColor(R.color.white, null));
            } else {
                searchInputLayout.setBoxStrokeColor(getResources().getColor(R.color.light_text_secondary, null));
            }
        });
        filtersChipsRecycler = view.findViewById(R.id.filters_chips_recycler);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        filtersChipsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        filterChipAdapter = new FilterChipAdapter(filter -> {
            removeFilter(filter);
        });
        filtersChipsRecycler.setAdapter(filterChipAdapter);
        
        filtersButton.setOnClickListener(v -> showFiltersDialog());
        updateActiveFiltersDisplay();

        allProducts = new ArrayList<>();
        adapter = new ProductAdapter(allProducts, new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onAddToCartClick(Product product) {
                if (prefsHelper.isLoggedIn()) {
                    addToCart(product);
                } else {
                    Toast.makeText(getContext(), R.string.login_required, Toast.LENGTH_SHORT).show();
                }
            }
        }, prefsHelper.isLoggedIn());

        recyclerView.setAdapter(adapter);
        loadProducts();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchEditText != null && searchEditText.hasFocus()) {
                    searchEditText.clearFocus();
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager) requireContext()
                                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    }
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadProducts();
                } else {
                    searchProducts(query);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });

        recyclerView.setOnTouchListener((v, event) -> {
            if (searchEditText.hasFocus()) {
                searchEditText.clearFocus();
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) requireContext()
                                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                }
            }
            return false;
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setLoggedIn(prefsHelper.isLoggedIn());
        loadProducts();
    }

    private void loadProducts() {
        applyFilters();
    }

    private void searchProducts(String query) {
        String searchQuery = "%" + query + "%";
        List<Product> results = database.productDao().searchProducts(searchQuery);
        results = applyFiltersToResults(results);
        adapter.updateProducts(results);
    }
    
    private void applyFilters() {
        Integer inStockInt = null;
        if (inStock != null) {
            inStockInt = inStock ? 1 : 0;
        }
        List<Product> products = database.productDao().getFilteredProducts(
            selectedCategory, minPrice, maxPrice, inStockInt, selectedSize);
        adapter.updateProducts(products);
    }
    
    private List<Product> applyFiltersToResults(List<Product> products) {
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            boolean matches = true;
            
            if (selectedCategory != null && !product.getCategory().equals(selectedCategory)) {
                matches = false;
            }
            if (minPrice != null && product.getPrice() < minPrice) {
                matches = false;
            }
            if (maxPrice != null && product.getPrice() > maxPrice) {
                matches = false;
            }
            if (inStock != null && product.isInStock() != inStock) {
                matches = false;
            }
            if (selectedSize != null && !product.getSize().equals(selectedSize)) {
                matches = false;
            }
            
            if (matches) {
                filtered.add(product);
            }
        }
        return filtered;
    }
    
    private void showFiltersDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Фильтры");
        
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filters, null);
        builder.setView(dialogView);
        
        com.google.android.material.textfield.MaterialAutoCompleteTextView categoryFilter = 
            dialogView.findViewById(R.id.filter_category);
        com.google.android.material.textfield.TextInputEditText priceFrom = 
            dialogView.findViewById(R.id.filter_price_from);
        com.google.android.material.textfield.TextInputEditText priceTo = 
            dialogView.findViewById(R.id.filter_price_to);
        com.google.android.material.textfield.MaterialAutoCompleteTextView stockFilter = 
            dialogView.findViewById(R.id.filter_stock);
        com.google.android.material.textfield.MaterialAutoCompleteTextView sizeFilter = 
            dialogView.findViewById(R.id.filter_size);
        
        String[] categories = getResources().getStringArray(R.array.product_categories);
        String[] categoriesWithAll = new String[categories.length + 1];
        categoriesWithAll[0] = getString(R.string.all);
        System.arraycopy(categories, 0, categoriesWithAll, 1, categories.length);
        ArrayAdapter<String> categoryAdapter = new android.widget.ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, categoriesWithAll);
        categoryFilter.setAdapter(categoryAdapter);
        if (selectedCategory != null) {
            categoryFilter.setText(selectedCategory, false);
        } else {
            categoryFilter.setText(getString(R.string.all), false);
        }
        
        if (minPrice != null) {
            priceFrom.setText(String.valueOf(minPrice.intValue()));
        }
        if (maxPrice != null) {
            priceTo.setText(String.valueOf(maxPrice.intValue()));
        }
        
        String[] stockOptions = {
            getString(R.string.all),
            getString(R.string.in_stock_only),
            getString(R.string.out_of_stock_only)
        };
        ArrayAdapter<String> stockAdapter = new android.widget.ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, stockOptions);
        stockFilter.setAdapter(stockAdapter);
        if (inStock == null) {
            stockFilter.setText(getString(R.string.all), false);
        } else if (inStock) {
            stockFilter.setText(getString(R.string.in_stock_only), false);
        } else {
            stockFilter.setText(getString(R.string.out_of_stock_only), false);
        }
        
        String[] sizes = getResources().getStringArray(R.array.product_sizes);
        String[] sizesWithAll = new String[sizes.length + 1];
        sizesWithAll[0] = getString(R.string.all);
        System.arraycopy(sizes, 0, sizesWithAll, 1, sizes.length);
        ArrayAdapter<String> sizeAdapter = new android.widget.ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, sizesWithAll);
        sizeFilter.setAdapter(sizeAdapter);
        if (selectedSize != null) {
            sizeFilter.setText(selectedSize, false);
        } else {
            sizeFilter.setText(getString(R.string.all), false);
        }
        
        builder.setPositiveButton("Применить", (dialog, which) -> {
            try {
                String category = categoryFilter.getText().toString();
                selectedCategory = category.equals(getString(R.string.all)) ? null : category;
                
                String priceFromStr = priceFrom.getText().toString().trim();
                minPrice = priceFromStr.isEmpty() ? null : Double.parseDouble(priceFromStr);
                
                String priceToStr = priceTo.getText().toString().trim();
                maxPrice = priceToStr.isEmpty() ? null : Double.parseDouble(priceToStr);
                
                String stock = stockFilter.getText().toString();
                if (stock.equals(getString(R.string.all))) {
                    inStock = null;
                } else if (stock.equals(getString(R.string.in_stock_only))) {
                    inStock = true;
                } else {
                    inStock = false;
                }
                
                String size = sizeFilter.getText().toString();
                selectedSize = size.equals(getString(R.string.all)) ? null : size;
                
                String searchQuery = searchEditText.getText().toString().trim();
                if (searchQuery.isEmpty()) {
                    applyFilters();
                } else {
                    searchProducts(searchQuery);
                }
                updateActiveFiltersDisplay();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Неверный формат цены", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNeutralButton("Сбросить", (dialog, which) -> {
            selectedCategory = null;
            minPrice = null;
            maxPrice = null;
            inStock = null;
            selectedSize = null;
            
            String searchQuery = searchEditText.getText().toString().trim();
            if (searchQuery.isEmpty()) {
                loadProducts();
            } else {
                searchProducts(searchQuery);
            }
            updateActiveFiltersDisplay();
        });
        
        builder.setNegativeButton("Отмена", null);
        
        builder.show();
    }

    private void updateActiveFiltersDisplay() {
        List<FilterChipAdapter.FilterItem> activeFilters = new ArrayList<>();
        
        if (selectedCategory != null) {
            activeFilters.add(new FilterChipAdapter.FilterItem("Категория: " + selectedCategory, 
                FilterChipAdapter.FilterType.CATEGORY));
        }
        
        if (minPrice != null || maxPrice != null) {
            String priceText = "Цена: ";
            if (minPrice != null && maxPrice != null) {
                priceText += minPrice.intValue() + " - " + maxPrice.intValue() + " ₽";
            } else if (minPrice != null) {
                priceText += "от " + minPrice.intValue() + " ₽";
            } else {
                priceText += "до " + maxPrice.intValue() + " ₽";
            }
            activeFilters.add(new FilterChipAdapter.FilterItem(priceText, 
                FilterChipAdapter.FilterType.PRICE));
        }
        
        if (inStock != null) {
            String stockText = inStock ? "В наличии" : "Нет в наличии";
            activeFilters.add(new FilterChipAdapter.FilterItem(stockText, 
                FilterChipAdapter.FilterType.STOCK));
        }
        
        if (selectedSize != null) {
            activeFilters.add(new FilterChipAdapter.FilterItem("Размер: " + selectedSize, 
                FilterChipAdapter.FilterType.SIZE));
        }
        
        filterChipAdapter.updateFilters(activeFilters);
        
        if (activeFilters.isEmpty()) {
            filtersChipsRecycler.setVisibility(View.GONE);
        } else {
            filtersChipsRecycler.setVisibility(View.VISIBLE);
        }
    }
    
    private void removeFilter(FilterChipAdapter.FilterItem filter) {
        switch (filter.getType()) {
            case CATEGORY:
                selectedCategory = null;
                break;
            case PRICE:
                minPrice = null;
                maxPrice = null;
                break;
            case STOCK:
                inStock = null;
                break;
            case SIZE:
                selectedSize = null;
                break;
        }
        
        String searchQuery = searchEditText.getText().toString().trim();
        if (searchQuery.isEmpty()) {
            applyFilters();
        } else {
            searchProducts(searchQuery);
        }
        updateActiveFiltersDisplay();
    }

    private void addToCart(Product product) {
        int userId = prefsHelper.getUserId();
        if (userId != -1) {
            com.mobl.clothingmarket.model.CartItem existingItem = 
                database.cartDao().getCartItem(userId, product.getId());
            
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                database.cartDao().updateCartItem(existingItem);
            } else {
                com.mobl.clothingmarket.model.CartItem newItem = 
                    new com.mobl.clothingmarket.model.CartItem(userId, product.getId(), 1);
                database.cartDao().insertCartItem(newItem);
            }
            Toast.makeText(getContext(), "Товар добавлен в корзину", Toast.LENGTH_SHORT).show();
        }
    }
}

