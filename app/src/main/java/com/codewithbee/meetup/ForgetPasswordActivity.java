package com.codewithbee.meetup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class ForgetPasswordActivity extends AppCompatActivity {

    private Button mForgotPasswordButton;
    private EditText mEmail;
    private FirebaseAuth mAuth;
    private int flag;
    private String emailPattern = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.+[a-z]+", email;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();
        flag = 0;
        mForgotPasswordButton =(Button) findViewById(R.id.resetPasswordButton);
        mEmail = (EditText) findViewById(R.id.resetPasswordEmail);

        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString();
                if(email.equals(emailPattern)) {
                    Toast.makeText(ForgetPasswordActivity.this, "Invalid Email Address, enter valid email id", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                       flag = 1;
                       mAuth.sendPasswordResetEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                              if (task.isSuccessful()) {
                                  Toast.makeText(ForgetPasswordActivity.this, "Password", Toast.LENGTH_SHORT).show();
                              }else{
                                  Toast.makeText(ForgetPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                              }
                           }
                       });
                    }
                });
                if(flag == 0)
                {
                    Toast.makeText(ForgetPasswordActivity.this, "Email Address not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent btnCLick = new Intent(ForgetPasswordActivity.this,LoginActivity.class);
        startActivity(btnCLick);
        super.onBackPressed();
        finish();
        return;
    }
}