package com.mobl.clothingmarket.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mobl.clothingmarket.AdminActivity;
import com.mobl.clothingmarket.LoginActivity;
import com.mobl.clothingmarket.R;
import com.mobl.clothingmarket.database.AppDatabase;
import com.mobl.clothingmarket.model.User;
import com.mobl.clothingmarket.util.SharedPreferencesHelper;

public class ProfileFragment extends Fragment {
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView roleTextView;
    private Button loginButton;
    private Button logoutButton;
    private Button adminButton;
    private LinearLayout userInfoLayout;
    private LinearLayout guestLayout;
    private AppDatabase database;
    private SharedPreferencesHelper prefsHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        database = AppDatabase.getInstance(requireContext());
        prefsHelper = new SharedPreferencesHelper(requireContext());

        nameTextView = view.findViewById(R.id.profile_name);
        emailTextView = view.findViewById(R.id.profile_email);
        roleTextView = view.findViewById(R.id.profile_role);
        loginButton = view.findViewById(R.id.btn_login);
        logoutButton = view.findViewById(R.id.btn_logout);
        adminButton = view.findViewById(R.id.btn_admin);
        userInfoLayout = view.findViewById(R.id.user_info_layout);
        guestLayout = view.findViewById(R.id.guest_layout);

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), LoginActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            prefsHelper.clearUserSession();
            updateUI();
            Toast.makeText(getContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
        });

        adminButton.setOnClickListener(v -> {
            if (prefsHelper.isAdmin()) {
                startActivity(new Intent(getContext(), AdminActivity.class));
            } else {
                Toast.makeText(getContext(), R.string.admin_only, Toast.LENGTH_SHORT).show();
            }
        });

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (prefsHelper.isLoggedIn()) {
            int userId = prefsHelper.getUserId();
            User user = database.userDao().getUserById(userId);
            
            if (user != null) {
                nameTextView.setText(user.getName());
                emailTextView.setText(user.getEmail());
                roleTextView.setText(user.isAdmin() ? "Администратор" : "Пользователь");
                
                if (user.isAdmin()) {
                    adminButton.setVisibility(View.VISIBLE);
                } else {
                    adminButton.setVisibility(View.GONE);
                }
            }
            
            userInfoLayout.setVisibility(View.VISIBLE);
            guestLayout.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            userInfoLayout.setVisibility(View.GONE);
            guestLayout.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            adminButton.setVisibility(View.GONE);
        }
    }
}



