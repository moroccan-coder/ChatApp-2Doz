package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.FriendsRequest;
import com.example.needlove.Model.newFriend;
import com.example.needlove.R;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendReqAdapter extends RecyclerView.Adapter<FriendReqAdapter.UserHolder> {

    Context mContext;
    List<FriendsRequest> friendList;

    private OnItemClickListener mListenerClick;
    private OnItemClickListenerAccept mListenerClickAccept;
    private OnItemClickListenerCancel mListenerClickCancel;


    public FriendReqAdapter(Context mContext, List<FriendsRequest> friendlist) {
        this.mContext = mContext;
        this.friendList = friendlist;
    }

    @Override
    public void onBindViewHolder(FriendReqAdapter.UserHolder holder, int position) {

        FriendsRequest Curretuser = friendList.get(position);

        holder.txtUseRequestName.setText(Curretuser.getUsername());

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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);

        return new UserHolder(view);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView my_profile_pic;
        TextView txtUseRequestName;
        Button btnAccept;
        ImageView btnCancel;

        LinearLayout linerClickUser;


        public UserHolder(@NonNull View itemView) {
            super(itemView);

            my_profile_pic = itemView.findViewById(R.id.my_profile_pic);
            txtUseRequestName = itemView.findViewById(R.id.txtUseRequestName);
            linerClickUser = itemView.findViewById(R.id.linerClickUser);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCancel = itemView.findViewById(R.id.btnCancel);


            linerClickUser.setOnClickListener(this);
            btnAccept.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();


            if (position != RecyclerView.NO_POSITION) {
                switch (view.getId()) {
                    case R.id.btnAccept:
                        mListenerClickAccept.onItemClickAccept(position);

                        break;
                    case R.id.btnCancel:

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


