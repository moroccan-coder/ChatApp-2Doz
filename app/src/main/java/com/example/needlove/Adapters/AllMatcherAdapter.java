package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.MatcherModel;
import com.example.needlove.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllMatcherAdapter extends RecyclerView.Adapter<AllMatcherAdapter.UserHolder> {

    Context mContext;
    List<MatcherModel> matcherList;

    private OnItemClickListener mListenerClick;

    public AllMatcherAdapter(Context mContext, List<MatcherModel> likeslist) {
        this.mContext = mContext;
        this.matcherList = likeslist;
    }

    @Override
    public void onBindViewHolder(AllMatcherAdapter.UserHolder holder, int position) {

        MatcherModel Curretuser = matcherList.get(position);

        holder.txtUseRequestName.setText(Curretuser.getUsername());

        Picasso.get().load(Curretuser.getPic())
                .placeholder(R.drawable.avatar_prf)
                .into(holder.my_profile_pic);


    }

    @Override
    public int getItemCount() {
        return matcherList.size();
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_allmatcher, parent, false);

        return new UserHolder(view);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView my_profile_pic;
        TextView txtUseRequestName;


        LinearLayout linerClickUser;


        public UserHolder(@NonNull View itemView) {
            super(itemView);

            my_profile_pic = itemView.findViewById(R.id.my_profile_pic);
            txtUseRequestName = itemView.findViewById(R.id.txtUseRequestName);
            linerClickUser = itemView.findViewById(R.id.linerClickUser);
            linerClickUser.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                mListenerClick.onItemClick(position, my_profile_pic);
            }


        }
    }


    public interface OnItemClickListener {

        void onItemClick(int position, CircleImageView my_profile_pic);


    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListenerClick = listener;
    }


}


