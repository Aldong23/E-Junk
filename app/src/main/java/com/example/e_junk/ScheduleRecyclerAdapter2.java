package com.example.e_junk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScheduleRecyclerAdapter2 extends RecyclerView.Adapter<ScheduleRecyclerAdapter2.ViewHolder> {

    Context context;
    ArrayList<ScheduleHelperClass> scheduleHelperClassArrayList;

    public ScheduleRecyclerAdapter2(Context context, ArrayList<ScheduleHelperClass> scheduleHelperClassArrayList) {
        this.context = context;
        this.scheduleHelperClassArrayList = scheduleHelperClassArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.schedule_item_normal, parent, false);
        return new ScheduleRecyclerAdapter2.ViewHolder(view);
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
