package com.example.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Константы для логина и пароля
    private static final String CORRECT_LOGIN = "admin";
    private static final String CORRECT_PASSWORD = "password123";

    private EditText editTextLogin;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов интерфейса
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewResult = findViewById(R.id.textViewResult);

        // Установка подсказок (hint) для полей ввода
        editTextLogin.setHint("Введите логин");
        editTextPassword.setHint("Введите пароль");

        // Обработчик нажатия на кнопку "Вход"
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCredentials();
            }
        });
    }

    private void checkCredentials() {
        // Получение введенных значений
        String enteredLogin = editTextLogin.getText().toString().trim();
        String enteredPassword = editTextPassword.getText().toString().trim();

        // Проверка логина и пароля
        if (enteredLogin.equals(CORRECT_LOGIN) && enteredPassword.equals(CORRECT_PASSWORD)) {
            // Если верно - зеленым цветом "Верно"
            textViewResult.setText("Верно");
            textViewResult.setTextColor(Color.parseColor("#4CAF50")); // Зеленый цвет
        } else {
            // Если неверно - красным цветом "Вы ошиблись в логине или пароле"
            textViewResult.setText("Вы ошиблись в логине или пароле");
            textViewResult.setTextColor(Color.parseColor("#F44336")); // Красный цвет
        }

        // Очистка полей ввода
        editTextLogin.setText("");
        editTextPassword.setText("");
    }
}

