package com.mobl.clothingmarket;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.fragment.CartFragment;
import com.mobl.clothingmarket.fragment.HomeFragment;
import com.mobl.clothingmarket.fragment.ProfileFragment;
import com.mobl.clothingmarket.model.Product;
import com.mobl.clothingmarket.model.User;

public class MainActivity extends AppCompatActivity {
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(this);

        if (database.productDao().getAllProducts().isEmpty()) {
            initializeSampleData();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void initializeSampleData() {
        Product p1 = new Product("Футболка классическая", "Удобная хлопковая футболка", 1999.0, "Футболки", "M", "Белый", 15);
        Product p2 = new Product("Джинсы", "Классические джинсы", 4999.0, "Брюки", "32", "Синий", 8);
        Product p3 = new Product("Куртка", "Теплая зимняя куртка", 8999.0, "Верхняя одежда", "L", "Черный", 0);
        Product p4 = new Product("Кроссовки", "Спортивные кроссовки", 5999.0, "Обувь", "42", "Белый", 12);
        
        database.productDao().insertProduct(p1);
        database.productDao().insertProduct(p2);
        database.productDao().insertProduct(p3);
        database.productDao().insertProduct(p4);

        User admin = new User("admin@admin.com", "admin123", "Администратор", true);
        database.userDao().insertUser(admin);
    }
}
