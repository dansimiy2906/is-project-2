package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TalentCategories extends AppCompatActivity {
    private BottomNavigationView bottom_nav;


    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talent_categories);
        auth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNav);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,
                new Fragment2()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNav = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selected = null;
            switch (item.getItemId()){
                case R.id.home_bottom:
                    selected = new Fragment4();
                    break;
                case R.id.ask_bottom:
                    selected = new Fragment2();
                    break;

                case R.id.profile_bottom:
                    selected = new Fragment1();
                    break;

            }

            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,
                    selected).commit();


            return true;

        }
    };

}