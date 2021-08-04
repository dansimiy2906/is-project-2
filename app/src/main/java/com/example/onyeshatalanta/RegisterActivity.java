package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText userEmail,userPassword,userConfirmpassword;
    private Button registerbtn,goTologinbtn;
    private CheckBox hidepass;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStrore;
    private ProgressDialog loadingBar;
    private CheckBox TalentedUser,OpportunityProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        fStrore = FirebaseFirestore.getInstance();
        userEmail = (EditText) findViewById(R.id.register_email_et);
        userPassword = (EditText) findViewById(R.id.register_password_et);
        userConfirmpassword = (EditText) findViewById(R.id.register_confirmpassword_et);
        registerbtn = (Button) findViewById(R.id.button_register);
        goTologinbtn = (Button) findViewById(R.id.signup_to_login);
        hidepass = (CheckBox) findViewById(R.id.register_checkbox);
        TalentedUser = (CheckBox) findViewById(R.id.isTalentedUser);

        OpportunityProvider = (CheckBox) findViewById(R.id.isOportunityProvider);
        loadingBar = new ProgressDialog(this);


        hidepass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    userPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    userConfirmpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {

                    userPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    userConfirmpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }

        });

        //check Only one box
        TalentedUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    OpportunityProvider.setChecked(false);
                }
            }
        });
        OpportunityProvider.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    TalentedUser.setChecked(false);
                }
            }
        });
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

        goTologinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {
        Intent sendtoMain = new Intent(RegisterActivity.this,MainActivity.class);
        sendtoMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendtoMain);
        finish();
    }

    private void CreateNewAccount() {
        String email = userEmail.getText().toString();
        String userpassword = userPassword.getText().toString();
        String cPass = userConfirmpassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Email Field Cannot Be Empty",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userpassword)){
            Toast.makeText(this, "Password Cannot be Empty", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cPass)){
            Toast.makeText(this, "Kindly Confirm Password", Toast.LENGTH_SHORT).show();
        }
        else if(!userpassword.equals(cPass)){
            Toast.makeText(this, "Passwords Do Not Match!!", Toast.LENGTH_SHORT).show();
        }
        //else if (!(TalentedUser.isChecked() || OpportunityProvider.isChecked())){
            //Toast.makeText(this, "Select Your Account Type", Toast.LENGTH_SHORT).show();
           // return;
       // }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait while your account is being created....");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email,userpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(RegisterActivity.this, "You Have Been Successfully Registered", Toast.LENGTH_SHORT).show();

                                Intent sendtoProfile = new Intent(RegisterActivity.this,CreateProfile.class);
                                //sendtoMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(sendtoProfile);
                                finish();



                               // DocumentReference db = fStrore.collection("Users").document(user.getUid());
                               // Map<String,Object> userInfo = new HashMap<>();
                                //userInfo.put("UserEmail",userEmail.getText().toString());
                                //Accesslevel
                                //if (TalentedUser.isChecked()){
                                    //userInfo.put("IsTalentedUser","1");
                                //}
                                //if (OpportunityProvider.isChecked()){
                                   // userInfo.put("IsOpportunityProvider","0");
                               // }
                                //db.set(userInfo);
                               // if (TalentedUser.isChecked()){
                                    //sendUserToSetupActivity();
                               // }
                                //if(OpportunityProvider.isChecked()){
                                    //startActivity(new Intent(getApplicationContext(),TalentCategories.class));
                                    //finish();
                               // }

                                loadingBar.dismiss();


                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error Occured:"+ message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                        }
                    });
        }

    }

   // private void sendUserToSetupActivity() {
        //Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
        //setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
       // startActivity(setupIntent);
        //finish();
    //}


}