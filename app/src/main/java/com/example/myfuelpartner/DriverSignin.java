package com.example.myfuelpartner;
import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.Toast;

        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.material.textfield.TextInputLayout;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;

        import java.util.Objects;

public class DriverSignin extends AppCompatActivity {
    TextInputLayout email , password;
    Button loginBtn;
    boolean valid = true;


    //For Firebase Authentication
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_signup);

        //Hiding Action Bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        //Getting Firebase Instance
        fAuth = FirebaseAuth.getInstance();


        email = (TextInputLayout)findViewById(R.id.loginEmail);
        password = (TextInputLayout)findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Checking if the EditText is filled or not
                checkField(email);
                checkField(password);

                //Checking for a valid Email Id and Password
                if (valid) {
                    fAuth.signInWithEmailAndPassword(email.getEditText().getText().toString() , password.getEditText().getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(DriverSignin.this, "SignIn Successfully" , Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), DriverMapsActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e)
                        {
                          Toast.makeText(DriverSignin.this ,"Please enter a valid Email & Password..." , Toast.LENGTH_SHORT ).show();
                        }
                    });
                }
            }
        });

    }


    //checking that the EditText is empty or not
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





