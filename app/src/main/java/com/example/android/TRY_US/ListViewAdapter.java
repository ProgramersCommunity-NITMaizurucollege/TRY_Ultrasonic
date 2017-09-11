package com.example.android.TRY_US;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kanta on 9/11/17.
 */

public class ListViewAdapter extends ArrayAdapter<ListData> {
    private LayoutInflater layoutInflater;

    public ListViewAdapter(Context context, int resource, List<ListData> objects) {
        super(context, resource, objects);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListData data = (ListData) getItem(position);
        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.activity_sub, null);
        }

        TextView daydreamText;
        TextView trafficText;
        TextView stationsText;
        TextView otherText;
/*
        daydreamText = (TextView)convertView.findViewById(R.id.daydream);
        trafficText = (TextView)convertView.findViewById(R.id.name);
        stationsText = (TextView)convertView.findViewById(R.id.yomi);
        otherText = (TextView)convertView.findViewById(R.id.kentyo);

        daydreamText.setText(data.getDayDream());
        trafficText.setText(data.getTraffic());
        stationsText.setText(data.getStations());
        otherText.setText(data.getOthers());
*/
        return convertView;
    }
}
