package com.example.voice;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListSocialAdapter extends ArrayAdapter<String> {

    private Activity context = null;
    ArrayList<String> timeItems=new ArrayList<String>();
    ArrayList<String> placeItems=new ArrayList<String>();
    ArrayList<String> eventItems=new ArrayList<String>();

    public ListSocialAdapter(Activity context, ArrayList<String> placeItems, ArrayList<String> eventItems,ArrayList<String> timestampItems) {
        super(context, R.layout.row_view, timestampItems);
        this.context = context;
        this.placeItems = placeItems;
        this.timeItems = timestampItems;
        this.eventItems = eventItems;

    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.row_social, null, true);
        TextView time = (TextView)  rowView.findViewById(R.id.Time);
        TextView place = (TextView) rowView.findViewById(R.id.Place);
        TextView event = (TextView) rowView.findViewById(R.id.Event);
        time.setText("Time:"+"\n"+(timeItems.get(position)));
        place.setText("Place:"+"\n"+(placeItems.get(position)));
        event.setText("Event:"+"\n"+eventItems.get(position));

        return rowView;
    }
}
