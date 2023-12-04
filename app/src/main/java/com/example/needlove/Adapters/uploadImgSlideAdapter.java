package com.example.needlove.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.needlove.Model.Slide;
import com.example.needlove.Model.img_slide;
import com.example.needlove.OnIntentReceived;
import com.example.needlove.ProfileFrag;
import com.example.needlove.R;
import com.example.needlove.SetupActivity;
import com.example.needlove.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class uploadImgSlideAdapter extends RecyclerView.Adapter<uploadImgSlideAdapter.ViewHolder> {
    private ArrayList<String> imgUrlList;
    private ArrayList<String> imgUrlList_indice;
    private Context mContext;

    private ProgressBar holderProgressBar;


    private OnItemClickListener mListener;
    private OnItemLongClickListenr mListenerLongClick;
    private OnItemTouchLiseener mListenerTouchClick;

    public uploadImgSlideAdapter(Context context, ArrayList<String> imgURL) {
        this.mContext = context;
        this.imgUrlList = imgURL;

    }

    @Override
    public uploadImgSlideAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.uplad_img_slide_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final uploadImgSlideAdapter.ViewHolder viewHolder, final int i) {

        holderProgressBar = viewHolder.progress_heart;
        if (imgUrlList.get(i).equals("http://"))
        {
            Picasso.get().load(imgUrlList.get(i)).placeholder(R.drawable.addpicc).into(viewHolder.imgSlidee);
        }
        else {
            Picasso.get().load(imgUrlList.get(i)).placeholder(R.drawable.avatar_prf).into(viewHolder.imgSlidee);
        }

        holderProgressBar.setVisibility(View.GONE);


    }

    @Override
    public int getItemCount() {
        return imgUrlList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

        private ImageView imgSlidee;
        private ProgressBar progress_heart;
        private View itemView;
        LinearLayout lnrUpdatePic;
        FloatingActionButton updateImage, updateDelete;

        public ViewHolder(View view) {
            super(view);
            imgSlidee = view.findViewById(R.id.imgSlide);
            progress_heart = view.findViewById(R.id.progress_heart);
            lnrUpdatePic = view.findViewById(R.id.lnrUpdatePic);
            updateImage = view.findViewById(R.id.updateImage);
            updateDelete = view.findViewById(R.id.updateDelete);
            itemView = view;


            imgSlidee.setOnClickListener(this);
            imgSlidee.setOnLongClickListener(this);
            imgSlidee.setOnTouchListener(this);

            updateImage.setOnClickListener(this);
            updateDelete.setOnClickListener(this);

            lnrUpdatePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lnrUpdatePic.setVisibility(View.GONE);
                }
            });


        }

        @Override
        public void onClick(View view) {

            if (view.getId() == R.id.imgSlide) {

                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(1,position, imgSlidee, progress_heart, lnrUpdatePic);
                    }
                }

            } else if (view.getId() == R.id.updateImage) {

                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(2,position, imgSlidee, progress_heart, lnrUpdatePic);
                    }
                }

            } else {

                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(3,position, imgSlidee, progress_heart, lnrUpdatePic);
                    }
                }

            }





        }

        @Override
        public boolean onLongClick(View view) {
            if (mListenerLongClick != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListenerLongClick.onItemLongClick(position, itemView);
                }
            }

            return true;
        }

        @Override
        public boolean onTouch(View viewTouch, MotionEvent motionEvent) {


            if (mListenerLongClick != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListenerTouchClick.onItemTouchClick(position, itemView, viewTouch, motionEvent);
                }
            }
            return false;
        }
    }


    public interface OnItemClickListener {

        void onItemClick(int wichClick,int position, ImageView imgv, ProgressBar progress, LinearLayout lnr);


    }





    public interface OnItemLongClickListenr {

        void onItemLongClick(int position, View itemView);
    }


    public interface OnItemTouchLiseener {

        void onItemTouchClick(int position, View itemView, View vv, MotionEvent motionEvent);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public void setOnItemLongClickListener(OnItemLongClickListenr listener) {
        mListenerLongClick = listener;
    }


    public void setOnItemTouchLisener(OnItemTouchLiseener listener) {
        mListenerTouchClick = listener;
    }


}