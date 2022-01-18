package com.huce.mynotes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.huce.mynotes.R;

public class MainActivity extends AppCompatActivity {

    private EditText mLgEmail, mLgPassword;
    private Button mBtnLogin, mBtnWTSignup;
    private TextView mFgPass;
    private ImageView imgEye;
    private boolean isPasswordHidden = true;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        processEvents();
    }

    private void init() {
        mLgEmail = findViewById(R.id.edLgEmail);
        mLgPassword = findViewById(R.id.edLgPassword);
        mBtnLogin = findViewById(R.id.btnLogin);
        mBtnWTSignup = findViewById(R.id.btnWantToSignUp);
        mFgPass = findViewById(R.id.tvForgotPassword);
        imgEye = findViewById(R.id.imgEye);
    }

    private void processEvents(){
        try{
            firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser!=null)
            {
                finish();
                startActivity(new Intent(MainActivity.this, NotesActivity.class));
            }

            mBtnWTSignup.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,SignupActivity.class)));

            mFgPass.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class)));


            imgEye.setOnClickListener(view -> {
                showOrHidePassword();
            });

            mBtnLogin.setOnClickListener(view -> {
                String email = mLgEmail.getText().toString().trim();
                String password = mLgPassword.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"All fields are Required!",Toast.LENGTH_SHORT).show();
                }
                else
                {

                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                        {
                            checkEmailVerification();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Account doesn't Exist!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }catch (Exception ex){
            Log.e("Events: ",ex.getMessage());
        }
    }

    private void showOrHidePassword() {
        isPasswordHidden = !isPasswordHidden;
        if(!isPasswordHidden){
            imgEye.setImageDrawable(getDrawable(R.drawable.ic_show_password));
            mLgPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }else {
            imgEye.setImageDrawable(getDrawable(R.drawable.ic_hide_password));
            mLgPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        String pass = mLgPassword.getText().toString();
        mLgPassword.setText("");
        mLgPassword.append(pass);
    }

    private void checkEmailVerification()
    {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            if(firebaseUser.isEmailVerified())
            {
                Toast.makeText(getApplicationContext(),"Logged In!", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(MainActivity.this, NotesActivity.class));
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Verify your email first!", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
            }
        }
    }
}