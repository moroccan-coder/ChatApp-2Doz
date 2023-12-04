package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.MessageChat;
import com.example.needlove.R;
import com.example.needlove.TimeSince;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MsgViewHolder> {

    Context mContext;
    List<MessageChat> msgList;

    private OnItemClickListener mListener;
    private OnImgClickListener mLisener2;
    int lang = 0;

    String CurrentUserID;
    CollectionReference mCollectionRef = FirebaseFirestore.getInstance().collection("Chat");
    String UserId;

    public MessagesAdapter(Context mContext, List<MessageChat> msgList,String currentUser,String usrid) {
        this.mContext = mContext;
        this.msgList = msgList;
        this.CurrentUserID = currentUser;
        this.UserId = usrid;

    }

    @Override
    public MsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.item_message, parent, false);

        return new MsgViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MsgViewHolder holder, int position) {
        MessageChat CurrentMessage = msgList.get(position);



            if(CurrentMessage.getFrom().equals(CurrentUserID))
            {
                holder.lnr_Other.setVisibility(View.GONE);
                holder.lnr_Current.setVisibility(View.VISIBLE);


                if (CurrentMessage.getType().equals("text"))
                {
                    holder.txtUserMsg.setVisibility(View.VISIBLE);
                    holder.imgUserMsg.setVisibility(View.GONE);
                    holder.txtUserMsg.setText(CurrentMessage.getMessage());

                }
                else {
                    holder.imgUserMsg.setVisibility(View.VISIBLE);
                    holder.txtUserMsg.setVisibility(View.GONE);
                    Picasso.get().load(CurrentMessage.getPic()).into(holder.imgUserMsg);
                }

                if(CurrentMessage.getTime() != null)
                {
                    long time = CurrentMessage.getTime().toDate().getTime();
                    holder.txtUserTime.setText(TimeSince.getTimeAgo(time));
                }
                else {
                    holder.txtUserTime.setText("just now");
                }



                if(CurrentMessage.isSeen())
                {
                    holder.user_msg_visible.setVisibility(View.VISIBLE);
                }
                else {
                    holder.user_msg_visible.setVisibility(View.GONE);
                }

            }else {
                holder.lnr_Other.setVisibility(View.VISIBLE);
                holder.lnr_Current.setVisibility(View.GONE);
                if (CurrentMessage.getType().equals("text"))
                {
                    holder.txtAdminMsg.setVisibility(View.VISIBLE);
                    holder.imgAdminMsg.setVisibility(View.GONE);
                    holder.txtAdminMsg.setText(CurrentMessage.getMessage());

                }
                else {
                    holder.imgAdminMsg.setVisibility(View.VISIBLE);
                    holder.txtAdminMsg.setVisibility(View.GONE);
                    Picasso.get().load(CurrentMessage.getPic()).into(holder.imgAdminMsg);
                }
                if(CurrentMessage.getTime() != null)
                {
                    long time = CurrentMessage.getTime().toDate().getTime();
                    holder.txtAdminTime.setText(TimeSince.getTimeAgo(time));
                }
                else {
                    holder.txtAdminTime.setText("just now");
                }

                if(!CurrentMessage.isSeen())
                {
                    mCollectionRef.document(UserId).
                            collection("ChatingUser")
                            .document(CurrentUserID).collection("Messages")
                            .document(CurrentMessage.getMsgID()).
                            update("seen",true);
                }
            }





    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        ImageView user_msg_visible,imgAdminMsg,imgUserMsg;
        TextView txtAdminMsg;
        TextView txtAdminTime;
        TextView txtUserMsg;
        TextView txtUserTime;
        LinearLayout lnr_Other,lnr_Current;

        View view;


        public MsgViewHolder(View itemView) {
            super(itemView);

            user_msg_visible = itemView.findViewById(R.id.user_msg_visible);
            txtAdminMsg = itemView.findViewById(R.id.txtAdminMsg);
            txtAdminTime = itemView.findViewById(R.id.txtAdminTime);
            txtUserMsg = itemView.findViewById(R.id.txtUserMsg);
            txtUserTime = itemView.findViewById(R.id.txtUserTime);
            lnr_Current = itemView.findViewById(R.id.lnr_Current);
            lnr_Other = itemView.findViewById(R.id.lnr_Other);
            imgAdminMsg = itemView.findViewById(R.id.imgAdminMsg);
            imgUserMsg = itemView.findViewById(R.id.imgUserMsg);
            view = itemView;

            lnr_Current.setOnLongClickListener(this);
            imgAdminMsg.setOnClickListener(this);
            imgUserMsg.setOnClickListener(this);
        }



        @Override
        public boolean onLongClick(View view) {
            if (mListener != null) {

                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemMsgLongClick(position,lnr_Current);

                }
            }
            return true;
        }

        @Override
        public void onClick(View view) {
            if (mLisener2 != null) {

                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    if (view.getId() == R.id.imgAdminMsg)
                    {
                        mLisener2.onImgClick(position,imgAdminMsg);
                    }
                    else {
                        mLisener2.onImgClick(position,imgUserMsg);
                    }


                }
            }
        }
    }


    public interface OnItemClickListener {

        void onItemMsgLongClick(int position,LinearLayout view);


    }

    public interface OnImgClickListener {

        void onImgClick(int position,ImageView img);


    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public void setOnImageClickLisener(OnImgClickListener mLisener)
    {
        mLisener2 = mLisener;
    }
}

