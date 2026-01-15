package com.mobl.clothingmarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.User;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = AppDatabase.getInstance(this);
        prefsHelper = new SharedPreferencesHelper(this);

        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_login);
        registerLink = findViewById(R.id.link_register);

        loginButton.setOnClickListener(v -> login());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = database.userDao().login(email, password);
        if (user != null) {
            prefsHelper.saveUserSession(user.getId(), user.isAdmin());
            Toast.makeText(this, "Добро пожаловать, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
        }
    }
}



