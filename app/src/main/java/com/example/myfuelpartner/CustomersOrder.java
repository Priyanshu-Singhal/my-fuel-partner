package com.example.myfuelpartner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomersOrder extends AppCompatActivity {



    private String getType;

    private CircleImageView profileImageView;
    private EditText nameEditText, phoneEditText, quantityEditText, dateEditText;
    private Button orderBtn;


    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private String checker = "";

    RadioGroup radioGroup;
    RadioButton radioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_order);

        getType = getIntent().getStringExtra("type");

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);

        nameEditText =findViewById(R.id.name);
        phoneEditText =findViewById(R.id.phone_number);
        quantityEditText =findViewById(R.id.orderQuantity);
        dateEditText =findViewById(R.id.Date);
        orderBtn = findViewById(R.id.orderButton);

        radioGroup = findViewById(R.id.radio);
        int radioId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);

        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                validateControllers();
            }
        });


    }

    private void validateControllers()
    {

        if (TextUtils.isEmpty(nameEditText.getText().toString()))
        {
            Toast.makeText(this, "Please enter your Name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phoneEditText.getText().toString()))
        {
            Toast.makeText(this, "Please enter your Phone Number", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(quantityEditText.getText().toString()))
        {
            Toast.makeText(this, "Please enter Quantity", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(dateEditText.getText().toString()))
        {
            Toast.makeText(this, "Please select Date", Toast.LENGTH_SHORT).show();
        }
        else
        {

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("name",nameEditText.getText().toString());
            userMap.put("phone",phoneEditText.getText().toString());
            userMap.put("quantity",quantityEditText.getText().toString());
            userMap.put("date",dateEditText.getText().toString());
            userMap.put("fuel",radioButton.getText().toString());

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);

            Toast.makeText(CustomersOrder.this ,"Your order is placed successfully.. " ,Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), CustomerMapsActivity.class));
            finish();
        }
    }
}