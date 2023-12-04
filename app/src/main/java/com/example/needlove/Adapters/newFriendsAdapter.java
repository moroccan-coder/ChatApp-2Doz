package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.newFriend;
import com.example.needlove.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class newFriendsAdapter extends RecyclerView.Adapter<newFriendsAdapter.friendCiewHolder> {

    Context mContext;
    List<newFriend> friendList;
    private OnItemClickListener mListener;


    public newFriendsAdapter(Context mContext, List<newFriend> friendlist) {
        this.mContext = mContext;
        this.friendList = friendlist;

    }

    @Override
    public friendCiewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_new_friend, parent, false);

        return new friendCiewHolder(v);
    }

    @Override
    public void onBindViewHolder(friendCiewHolder holder, int position) {
        newFriend Curretuser = friendList.get(position);


        Picasso.get().load(Curretuser.getPic()).placeholder(R.drawable.avatar_prf).into(holder.profile_user);

        }





    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class friendCiewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView profile_user;


        public friendCiewHolder(View itemView) {
            super(itemView);

            profile_user = itemView.findViewById(R.id.profile_user);



            profile_user.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClickNew(position,profile_user);
                }
            }

        }


    }


    public interface OnItemClickListener {

        void onItemClickNew(int position, CircleImageView img);


    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}

