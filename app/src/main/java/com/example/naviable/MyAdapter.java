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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private ArrayList<String> searchSuggestions;
    private ArrayList<String> searchSuggestionsAll;
    private ArrayList<String> searchSuggestionsRecents;

    private final RecyclerViewClickListener clickListener;

    public MyAdapter(Context context, ArrayList<String> searchSuggestionsNotRecents, ArrayList<String> recentSearchedLocations, RecyclerViewClickListener clickListener) {
        this.context = context;
        this.searchSuggestionsRecents = new ArrayList<>(recentSearchedLocations);
        this.searchSuggestions = new ArrayList<>(recentSearchedLocations);
        ArrayList<String> notRecentCopy = new ArrayList<>(searchSuggestionsNotRecents);
        this.searchSuggestions.addAll(notRecentCopy);
        this.searchSuggestionsAll = new ArrayList<>(this.searchSuggestions);
        // makeRecentSearchesFirst(searchSuggestions);
        this.clickListener = clickListener;
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
        String location = this.searchSuggestions.get(position);
        if (this.searchSuggestionsRecents.contains(location)) {
            holder.searchSuggestionImageView.setImageResource(R.drawable.recent_searched);
        } else {
            holder.searchSuggestionImageView.setImageResource(R.drawable.not_recent_search);
        }
        holder.searchSuggestionTextView.setText(location);
    }

    @Override
    public int getItemCount() {
        return this.searchSuggestions.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            String charSequenceStringLowerCased = charSequence.toString().toLowerCase();
            ArrayList<String> filteredSearchSuggestions = new ArrayList<>();
            if (charSequence.toString().isEmpty()) {
                filteredSearchSuggestions.addAll(searchSuggestionsAll);
            } else {
                for (String resultSuggestion : searchSuggestions) {
                    if (resultSuggestion.toLowerCase().contains(charSequenceStringLowerCased))
                        filteredSearchSuggestions.add(resultSuggestion);
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredSearchSuggestions;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            searchSuggestions.clear();
            searchSuggestions.addAll((Collection<? extends String>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView searchSuggestionTextView;
        ImageView searchSuggestionImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            searchSuggestionTextView = itemView.findViewById(R.id.mySearchSuggestionTextView);
            searchSuggestionImageView = itemView.findViewById(R.id.mySearchSuggestionImageView);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener {
        void onClick(View v, int position);
    }

}
