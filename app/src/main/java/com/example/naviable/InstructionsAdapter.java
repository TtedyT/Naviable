package com.example.naviable;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naviable.navigation.Direction;

import java.util.List;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.MyViewHolder> {

	private Context context;
	private List<Direction> instructions;
	private TextToSpeech textToSpeech;

	public InstructionsAdapter(Context context, List<Direction> instructions, TextToSpeech textToSpeech) {
		this.context = context;
		this.instructions = instructions;
		this.textToSpeech = textToSpeech;
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
		 * 'RAMP',
		 * 'ELEVATOR'
		 */
		int imageFromType = NaviableApplication.getInstance().getDB().getImagePathFromMap(instruction.getType());
		holder.instructionImageView.setImageResource(imageFromType);

		holder.instructionTextToSpeech.setOnClickListener(view -> {
			String textToReadOutLoud = holder.instructionTextView.getText().toString();

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				textToSpeech.speak(textToReadOutLoud,TextToSpeech.QUEUE_FLUSH,null,null);
			} else {
				textToSpeech.speak(textToReadOutLoud, TextToSpeech.QUEUE_FLUSH, null);
			}
		});
	}

	@Override
	public int getItemCount() {
		return instructions.size();
	}

	public void setInstructions(List<Direction> instructions) {
		this.instructions = instructions;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {

		TextView instructionTextView;
		ImageView instructionImageView;
		ImageView instructionTextToSpeech;

		public MyViewHolder(@NonNull View itemView) {
			super(itemView);
			instructionTextView = itemView.findViewById(R.id.myInstructionTextView);
			instructionImageView = itemView.findViewById(R.id.instructionImage);
			instructionTextToSpeech = itemView.findViewById(R.id.instruction_text_to_speech);
		}
	}
}
