package com.example.voice;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListFoodAdapter extends ArrayAdapter<String> {

        private Activity context=null;
        ArrayList<String> foodItems=new ArrayList<String>();
        ArrayList<Integer> calorieItems=new ArrayList<Integer>();

public ListFoodAdapter(Activity context,ArrayList<Integer> calorieItems,ArrayList<String> foodItems){
        super(context,R.layout.row_food,foodItems);
        this.context=context;
        this.foodItems=foodItems;
        this.calorieItems=calorieItems;
        }


@Override
public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.row_food,null,true);
        TextView food=(TextView)rowView.findViewById(R.id.FoodItem);
        TextView calorie =(TextView)rowView.findViewById(R.id.Calories);
        food.setText("Food:"+"\n"+(foodItems.get(position)));
        calorie.setText("Calories:"+"\n"+(calorieItems.get(position)));

        return rowView;
        }
}