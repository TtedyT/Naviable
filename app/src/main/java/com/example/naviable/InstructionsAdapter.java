package com.example.naviable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<String> instructions;

    public InstructionsAdapter(Context context, ArrayList<String> instructions){
        this.context = context;
        this.instructions = instructions;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_one_instruction, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.instructionTextView.setText(instructions.get(position));
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView instructionTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            instructionTextView = itemView.findViewById(R.id.myInstructionTextView);
        }
    }
}
