package com.example.e_junk;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ViewHolder> {

    Context context;
    ArrayList<ScheduleHelperClass> scheduleHelperClassArrayList;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    int Hour;
    int Minute;

    public ScheduleRecyclerAdapter(Context context, ArrayList<ScheduleHelperClass> scheduleHelperClassArrayList) {
        this.context = context;
        this.scheduleHelperClassArrayList = scheduleHelperClassArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.schedule_item_driver, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ScheduleHelperClass scheduleHelperClass = scheduleHelperClassArrayList.get(position);
        String id = scheduleHelperClass.getId();
        String barangay = scheduleHelperClass.getBarangay();
        String zone = scheduleHelperClass.getZone();
        String time = scheduleHelperClass.getTime();
        String date = scheduleHelperClass.getDate();
        String address = zone + " " + barangay + " Manaoag";

        holder.addressTV.setText(address);
        holder.dateTV.setText(date);
        holder.timeTV.setText(time);

        holder.editBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editSched(id, barangay, zone, time, date);
            }
        });

        holder.doneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSched(id, barangay);
            }
        });

    }

    private void removeSched(String id, String barangay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Mark as done?")
                .setMessage("Clicking ok will delete this schedule permanently!.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("schedule");
                        reference.child(barangay).child(id).removeValue();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.create().show();
    }

    private void editSched(String id, String barangay, String zone, String time, String date) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.input_sched, null);

        AutoCompleteTextView zoneEdit;
        TextInputEditText dateEdit, timeEdit;
        zoneEdit = (AutoCompleteTextView) view.findViewById(R.id.zoneEdit);
        dateEdit = (TextInputEditText) view.findViewById(R.id.dateEdit);
        timeEdit = (TextInputEditText) view.findViewById(R.id.timeEdit);

        //old value
        zoneEdit.setText(zone);
        dateEdit.setText(date);
        timeEdit.setText(time);

        String[] zoneItems = {"Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7"};
        ArrayAdapter<String> adapterItems;
        adapterItems = new ArrayAdapter<String>(context,R.layout.dropdown_layout,zoneItems);
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
                        context, android.R.style.Theme_Holo_Light_Dialog_MinWidth,onDateSetListener, year, month, day);
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view)
                .setTitle("Edit Schedule")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String zone = zoneEdit.getText().toString().trim();
                        String date = dateEdit.getText().toString().trim();
                        String time = timeEdit.getText().toString().trim();
                        if(zone.isEmpty() || date.isEmpty() || time.isEmpty()){
                            Toast.makeText(context, "Please fill all field to add", Toast.LENGTH_LONG).show();
                        }else{
                            editToDB(id, barangay, zone, time, date);
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.create().show();
    }

    private void editToDB(String id, String barangay, String zone, String time, String date) {
        ScheduleHelperClass scheduleHelperClass = new ScheduleHelperClass(id, barangay, zone, time, date);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("schedule");
        reference.child(barangay).child(id).setValue(scheduleHelperClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, zone + " Edited Successfuly", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Failed to edit schedule", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleHelperClassArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView addressTV, dateTV, timeTV;
        Button doneBTN, editBTN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            addressTV = itemView.findViewById(R.id.addressTV);
            dateTV = itemView.findViewById(R.id.dateTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            doneBTN = itemView.findViewById(R.id.doneBTN);
            editBTN = itemView.findViewById(R.id.editBTN);

        }
    }
}
