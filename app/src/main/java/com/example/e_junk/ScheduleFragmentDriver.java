package com.example.e_junk;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleFragmentDriver#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragmentDriver extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ScheduleFragmentDriver() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScheduleFragmentDriver.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleFragmentDriver newInstance(String param1, String param2) {
        ScheduleFragmentDriver fragment = new ScheduleFragmentDriver();
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

    RecyclerView recyclerView;
    FloatingActionButton addSchedBTN;
    ArrayList<ScheduleHelperClass> scheduleHelperClassArrayList;
    ScheduleRecyclerAdapter adapter;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    ScheduleHelperClass scheduleHelperClass;

    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    ProgressBar progressDialog;
    UserHelperClass helperClass;

    int Hour;
    int Minute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_driver, container, false);
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();
        progressDialog = (ProgressBar) view.findViewById(R.id.progressDialog);

        scheduleHelperClassArrayList = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        addSchedBTN = (FloatingActionButton) view.findViewById(R.id.addSchedBTN);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        recyclerView.addItemDecoration(itemDecorator);

        addSchedBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSchedule();
            }
        });

        showSched();

        return view;
    }

    private void showSched() {
        String UserID = ejunkUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
        reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                helperClass = snapshot.getValue(UserHelperClass.class);
                if(helperClass != null) {
                    String barangay = helperClass.getBarangay();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("schedule");
                    reference.child(barangay).orderByChild("date").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            scheduleHelperClassArrayList.clear();
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                ScheduleHelperClass scheduleHelperClass = dataSnapshot.getValue(ScheduleHelperClass.class);
                                scheduleHelperClassArrayList.add(scheduleHelperClass);
                            }
                            adapter = new ScheduleRecyclerAdapter(getContext(), scheduleHelperClassArrayList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addSchedule() {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.input_sched, null);

        AutoCompleteTextView zoneEdit;
        TextInputEditText dateEdit, timeEdit;

        zoneEdit = (AutoCompleteTextView) view.findViewById(R.id.zoneEdit);
        dateEdit = (TextInputEditText) view.findViewById(R.id.dateEdit);
        timeEdit = (TextInputEditText) view.findViewById(R.id.timeEdit);

        String[] zoneItems = {"Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7"};
        ArrayAdapter<String> adapterItems;
        adapterItems = new ArrayAdapter<String>(getActivity(),R.layout.dropdown_layout,zoneItems);
        zoneEdit.setAdapter(adapterItems);

        //dateinputPicker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,onDateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = month+"/"+dayOfMonth+"/"+year;
                dateEdit.setText(date);
            }
        };

        //timepicker

        timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        Hour = i;
                        Minute = i1;

                        String time = Hour + ":" + Minute;

                        SimpleDateFormat f24hours = new SimpleDateFormat("HH:mm");
                        try{
                            Date date = f24hours.parse(time);
                            SimpleDateFormat f12hours = new SimpleDateFormat("hh:mm aa");
                            timeEdit.setText(f12hours.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },12, 0, false);
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.updateTime(Hour, Minute);
                timePickerDialog.show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Add Schedule")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String UserID = ejunkUser.getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
                        reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                helperClass = snapshot.getValue(UserHelperClass.class);
                                if(helperClass != null){
                                    String id = "sched" + new Date().getTime();
                                    String barangay = helperClass.getBarangay();
                                    String zone = zoneEdit.getText().toString().trim();
                                    String date = dateEdit.getText().toString().trim();
                                    String time = timeEdit.getText().toString().trim();
                                    if(zone.isEmpty() || date.isEmpty() || time.isEmpty()){
                                        Toast.makeText(getActivity(), "Please fill all the fields to add", Toast.LENGTH_LONG).show();
                                    }else{
                                        progressDialog.setVisibility(View.VISIBLE);
                                        addToDB(id, barangay, zone, time, date);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.create().show();

    }

    private void addToDB(String id, String barangay, String zone, String time, String date) {

        scheduleHelperClass = new ScheduleHelperClass(id, barangay, zone, time, date);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("schedule");
        reference.child(barangay).child(id).setValue(scheduleHelperClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "New schedule added", Toast.LENGTH_SHORT).show();
                    progressDialog.setVisibility(View.GONE);
                }else{
                    progressDialog.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Failed to add schedule", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}