package com.example.harbon.test;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harbon on 15-6-16.
 */
public class CloudTaskAdapter extends BaseAdapter{
    private Context mContext;
    private List<String> Data;
    public CloudTaskAdapter(Context context) {
        mContext = context;
        Data = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            Data.add("aa");
        }
    }
    @Override
    public int getCount() {
        return Data.size();
    }

    @Override
    public Object getItem(int position) {
        return Data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public void addItem(String a) {
        Data.add(a);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
        view = LayoutInflater.from(mContext).inflate(R.layout.item_view, null);
        }else {
            view = convertView;
        }
        TextView textView  = (TextView) view.findViewById(R.id.test_view);
        textView.setText(String.valueOf(position));
        return view;
    }
}
