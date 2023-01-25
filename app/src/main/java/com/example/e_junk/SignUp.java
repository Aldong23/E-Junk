package com.example.e_junk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;

public class SignUp extends AppCompatActivity {

    Button signupBTN, alreadyaccountBTN;
    TextInputEditText nameET, usernameET, contactET, emailET, passwordET, birthdayET;
    AutoCompleteTextView barangayET, zoneET;
    DatePickerDialog.OnDateSetListener onDateSetListener;

    UserHelperClass helperClass;
    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    ProgressDialog progressDialog;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String contactPattern = "[0][6-9][0-9]{9}";

    //spinner dropdown
    String[] brgyItems = {"Pugaro"};
    String[] zoneItems = {"Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7"};
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupBTN = findViewById(R.id.signupBTN);
        alreadyaccountBTN = findViewById(R.id.alreadyaccountBTN);
        nameET = findViewById(R.id.nameET);
        usernameET = findViewById(R.id.usernameET);
        contactET = findViewById(R.id.contactET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        barangayET = findViewById(R.id.barangayET);
        zoneET = findViewById(R.id.zoneET);
        birthdayET = findViewById(R.id.birthdayET);
        progressDialog = new ProgressDialog(this);
        ejunkAuth = FirebaseAuth.getInstance();

        //for dropdown town and brgy
        adapterItems = new ArrayAdapter<String>(this,R.layout.dropdown_layout,brgyItems);
        barangayET.setAdapter(adapterItems);
        adapterItems = new ArrayAdapter<String>(this,R.layout.dropdown_layout,zoneItems);
        zoneET.setAdapter(adapterItems);

        //dateinputPicker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        birthdayET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SignUp.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,onDateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = month+"/"+dayOfMonth+"/"+year;
                birthdayET.setText(date);
            }
        };

        //if the user has already an account
        alreadyaccountBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
            }
        });

        signupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformAuth();


            }
        });

    }//end of the inner block

    private void PerformAuth() {
        String name = nameET.getText().toString().trim();
        String username = usernameET.getText().toString().trim();
        String contact = contactET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String barangay = barangayET.getText().toString().trim();
        String zone = zoneET.getText().toString().trim();
        String birthday = birthdayET.getText().toString().trim();
        String usertype = "normalUser";

        if(name.isEmpty()){
            nameET.setError("Please enter your full name");
            nameET.requestFocus();
        }else if(username.isEmpty()){
            usernameET.setError("Please enter your username");
            usernameET.requestFocus();
        }else if(contact.isEmpty()){
            contactET.setError("Please enter your contact number");
            contactET.requestFocus();
        }else if(!contact.matches(contactPattern)){
            contactET.setError("Enter correct contact number");
            contactET.requestFocus();
        }else if(!email.matches(emailPattern)){
            emailET.setError("Enter correct Email");
            emailET.requestFocus();
        }else if(password.isEmpty()){
            passwordET.setError("Please enter your password");
            passwordET.requestFocus();
        }else if(password.length() < 8){
            passwordET.setError("Password is weak");
            passwordET.requestFocus();
        }else if(barangay.isEmpty()){
            barangayET.setError("Please enter your Barangay");
            barangayET.requestFocus();
        }else if(zone.isEmpty()){
            zoneET.setError("Please enter your zone");
            barangayET.setError(null);
            zoneET.requestFocus();
        }else if(birthday.isEmpty()){
            birthdayET.setError("Please enter your birthday");
            zoneET.setError(null);
            birthdayET.requestFocus();
        }else{
            progressDialog.setMessage("Signing up please wait");
            progressDialog.setTitle("Registration");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            ejunkAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        ejunkUser = ejunkAuth.getCurrentUser();
                        helperClass = new UserHelperClass(name, username, contact, barangay, zone, birthday, usertype);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
                        reference.child(ejunkUser.getUid()).setValue(helperClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   progressDialog.dismiss();
                                   ejunkUser.sendEmailVerification();
                                   redirectUser();
                                   Toast.makeText(SignUp.this, "Registration Successfully", Toast.LENGTH_SHORT).show();
                               }else{
                                   progressDialog.dismiss();
                                   Toast.makeText(SignUp.this, "Registration failed. Please try again", Toast.LENGTH_SHORT).show();
                               }
                            }
                        });

                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(SignUp.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void redirectUser() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        Intent intent = new Intent(SignUp.this, Verification.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);

    }


}//end of bracket