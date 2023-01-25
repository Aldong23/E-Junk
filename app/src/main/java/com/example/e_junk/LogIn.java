package com.example.e_junk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LogIn extends AppCompatActivity {

    Button loginBTN, createaccountBTN, forgotpasswordBTN;
    TextInputEditText emailET, passwordET;

    private final static String TAG = "LogIn";
    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    ProgressDialog progressDialog;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    UserHelperClass helperClass;
    String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loginBTN = findViewById(R.id.loginBTN);
        createaccountBTN = findViewById(R.id.createaccountBTN);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        forgotpasswordBTN = findViewById(R.id.forgotpasswordBTN);
        progressDialog = new ProgressDialog(this);
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();

        //forgot password
        forgotpasswordBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogIn.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        createaccountBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });

        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformLogin();
            }
        });

    }//end of innerblock

    private void PerformLogin() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();


        if(!email.matches(emailPattern)){
            emailET.setError("Enter Correct Email");
        }else if(password.isEmpty()){
            passwordET.setError("Please enter your password");
        }else {
            progressDialog.setMessage("Logging in please wait");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            ejunkAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        ejunkUser = ejunkAuth.getCurrentUser();
                        if(ejunkUser.isEmailVerified()){
                            progressDialog.dismiss();
                            redirectUser();
                            Toast.makeText(LogIn.this, "LogIn Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            progressDialog.dismiss();
                            ejunkUser.sendEmailVerification();
                            ejunkAuth.signOut();
                            emailVerifyMessage();
                        }
                    }else {
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        }catch(FirebaseAuthInvalidUserException e){
                            Toast.makeText(LogIn.this, "Email does not exist or is no longer valid", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseAuthInvalidCredentialsException e){
                            Toast.makeText(LogIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseNetworkException e){
                            Toast.makeText(LogIn.this, "Network error", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(LogIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private void emailVerifyMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
        builder.setTitle("Email not verified")
                .setMessage("Please verify your email before logging in")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        builder.create().show();

    }

    private void redirectUser() {
        String UserID = ejunkUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
        reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                helperClass = snapshot.getValue(UserHelperClass.class);
                if(helperClass != null){
                    userType = helperClass.getUsertype();
                    if(userType.equals("adminUser")){

                    }else if(userType.equals("driverUser")){
                        Intent intent = new Intent(LogIn.this, MainActivityDriver.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(LogIn.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ejunkAuth.getCurrentUser() != null){
            progressDialog.setMessage("Logging in please wait");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            String UserID = ejunkUser.getUid();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
            reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    helperClass = snapshot.getValue(UserHelperClass.class);
                    if(helperClass != null){
                        userType = helperClass.getUsertype();
                        if(userType.equals("adminUser")){

                        }else if(userType.equals("driverUser")){
                            startActivity(new Intent(LogIn.this, MainActivityDriver.class));
                            finish();
                            progressDialog.dismiss();
                        }else {
                            startActivity(new Intent(LogIn.this, MainActivity.class));
                            finish();
                            progressDialog.dismiss();
                        }
                    }else{
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}//end of outerblock