package com.huce.mynotes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.huce.mynotes.R;

public class SignupActivity extends AppCompatActivity {

    private EditText mSEmail, mSPassword;
    private Button mBtnSignup;
    private TextView mLogin;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();
        processEvents();
    }

    private void init() {
        try {
            mSEmail = findViewById(R.id.edSEmail);
            mSPassword = findViewById(R.id.edSPassword);
            mBtnSignup = findViewById(R.id.btnSignup);
            mLogin = findViewById(R.id.tvWantToLogin);
            firebaseAuth = FirebaseAuth.getInstance();
        } catch (Exception ex) {
            Log.e("onCreate", ex.getMessage());
        }
    }

    private void processEvents(){
        try{
            mLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SignupActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            });

            mBtnSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = mSEmail.getText().toString().trim();
                    String password = mSPassword.getText().toString().trim();
                    if (email.isEmpty() || password.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"All fields are Required!",Toast.LENGTH_SHORT).show();
                    }
                    else if (password.length() < 8)
                    {
                        Toast.makeText(getApplicationContext(),"Password should greater than 8 digits!",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //Firebase
                        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(),"Registration Successful!",Toast.LENGTH_SHORT).show();
                                    sendEmail();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),"Failed to register!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }catch (Exception ex){
            Log.e("Events: ",ex.getMessage());
        }
    }

    private void sendEmail()
    {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(),"Verification email is sent, verify and login again!",Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    finish();
                    startActivity(new Intent(SignupActivity.this,MainActivity.class));
                }
            });
        }
    }
}