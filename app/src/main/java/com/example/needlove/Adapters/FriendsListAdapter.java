package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.Friends;
import com.example.needlove.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.UserHolder> {

    Context mContext;
    List<Friends> friendList;

    private OnItemClickListener mListenerClick;
    private OnItemClickListenerMor mListenerClickMore;


    public FriendsListAdapter(Context mContext, List<Friends> friendlist) {
        this.mContext = mContext;
        this.friendList = friendlist;
    }

    @Override
    public void onBindViewHolder(FriendsListAdapter.UserHolder holder, int position) {

        Friends Curretuser = friendList.get(position);

        holder.username.setText(Curretuser.getUsername());

        Picasso.get().load(Curretuser.getPic())
                .placeholder(R.drawable.avatar_prf)
                .into(holder.my_profile_pic);


    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        return new UserHolder(view);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView my_profile_pic;
        TextView username;
        ImageView btnMore;

        LinearLayout linerClickUser;


        public UserHolder(@NonNull View itemView) {
            super(itemView);

            my_profile_pic = itemView.findViewById(R.id.my_profile_pic);
            username = itemView.findViewById(R.id.username);
            linerClickUser = itemView.findViewById(R.id.linerClickUser);
            btnMore = itemView.findViewById(R.id.btnMore);



            linerClickUser.setOnClickListener(this);
            btnMore.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();


            if (position != RecyclerView.NO_POSITION) {
                switch (view.getId()) {

                    case R.id.btnMore:

                        mListenerClickMore.onItemClickMore(position);

                        break;

                    case R.id.linerClickUser:
                        mListenerClick.onItemClick(position, my_profile_pic);

                        break;

                }
            }


        }
    }


    public interface OnItemClickListener {

        void onItemClick(int position, CircleImageView my_profile_pic);


    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListenerClick = listener;
    }




    public void setOnItemClickMore(OnItemClickListenerMor listener) {
        mListenerClickMore = listener;
    }



    public interface OnItemClickListenerMor {

        void onItemClickMore(int position);


    }
}


