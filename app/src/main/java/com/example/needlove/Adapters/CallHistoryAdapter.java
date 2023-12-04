package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.CallHistoryModel;
import com.example.needlove.R;
import com.example.needlove.TimeLikeWhat;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.CallHolder> {

    Context mContext;
    List<CallHistoryModel> CallList;

    private OnItemClickListener mListenerClick;

    public CallHistoryAdapter(Context mContext, List<CallHistoryModel> msglist) {
        this.mContext = mContext;
        this.CallList = msglist;
    }

    @Override
    public void onBindViewHolder(CallHolder holder, int position) {

        CallHistoryModel CurrentCall = CallList.get(position);

        holder.call_username.setText(CurrentCall.getUsername());
        holder.call_country.setText(CurrentCall.getCountry());

        Picasso.get().load(CurrentCall.getPic())
                .placeholder(R.drawable.avatar_prf)
                .into(holder.my_profile_pic);


        String[] timeCall = CurrentCall.getCallingtime().split(":");

        holder.call_time.setText(timeCall[0]+" mins "+ timeCall[1]+" sec");


        if (CurrentCall.getTime() != null) {
            long time = CurrentCall.getTime().toDate().getTime();

            holder.call_date.setText(TimeLikeWhat.getSmsTodayYestFromMilli(time));
        } else {
            holder.call_date.setText("just now");
        }


    }

    @Override
    public int getItemCount() {
        return CallList.size();
    }

    @NonNull
    @Override
    public CallHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.call_hitory_item, parent, false);

        return new CallHolder(view);
    }

    class CallHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView my_profile_pic;
        TextView call_username, call_country, call_date, call_time;


        public CallHolder(@NonNull View itemView) {
            super(itemView);

            my_profile_pic = itemView.findViewById(R.id.my_profile_pic);
            call_username = itemView.findViewById(R.id.call_username);
            call_country = itemView.findViewById(R.id.call_country);
            call_date = itemView.findViewById(R.id.call_date);
            call_time = itemView.findViewById(R.id.call_time);


            itemView.setOnClickListener(this);

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


