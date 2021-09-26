package com.example.naviable;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naviable.navigation.Direction;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {

	private List<Direction> directionsList;
	private Context context;

	public BottomSheetFragment(List<Direction> directionList){
		this.directionsList = directionList;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(container == null){
			return null;
		}

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		View view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet, null);
		bottomSheetDialog.setContentView(view);
		RecyclerView recyclerView = view.findViewById(R.id.rcv_data);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		InstructionsAdapter adapter = new InstructionsAdapter(context, directionsList);
		RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);

		recyclerView.setLayoutManager(linearLayoutManager);
		recyclerView.setAdapter(adapter);
		recyclerView.addItemDecoration(itemDecoration);

		return bottomSheetDialog;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		this.context = context;
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}
