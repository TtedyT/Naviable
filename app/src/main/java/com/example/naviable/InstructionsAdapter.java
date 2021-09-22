package com.example.naviable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naviable.navigation.Direction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.MyViewHolder> {

    private Context context;
    private List<Direction> instructions;

    public InstructionsAdapter(Context context, List<Direction> instructions){
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
        Direction instruction = instructions.get(position);
        holder.instructionTextView.setText(instruction.getDescription());
        /**
         * base on the python file in assets:
         *
         * 'STRAIGHT',
         * 'RIGHT',
         * 'LEFT',
         * 'ELEVATOR'
         */
        int imageFromType = NaviableApplication.getInstance().getDB().getImagePathFromMap(instruction.getType());
        // todo: make images same size
        //       make all images with gray background (some of them are black)
        holder.instructionImageView.setImageResource(imageFromType);
//        switch (instruction.getType()){
//            case "STRAIGHT":
//                holder.instructionImageView.setImageResource();
//                break;
//            case "RIGHT":
//                holder.instructionImageView.setImageResource(R.drawable.ic_turn_right);
//                break;
//            case "LEFT":
//                holder.instructionImageView.setImageResource(R.drawable.ic_turn_left);
//                break;
//            case "ELEVATOR":
//                holder.instructionImageView.setImageResource(R.drawable.ic_baseline_elevator_24);
//                break;
//        }
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView instructionTextView;
        ImageView instructionImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            instructionTextView = itemView.findViewById(R.id.myInstructionTextView);
            instructionImageView = itemView.findViewById(R.id.instructionImage);
        }
    }
}
