package com.example.needlove.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.UsersDiscover;
import com.example.needlove.R;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.squareup.picasso.Picasso;

public class UserAdapter extends FirestorePagingAdapter<UsersDiscover, UserAdapter.UserHolder> {

    private OnadaptrChenged mListener;
    private OnItemClickListener mListenerClick;
    private String UserID;

    public UserAdapter(@NonNull FirestorePagingOptions<UsersDiscover> options, String userID) {
        super(options);
        this.UserID = userID;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserHolder holder, int i, @NonNull UsersDiscover model) {


        holder.userName.setText(model.getUsername());
        holder.userCountry.setText(model.getCountry());
        holder.userAge.setText(String.valueOf(model.getAge()));

        Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.avatar_prf).into(holder.userImag);


        // holder.itemView.setVisibility(View.GONE);
        //  ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        // params.height = 0;
        //holder.itemView.setLayoutParams(params);

        if (model.isOnline()) {
            holder.staus_online.setVisibility(View.VISIBLE);
        } else {
            holder.staus_online.setVisibility(View.GONE);
        }


    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover, parent, false);
        int width = parent.getMeasuredWidth();
        int height = parent.getMeasuredHeight() / 3;//(Width/Height)
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        params.height = Math.round(height);
        view.setLayoutParams(params);

        return new UserHolder(view);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView userImag;
        private ImageView staus_online;
        private TextView userName;
        private TextView userAge;
        private TextView userCountry;


        public UserHolder(@NonNull View itemView) {
            super(itemView);

            userImag = itemView.findViewById(R.id.discover_user_img);
            staus_online = itemView.findViewById(R.id.staus_online);
            userName = itemView.findViewById(R.id.item_discover_username);
            userAge = itemView.findViewById(R.id.discover_user_age);
            userCountry = itemView.findViewById(R.id.item_discover_away);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListenerClick.onItemClick(position, userImag);
                }
            }
        }
    }


    public interface OnadaptrChenged {

        void onStateChengged(LoadingState state);
    }


    public void setOnadapterStateChange(OnadaptrChenged llistener) {
        mListener = llistener;
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        super.onLoadingStateChanged(state);

        mListener.onStateChengged(state);

    }


    public interface OnItemClickListener {

        void onItemClick(int position, ImageView imageView);


    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListenerClick = listener;
    }
}

