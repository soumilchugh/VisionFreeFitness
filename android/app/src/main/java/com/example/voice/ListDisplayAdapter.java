package com.example.voice;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListDisplayAdapter extends ArrayAdapter<String> {

    private Activity context = null;
    ArrayList<String> timestampItems=new ArrayList<String>();
    ArrayList<Integer> calorieItems=new ArrayList<Integer>();
    ArrayList<Integer> stepItems=new ArrayList<Integer>();

    public ListDisplayAdapter(Activity context, ArrayList<Integer> stepItems, ArrayList<Integer> calorieItems,ArrayList<String> timestampItems) {
        super(context, R.layout.row_view, timestampItems);
        this.context = context;
        this.stepItems = stepItems;
        this.calorieItems = calorieItems;
        this.timestampItems = timestampItems;

    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.row_view, null, true);
        TextView timestamp = (TextView)  rowView.findViewById(R.id.Timestamp);
        TextView steps = (TextView) rowView.findViewById(R.id.Steps);
        TextView calories = (TextView) rowView.findViewById(R.id.Calories);
        timestamp.setText("Time:"+String.valueOf(timestampItems.get(position)));
        steps.setText("Steps:"+String.valueOf(stepItems.get(position)));
        calories.setText("Calories:"+calorieItems.get(position));

        return rowView;
    }
}
