package com.example.e_junk;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    ProgressBar progressDialog;
    UserHelperClass helperClass;

    ImageView displayProfileIV;
    TextView displaynameTV, displayfullnameTV, displaycontactTV, displayemailTV, displayaddressTV, displaybirthdayTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();

        progressDialog = (ProgressBar) view.findViewById(R.id.progressDialog);
        displayProfileIV = (ImageView) view.findViewById(R.id.displayProfileIV);
        displaynameTV = (TextView) view.findViewById(R.id.displaynameTV);
        displayfullnameTV = (TextView) view.findViewById(R.id.displayfullnameTV);
        displaycontactTV = (TextView) view.findViewById(R.id.displaycontactTV);
        displayemailTV = (TextView) view.findViewById(R.id.displayemailTV);
        displayaddressTV = (TextView) view.findViewById(R.id.displayaddressTV);
        displaybirthdayTV = (TextView) view.findViewById(R.id.displaybirthdayTV);

        if(ejunkUser == null){
            Toast.makeText(getActivity(), "Something went wrong! Data is not available at the moment", Toast.LENGTH_LONG).show();
        }else{
            progressDialog.setVisibility(View.VISIBLE);
            showUserProfile();
        }

        return view;
    }

    private void showUserProfile() {
        String UserID = ejunkUser.getUid();

        //extracting data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
        reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                helperClass = snapshot.getValue(UserHelperClass.class);
                if(helperClass != null){
                    String username = helperClass.getUsername();
                    String fullname = helperClass.getName();
                    String contact = helperClass.getContact();
                    String email = ejunkUser.getEmail();
                    String barangay = helperClass.getBarangay();
                    String zone = helperClass.getZone();
                    String address = zone + " " + barangay + " Manaoag Pangasinan";
                    String birthday = helperClass.getBirthday();

                    displaynameTV.setText(username);
                    displayfullnameTV.setText(fullname);
                    displaycontactTV.setText(contact);
                    displayemailTV.setText(email);
                    displayaddressTV.setText(address);
                    displaybirthdayTV.setText(birthday);

                    Uri uri = ejunkUser.getPhotoUrl();
                    Picasso.get().load(uri).into(displayProfileIV);
                }
                progressDialog.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                progressDialog.setVisibility(View.GONE);
            }
        });
    }

}