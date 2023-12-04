package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.MatcherModel;
import com.example.needlove.Model.newFriend;
import com.example.needlove.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatcherAdapter extends RecyclerView.Adapter<MatcherAdapter.MatcherHolder> {

    Context mContext;
    List<MatcherModel> matcherList;
    private OnItemClickListener mListener;


    public MatcherAdapter(Context mContext, List<MatcherModel> matcherlist) {
        this.mContext = mContext;
        this.matcherList = matcherlist;

    }

    @Override
    public MatcherHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_new_friend, parent, false);

        return new MatcherHolder(v);
    }

    @Override
    public void onBindViewHolder(MatcherHolder holder, int position) {
        MatcherModel Curretmatch = matcherList.get(position);


        Picasso.get().load(Curretmatch.getPic()).placeholder(R.drawable.avatar_prf).into(holder.profile_user);

        }





    @Override
    public int getItemCount() {
        return matcherList.size();
    }

    public class MatcherHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView profile_user;


        public MatcherHolder(View itemView) {
            super(itemView);

            profile_user = itemView.findViewById(R.id.profile_user);



            profile_user.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClickMatch(position,profile_user);
                }
            }

        }


    }


    public interface OnItemClickListener {

        void onItemClickMatch(int position, CircleImageView img);


    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}

