package com.example.e_junk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Verification extends AppCompatActivity {

    TextView emailTV;
    Button resendBTN, continueBTN;
    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        emailTV = findViewById(R.id.emailTV);
        resendBTN = findViewById(R.id.resendBTN);
        continueBTN = findViewById(R.id.continueBTN);
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);

        String userEmail = ejunkUser.getEmail();
        emailTV.setText("userEmail");

        resendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ejunkUser.sendEmailVerification();
            }
        });

        continueBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformLogin();
            }
        });

    }

    private void PerformLogin() {
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

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
                        Toast.makeText(Verification.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        progressDialog.dismiss();
                        ejunkAuth.signOut();
                        emailVerifyMessage();
                    }
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(Verification.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void emailVerifyMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Verification.this);
        builder.setTitle("Email not verified")
                .setMessage("Please verify your email before logging in")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.create().show();

    }

    private void redirectUser() {
        Intent intent = new Intent(Verification.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}