package com.example.naviable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<String> searchSuggestions;

    public MyAdapter(Context context, ArrayList<String> searchSuggestions){
        this.context = context;
        this.searchSuggestions = searchSuggestions;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_one_result, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.searchSuggestionTextView.setText(searchSuggestions.get(position));
    }

    @Override
    public int getItemCount() {
        return searchSuggestions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView searchSuggestionTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            searchSuggestionTextView = itemView.findViewById(R.id.mySearchSuggestionTextView);
        }
    }

}
