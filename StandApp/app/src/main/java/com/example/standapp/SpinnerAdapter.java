package com.example.standapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter {
    private List<Integer> image;
    private List<String> text;

    public SpinnerAdapter(@NonNull Context context, int resource, List<Integer> image, List<String> text) {
        super(context, resource);
        this.image = image;
        this.text = text;
    }

    public List<Integer> getImage() {
        return image;
    }

    public void setImage(List<Integer> image) {
        this.image = image;
    }

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = text;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_item, parent, false);
        }


        //Set Custom View
        TextView tv = (TextView)convertView.findViewById(R.id.textView_spinnerItem);
        ImageView img = (ImageView) convertView.findViewById(R.id.imageView);

        tv.setText(text.get(position));
        img.setImageResource(image.get(position));

        return convertView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return getCustomView(position, convertView, parent);
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return getCustomView(position, convertView, parent);
        return super.getDropDownView(position, convertView, parent);
    }
}





