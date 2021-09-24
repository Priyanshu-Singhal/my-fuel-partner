package com.example.myfuelpartner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CustomerSignup extends AppCompatActivity {
    TextInputLayout email , password;
    Button registerBtn ;
    boolean valid = true;

    //For displaying Loading Dialogue Box
    ProgressDialog loadingBar;

    //For Firebase Authentication
    FirebaseAuth fAuth ;


    DatabaseReference CustomerDatabaseRef;

    String onlineCustomerId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup);

        //Hiding Action Bar
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        //Getting Firebase Instance
        fAuth = FirebaseAuth.getInstance();

        email =  (TextInputLayout)findViewById(R.id.loginEmail);
        password =  (TextInputLayout)findViewById(R.id.loginPassword);
        registerBtn = findViewById(R.id.loginBtn);

        //Applying ProgressDialogue to this Activity
        loadingBar = new ProgressDialog(this);


        registerBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //checking that the Edit text are Empty or not
                checkField(email);
                checkField(password);

                if(valid){

                    //If valid data is entered then ProgressDialogue starts to show
                    loadingBar.setTitle("Customer's Registration");
                    loadingBar.setMessage("Please wait, while we are registering your profile...");
                    loadingBar.show();

                    //start the user registration
                    fAuth.createUserWithEmailAndPassword(email.getEditText().getText().toString() , password.getEditText().getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {


                                    onlineCustomerId = fAuth.getCurrentUser().getUid();
                                    CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                            .child("Users").child("Customers").child(onlineCustomerId);

                                    CustomerDatabaseRef.setValue(true);

                                    startActivity(new Intent(getApplicationContext() , CustomerMapsActivity.class));
                                    finish();

                                    Toast.makeText(CustomerSignup.this , "SignUp Successfully" , Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(CustomerSignup.this , "Registration Unsuccessful" , Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            startActivity(new Intent(getApplicationContext() , MainActivity.class));
                            finish();

                        }
                    });
                }

            }
        });

    }

    public boolean checkField(TextInputLayout textField)
    {
        if(textField.getEditText().getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;

        }else{
            valid=true;
        }
        return valid;
    }

}