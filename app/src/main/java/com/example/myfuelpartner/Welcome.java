package com.example.myfuelpartner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class Welcome extends AppCompatActivity {
    Button admin;
    Button users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        //Hiding Action Bar
        Objects.requireNonNull(getSupportActionBar()).hide();


        admin = findViewById(R.id.btnAdmin);
        users = findViewById(R.id.buttonUser);

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this , Driver.class);
                startActivity(intent);
            }
        });
        users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this , Customer.class);
                startActivity(intent);
            }
        });



    }

    }
