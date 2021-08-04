package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private Button resetPasswordSendEmailButton;
    private EditText resetEmailInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        resetEmailInput = (EditText) findViewById(R.id.reset_Pass_Email);
        resetPasswordSendEmailButton = (Button) findViewById(R.id.reset_password_btn);

        resetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = resetEmailInput.getText().toString();
                if (TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPasswordActivity.this, "Email Field is Required!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ResetPasswordActivity.this, "Reset Password Link Has been Sent to Your Email", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                                    }
                                    else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(ResetPasswordActivity.this, "Error Occured:" + message, Toast.LENGTH_SHORT).show();
                                    }


                                }
                            });
                }
            }
        });

    }
}