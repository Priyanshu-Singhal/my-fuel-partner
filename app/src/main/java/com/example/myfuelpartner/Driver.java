package com.example.myfuelpartner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class Driver extends AppCompatActivity
{
    Button btnSignIn;
    Button btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        //Hiding Action Bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        btnSignIn = findViewById(R.id.signin);
        btnSignUp = findViewById(R.id.signup);

        btnSignIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Driver.this, DriverSignin.class);
                startActivity(intent);
            }
        });


        btnSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                //Calling an intent by using Async Task bcoz main thread is doing too much work
                NewIntent newIntent = new NewIntent();
                newIntent.execute();

            }
        });
    }
    public class NewIntent extends AsyncTask
    {
        @Override
        protected Object doInBackground(Object... objects) {
            try {
                Intent intent = new Intent(Driver.this, DriverSignup.class);
                startActivity(intent);
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
            return null;
        }
    }
}


