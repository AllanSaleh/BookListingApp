package com.example.allan.booklistingapp;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BookAdapter extends ArrayAdapter {
    private Context context;
    private int resource;
    private ArrayList<Books> booksArrayList;

    public BookAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Books> booksArrayList) {
        super(context, resource, booksArrayList);
        this.context = context;
        this.resource = resource;
        this.booksArrayList = booksArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(resource, null);
        }
        TextView title = convertView.findViewById(R.id.title);
        TextView authors = convertView.findViewById(R.id.authors);
        Books book = booksArrayList.get(position);
        title.setText(book.getTitle());
        authors.setText(String.valueOf(book.getAuthors()));
        return convertView;
    }
}
