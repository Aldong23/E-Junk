package com.example.e_junk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;

public class EditProfile extends AppCompatActivity {

    ImageView displaypicEdit;
    TextInputEditText nameEdit, usernameEdit, contactEdit, birthdayEdit;
    AutoCompleteTextView barangayEdit, zoneEdit;
    ProgressDialog progressDialog;
    Button saveBTN;

    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    StorageReference storageReference;
    Uri imgUri;
    UserHelperClass helperClass;
    private final static String TAG = "EditProfile";

    String contactPattern = "[0][6-9][0-9]{9}";
    String usertype;

    //spinner dropdown
    String[] brgyItems = {"Pugaro"};
    String[] zoneItems = {"Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7"};
    ArrayAdapter<String> adapterItems1, adapterItems2;

    DatePickerDialog.OnDateSetListener onDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        progressDialog = new ProgressDialog(this);
        displaypicEdit = findViewById(R.id.displaypicEdit);
        nameEdit = findViewById(R.id.nameEdit);
        usernameEdit = findViewById(R.id.usernameEdit);
        contactEdit = findViewById(R.id.contactEdit);
        barangayEdit = findViewById(R.id.barangayEdit);
        zoneEdit = findViewById(R.id.zoneEdit);
        birthdayEdit = findViewById(R.id.birthdayEdit);
        saveBTN = findViewById(R.id.saveBTN);
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();

        //current display pic
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");
        Uri uri = ejunkUser.getPhotoUrl();
        Picasso.get().load(uri).into(displaypicEdit);

        //get the user data
        getUserData();

        //for dropdown town and brgy
        adapterItems1 = new ArrayAdapter<String>(this,R.layout.dropdown_layout,brgyItems);
        adapterItems2 = new ArrayAdapter<String>(this,R.layout.dropdown_layout,zoneItems);


        //dateinputPicker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        birthdayEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EditProfile.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,onDateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = month+"/"+dayOfMonth+"/"+year;
                birthdayEdit.setText(date);
            }
        };

        //input picture
        displaypicEdit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if(!checkCameraPermission()){
                    requestCameraPermission();
                }else if(!checkStoragePermission()){
                    requestStoragePermission();
                }else{
                    PickImage();
                }
            }
        });

        //saving changes
        saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEdit.getText().toString().trim();
                String username = usernameEdit.getText().toString().trim();
                String contact = contactEdit.getText().toString().trim();
                String brgy = barangayEdit.getText().toString().trim();
                String zone = zoneEdit.getText().toString().trim();
                String birthday = birthdayEdit.getText().toString().trim();

                if(name.isEmpty()){
                    nameEdit.setError("Please enter your full name");
                    nameEdit.requestFocus();
                }else if(username.isEmpty()){
                    usernameEdit.setError("Please enter your username");
                    usernameEdit.requestFocus();
                }else if(contact.isEmpty()){
                    contactEdit.setError("Please enter your contact number");
                    contactEdit.requestFocus();
                }else if(!contact.matches(contactPattern)){
                    contactEdit.setError("Enter correct contact number");
                    contactEdit.requestFocus();
                }else if(brgy.isEmpty()){
                    barangayEdit.setError("Please enter your Barangay");
                    barangayEdit.requestFocus();
                }else if(zone.isEmpty()){
                    zoneEdit.setError("Please enter your zone");
                    barangayEdit.setError(null);
                    zoneEdit.requestFocus();
                }else if(birthday.isEmpty()){
                    birthdayEdit.setError("Please enter your birthday");
                    zoneEdit.setError(null);
                    birthdayEdit.requestFocus();
                }else{
                    progressDialog.setMessage("Updating your data. Please wait for a moment");
                    progressDialog.setTitle("Edit Profile");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    storeDisplayPicture();

                    String UserId = ejunkUser.getUid();
                    helperClass = new UserHelperClass(name, username, contact, brgy, zone, birthday, usertype);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
                    reference.child(UserId).setValue(helperClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                                ejunkUser.updateProfile(profileChangeRequest);
                                progressDialog.dismiss();
                                redirectUser();
                            }else{
                                progressDialog.dismiss();
                                try {
                                    throw task.getException();
                                }catch(FirebaseAuthInvalidUserException e){
                                    Toast.makeText(EditProfile.this, "Email does not exist or is no longer valid", Toast.LENGTH_SHORT).show();
                                }catch(FirebaseAuthInvalidCredentialsException e){
                                    Toast.makeText(EditProfile.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }catch(FirebaseNetworkException e){
                                    Toast.makeText(EditProfile.this, "Network error", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    Log.e(TAG, e.getMessage());
                                    Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }

            }
        });

    }

    private void redirectUser() {
        if(usertype.equals("adminUser")){

        }else if(usertype.equals("driverUser")){
            Intent intent = new Intent(EditProfile.this, MainActivityDriver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(EditProfile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void getUserData() {
        String UserId = ejunkUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
        reference.child(UserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                helperClass = snapshot.getValue(UserHelperClass.class);
                if(helperClass != null){
                    String username = helperClass.getUsername();
                    String fullname = helperClass.getName();
                    String contact = helperClass.getContact();
                    String barangay = helperClass.getBarangay();
                    String zone = helperClass.getZone();
                    String birthday = helperClass.getBirthday();
                    usertype = helperClass.getUsertype();

                    usernameEdit.setText(username);
                    nameEdit.setText(fullname);
                    contactEdit.setText(contact);
                    barangayEdit.setText(barangay);
                    zoneEdit.setText(zone);
                    birthdayEdit.setText(birthday);

                    barangayEdit.setAdapter(adapterItems1);
                    zoneEdit.setAdapter(adapterItems2);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void storeDisplayPicture() {
        if(imgUri != null){
            StorageReference filereference = storageReference.child(ejunkUser.getUid());

            filereference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filereference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;

                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            ejunkUser.updateProfile(profileUpdate);
                        }
                    });
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

    //picking image
    private void PickImage() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            ImagePicker.with(this).crop().compress(1024).start(101);
        }else{
            CropImage.activity().start(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    private boolean checkStoragePermission() {
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return res2;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

    }

    private boolean checkCameraPermission() {
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(requestCode == 101 && resultCode == RESULT_OK){
                imgUri = data.getData();
                Picasso.get().load(imgUri).into(displaypicEdit);
            }else{
                Toast.makeText(EditProfile.this, "No image is selected", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if(resultCode == RESULT_OK){
                    imgUri = result.getUri();
                    Picasso.get().load(imgUri).into(displaypicEdit);
                }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Toast.makeText(EditProfile.this, "No image is selected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}