package com.example.myfuelpartner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverProfile extends AppCompatActivity {

    private String getType;

    private CircleImageView profileImageView;
    private EditText nameEditText, phoneEditText, driverCarName;
    private Button submitBtn;


    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private String checker = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        getType = getIntent().getStringExtra("type");

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);


        profileImageView = findViewById(R.id.profile_image);
        nameEditText =findViewById(R.id.name);
        phoneEditText =findViewById(R.id.phone_number);
        driverCarName =findViewById(R.id.driver_car_name);
        submitBtn = findViewById(R.id.submit_button);



        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                validateControllers();
            }
        });



    }

    private void validateControllers()
    {
    String driver_name = nameEditText.getText().toString();
    String driver_phone = phoneEditText.getText().toString();
    String driver_car = driverCarName.getText().toString();
        if (TextUtils.isEmpty(driver_name))
        {
            Toast.makeText(this, "Please provide your Name.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(driver_phone))
        {
            Toast.makeText(this, "Please provide your Phone Number.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(driver_car))
        {
            Toast.makeText(this, "Please provide your car Name.", Toast.LENGTH_SHORT).show();
        }
        else
        {

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("name",driver_name);
            userMap.put("phone",driver_phone);
            userMap.put("car",driver_car);

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);

            Toast.makeText(DriverProfile.this ,"Your profile is registered successfully.. " ,Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), DriverMapsActivity.class));
            finish();
        }
    }
}
