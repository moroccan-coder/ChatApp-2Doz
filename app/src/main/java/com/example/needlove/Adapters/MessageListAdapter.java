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
import com.example.needlove.Model.MessagesList;
import com.example.needlove.R;
import com.example.needlove.TimeLikeWhat;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.messagesHolder> {

    Context mContext;
    List<MessagesList> MessagList;

    private OnItemClickListener mListenerClick;

    public MessageListAdapter(Context mContext, List<MessagesList> msglist) {
        this.mContext = mContext;
        this.MessagList = msglist;
    }

    @Override
    public void onBindViewHolder(messagesHolder holder, int position) {

        MessagesList CurrentMsg = MessagList.get(position);

        holder.txtMessage.setText(CurrentMsg.getLastmsg());
        holder.username.setText(CurrentMsg.getUsername());

        Picasso.get().load(CurrentMsg.getPic())
                .placeholder(R.drawable.avatar_prf)
                .into(holder.my_profile_pic);

        if (CurrentMsg.getMsgUnread() > 0) {
            holder.txtUnreadMsg.setVisibility(View.VISIBLE);
            holder.txtUnreadMsg.setText(String.valueOf(CurrentMsg.getMsgUnread()));
        } else {
            holder.txtUnreadMsg.setVisibility(View.GONE);
        }

        if (CurrentMsg.getTime() !=null)
        {
            long time = CurrentMsg.getTime().toDate().getTime();

            holder.txtmsgDate.setText(TimeLikeWhat.getSmsTodayYestFromMilli(time));
        }
        else {
            holder.txtmsgDate.setText("just now");
        }



    }

    @Override
    public int getItemCount() {
        return MessagList.size();
    }

    @NonNull
    @Override
    public messagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list, parent, false);

        return new messagesHolder(view);
    }

    class messagesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView my_profile_pic;
        TextView username, txtMessage, txtmsgDate, txtUnreadMsg;


        public messagesHolder(@NonNull View itemView) {
            super(itemView);

            my_profile_pic = itemView.findViewById(R.id.my_profile_pic);
            username = itemView.findViewById(R.id.username);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtmsgDate = itemView.findViewById(R.id.txtmsgDate);
            txtUnreadMsg = itemView.findViewById(R.id.txtUnreadMsg);


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


