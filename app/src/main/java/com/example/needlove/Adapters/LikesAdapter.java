package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.FriendsRequest;
import com.example.needlove.Model.LikesModel;
import com.example.needlove.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.UserHolder> {

    Context mContext;
    List<LikesModel> likesList;

    private OnItemClickListener mListenerClick;
    private OnItemClickListenerAccept mListenerClickAccept;
    private OnItemClickListenerCancel mListenerClickCancel;


    public LikesAdapter(Context mContext, List<LikesModel> likeslist) {
        this.mContext = mContext;
        this.likesList = likeslist;
    }

    @Override
    public void onBindViewHolder(LikesAdapter.UserHolder holder, int position) {

        LikesModel Curretuser = likesList.get(position);

        holder.txtUseRequestName.setText(Curretuser.getUsername());

        Picasso.get().load(Curretuser.getPic())
                .placeholder(R.drawable.avatar_prf)
                .into(holder.my_profile_pic);


    }

    @Override
    public int getItemCount() {
        return likesList.size();
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_likes, parent, false);

        return new UserHolder(view);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView my_profile_pic;
        TextView txtUseRequestName;
        FloatingActionButton right;
        FloatingActionButton left;

        LinearLayout linerClickUser;


        public UserHolder(@NonNull View itemView) {
            super(itemView);

            my_profile_pic = itemView.findViewById(R.id.my_profile_pic);
            txtUseRequestName = itemView.findViewById(R.id.txtUseRequestName);
            linerClickUser = itemView.findViewById(R.id.linerClickUser);
            right = itemView.findViewById(R.id.right);
            left = itemView.findViewById(R.id.left);


            linerClickUser.setOnClickListener(this);
            right.setOnClickListener(this);
            left.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();


            if (position != RecyclerView.NO_POSITION) {
                switch (view.getId()) {
                    case R.id.right:
                        mListenerClickAccept.onItemClickAccept(position);

                        break;
                    case R.id.left:

                        mListenerClickCancel.onItemClickCancel(position);

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


    public void setOnItemClickAccept(OnItemClickListenerAccept listener) {
        mListenerClickAccept = listener;
    }


    public void setOnItemClickCancel(OnItemClickListenerCancel listener) {
        mListenerClickCancel = listener;
    }


    public interface OnItemClickListenerAccept {

        void onItemClickAccept(int position);


    }

    public interface OnItemClickListenerCancel {

        void onItemClickCancel(int position);


    }
}


