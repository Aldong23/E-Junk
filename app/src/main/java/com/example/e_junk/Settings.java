package com.example.e_junk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class Settings extends AppCompatActivity {

    TextInputEditText E_currentEmail, E_password, E_newEmail, P_currentPassword, P_newPassword;
    Button E_Authenticate, E_Update, P_Authenticate, P_Update;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String OldEmail;
    String CurrentPassword;

    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    ProgressDialog progressDialog;

    private final static String TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        //need
        progressDialog = new ProgressDialog(this);
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();

        //change email
        E_currentEmail = findViewById(R.id.E_emailEdit);
        E_password = findViewById(R.id.E_passwordEdit);
        E_newEmail = findViewById(R.id.E_newemailEdit);
        E_Authenticate = findViewById(R.id.E_AthenticateEmail);
        E_Update = findViewById(R.id.E_UpdateEmail);

        E_Update.setEnabled(false);
        E_newEmail.setEnabled(false);

        E_Authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticateEmail();
            }
        });

        E_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEmail();
            }
        });

        //change password
        P_currentPassword = findViewById(R.id.P_passwordEdit);
        P_newPassword = findViewById(R.id.P_newpasswordEdit);
        P_Authenticate = findViewById(R.id.P_AthenticatePassword);
        P_Update = findViewById(R.id.P_UpdatePassword);

        P_newPassword.setEnabled(false);
        P_Update.setEnabled(false);

        P_Authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AthenticatePassword();
            }
        });

        P_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword();
            }
        });

    }

    private void updatePassword() {
        String NewPassword = P_newPassword.getText().toString().trim();

        if(NewPassword.isEmpty()){
            P_newPassword.setError("Please enter new password");
            P_newPassword.requestFocus();
        }else if(NewPassword.length() < 8){
            P_newPassword.setError("Password is weak");
            P_newPassword.requestFocus();
        }else if(NewPassword.equals(CurrentPassword)){
            P_newPassword.setError("Password cannot be the same as old password");
            P_newPassword.requestFocus();
        }else{
            progressDialog.setMessage("Updating your password.. Please wait...");
            progressDialog.setTitle("Update Password");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            ejunkUser.updatePassword(NewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        ejunkUser.sendEmailVerification();
                        redirectUser();
                        Toast.makeText(Settings.this, "Password has been updated. Please log in again to continue", Toast.LENGTH_LONG).show();
                    }else {
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        }catch(FirebaseAuthInvalidUserException e){
                            Toast.makeText(Settings.this, "Email does not exist or is no longer valid", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseAuthInvalidCredentialsException e){
                            Toast.makeText(Settings.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseNetworkException e){
                            Toast.makeText(Settings.this, "Network error", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(Settings.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private void AthenticatePassword() {
        CurrentPassword = P_currentPassword.getText().toString().trim();

        if(CurrentPassword.isEmpty()){
            P_currentPassword.setError("Please enter your current password");
            P_currentPassword.requestFocus();
        } else {
            progressDialog.setMessage("Authenticating.. Please wait");
            progressDialog.setTitle("Authenticate");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(ejunkUser.getEmail(), CurrentPassword);
            ejunkUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(Settings.this, "Password has been verified. You can now update your password", Toast.LENGTH_LONG).show();
                        P_Authenticate.setEnabled(false);
                        P_currentPassword.setEnabled(false);
                        P_newPassword.setEnabled(true);
                        P_Update.setEnabled(true);
                    }else{
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        }catch(FirebaseAuthInvalidUserException e){
                            Toast.makeText(Settings.this, "Email does not exist or is no longer valid", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseAuthInvalidCredentialsException e){
                            Toast.makeText(Settings.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseNetworkException e){
                            Toast.makeText(Settings.this, "Network error", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(Settings.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
    }

    private void updateEmail() {
        String newEmail = E_newEmail.getText().toString().trim();

        if(newEmail.isEmpty()){
            E_newEmail.setError("Please enter your email");
            E_newEmail.requestFocus();
        }else if(!newEmail.matches(emailPattern)){
            E_newEmail.setError("Enter correct Email");
            E_newEmail.requestFocus();
        }else if(newEmail.equals(OldEmail)){
            E_newEmail.setError("New email is required");
            E_newEmail.requestFocus();
        } else{
            progressDialog.setMessage("Updating.. Please wait for moment");
            progressDialog.setTitle("Update Email");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            ejunkUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        ejunkUser.sendEmailVerification();
                        redirectUser();
                        Toast.makeText(Settings.this, "Email has been updated. Verify your email and Log In again", Toast.LENGTH_LONG).show();
                    }else{
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        }catch(FirebaseAuthInvalidUserException e){
                            Toast.makeText(Settings.this, "Email does not exist or is no longer valid", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseAuthInvalidCredentialsException e){
                            Toast.makeText(Settings.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseNetworkException e){
                            Toast.makeText(Settings.this, "Network error", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(Settings.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

    }

    private void redirectUser() {
        ejunkAuth.signOut();
        Intent intent = new Intent(Settings.this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void AuthenticateEmail() {
        OldEmail = ejunkUser.getEmail();

        String currentEmail = E_currentEmail.getText().toString().trim();
        String Userpassword = E_password.getText().toString().trim();

        if(!currentEmail.matches(emailPattern)){
            E_currentEmail.setError("Enter correct Email");
            E_currentEmail.requestFocus();
        }else if(!currentEmail.equals(OldEmail)){
            E_currentEmail.setError("Your old email not match");
            E_currentEmail.requestFocus();
        }else if(Userpassword.isEmpty()){
            E_password.setError("Please enter your password");
            E_password.requestFocus();
        }else{
            progressDialog.setMessage("Authenticating.. Please wait");
            progressDialog.setTitle("Authenticate");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, Userpassword);

            ejunkUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(Settings.this, "Password has been verified. You can now update your email", Toast.LENGTH_LONG).show();
                        E_Update.setEnabled(true);
                        E_newEmail.setEnabled(true);
                        E_Authenticate.setEnabled(false);
                        E_currentEmail.setEnabled(false);
                        E_password.setEnabled(false);
                    }else{
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        }catch(FirebaseAuthInvalidUserException e){
                            Toast.makeText(Settings.this, "Email does not exist or is no longer valid", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseAuthInvalidCredentialsException e){
                            Toast.makeText(Settings.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }catch(FirebaseNetworkException e){
                            Toast.makeText(Settings.this, "Network error", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(Settings.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}