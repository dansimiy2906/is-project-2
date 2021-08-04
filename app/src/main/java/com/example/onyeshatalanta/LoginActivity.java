package com.example.onyeshatalanta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class LoginActivity extends AppCompatActivity {
    private Button LoginButton,RegisterButton;
    private ImageView googlesigninButton;
    private EditText UserEmail,UserPassword;
    private TextView forgotPasswordLink;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private ProgressDialog loadingBar;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //mToolbar = (Toolbar) findViewById(R.id.loginToolbar);
        //setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("Login");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        RegisterButton = (Button) findViewById(R.id.login_to_signup);
        forgotPasswordLink = (TextView) findViewById(R.id.resetPassword);
        LoginButton = (Button) findViewById(R.id.button_login);
        googlesigninButton = (ImageView) findViewById(R.id.google_signin_button);
        UserEmail = (EditText) findViewById(R.id.login_email_et);
        UserPassword = (EditText) findViewById(R.id.login_password_et);
        checkBox = (CheckBox) findViewById(R.id.login_checkbox);
        loadingBar = new ProgressDialog(this);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    UserPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //    confirm_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {

                    UserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //  confirm_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowingUserToLogin();
            }
        });
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Connection to Google Signin Failed!", Toast.LENGTH_SHORT).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        googlesigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            loadingBar.setTitle("Google Signin");
            loadingBar.setMessage("Login might take a few seconds,please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Please wait while we Authenticate your Account", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Authentication Failed!!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithCredential:success");
                            sendUserToMainActivity();
                            loadingBar.dismiss();
                        }
                        else
                        {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().toString();
                            sendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this, "Not Authenticated : " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
       if (FirebaseAuth.getInstance().getCurrentUser() != null){
           DocumentReference df = FirebaseFirestore.getInstance().collection("user")
                   .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
           df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
               @Override
               public void onSuccess(DocumentSnapshot documentSnapshot) {
                   if(documentSnapshot.getString("Talented User") != null){
                       sendUserToMainActivity();
                   }
                   if (documentSnapshot.getString("Opportunity Provider")!= null){
                       sendUserToProviderActivity();
                   }
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   FirebaseAuth.getInstance().signOut();
                   startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                   finish();
               }
           });
       }
    }
    private void AllowingUserToLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Input Your Email", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Input Your Password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Loging Into My Talents");
            loadingBar.setMessage("Login might take a few seconds,please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    CheckUserAccessLevel(authResult.getUser().getUid());
                    loadingBar.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, "Error Occured" , Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });
        }
    }
    private void CheckUserAccessLevel(String uid) {
        DocumentReference df = fStore.collection("user").document(uid);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("Talented User") != null){
                    sendUserToMainActivity();
                }
                if (documentSnapshot.getString("Opportunity Provider") !=null){
                    sendUserToProviderActivity();
                }
            }
        });
    }
    private void sendUserToProviderActivity() {
        Intent sendtoProvider = new Intent(LoginActivity.this,TalentCategories.class);
        startActivity(sendtoProvider);
        finish();
    }
    private void sendUserToMainActivity() {
        Intent sendtoMain = new Intent(LoginActivity.this,MainActivity.class);
        sendtoMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendtoMain);
        finish();
    }
    private void sendUserToRegisterActivity() {
        Intent goToRegister = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(goToRegister);
        finish();
    }
    private void sendUserToLoginActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}