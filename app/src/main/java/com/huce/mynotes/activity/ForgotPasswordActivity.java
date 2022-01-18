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
import com.google.firebase.auth.FirebaseAuth;
import com.huce.mynotes.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText medEmail;
    private Button mBtnClick;
    private TextView mBack;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        init();
        processEvents();
    }

    private void init() {
        try {
            medEmail = findViewById(R.id.edEmail);
            mBtnClick = findViewById(R.id.btnClick);
            mBack = findViewById(R.id.tvBack);
            firebaseAuth = FirebaseAuth.getInstance();
        } catch (Exception ex) {
            Log.e("onCreate", ex.getMessage());
        }
    }

    private void processEvents(){
        try{
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        mBtnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = medEmail.getText().toString().trim();
                if (mail.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Enter your mail first!",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(),"Email sent, You can recover your password using this email!", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Email is Wrong or Account is not Exist!", Toast.LENGTH_SHORT).show();
                                finish();
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
}