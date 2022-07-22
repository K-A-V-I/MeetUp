package com.codewithbee.meetup.Matches;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codewithbee.meetup.R;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesViewHolder> {

    private List<MatchesObject> matcheslist;
    private Context context;

    public MatchesAdapter(List<MatchesObject> matcheslist, Context context) {
        this.matcheslist =matcheslist;
        this.context = context;
    }
    @NonNull
    @Override
    public MatchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches,null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        MatchesViewHolder rcv = new MatchesViewHolder(layoutView);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolder holder, int position) {
        holder.mMatchId.setText(matcheslist.get(position).getUserId());
        holder.mBudget.setText(matcheslist.get(position).getBudget());
        holder.mGive.setText(matcheslist.get(position).getGive());
        holder.mProfile.setText(matcheslist.get(position).getProfileImageUrl());
        holder.mNeed.setText(matcheslist.get(position).getNeed());
        holder.mMatchName.setText(matcheslist.get(position).getName());
        holder.mLastMessage.setText(matcheslist.get(position).getLastMessage());
        String lastSeen = "";
        lastSeen = matcheslist.get(position).getLastSeen();

        if(lastSeen.equals("true"))
            holder.mNotificationDot.setVisibility(View.VISIBLE);
        else
            holder.mNotificationDot.setVisibility(View.INVISIBLE);
        holder.mLAstTimeStamp.setText(matcheslist.get(position).getLastTimeStamp());
        if (!matcheslist.get(position).getProfileImageUrl().equals("default")) {
            Glide.with(context).load(matcheslist.get(position).getProfileImageUrl()).into(holder.mMatchImage);
        }
    }

    @Override
    public int getItemCount() {
        return this.matcheslist.size();
    }
}
