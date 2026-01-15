package com.mobl.clothingmarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.User;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = AppDatabase.getInstance(this);
        prefsHelper = new SharedPreferencesHelper(this);

        nameEditText = findViewById(R.id.edit_name);
        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        registerButton = findViewById(R.id.btn_register);

        registerButton.setOnClickListener(v -> register());
    }

    private void register() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        User existingUser = database.userDao().getUserByEmail(email);
        if (existingUser != null) {
            Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(email, password, name, false);
        long userId = database.userDao().insertUser(newUser);
        
        if (userId > 0) {
            prefsHelper.saveUserSession((int) userId, false);
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
        }
    }
}



