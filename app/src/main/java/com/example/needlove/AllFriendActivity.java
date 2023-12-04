package com.example.needlove;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.needlove.Adapters.FriendReqAdapter;
import com.example.needlove.Adapters.FriendsListAdapter;
import com.example.needlove.Model.Friends;
import com.example.needlove.Model.FriendsRequest;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllFriendActivity extends AppCompatActivity implements FriendsListAdapter.OnItemClickListenerMor, FriendsListAdapter.OnItemClickListener {

    RecyclerView mRecyclerView;
    FriendsListAdapter mAdapter;
    DocumentSnapshot lasDocument;
    DocumentReference usrRef,usrRefOther;
    List<Friends> mListUser;
    CollectionReference FriendList, FriendListOther;
    //request userId
    String RequestUserID = null;
    String CurrentUserId;

    LinearLayout lnremptyReq;
    TextView txtNbFriend;

    //bootom Sheet
    LinearLayout bootmoShes;
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout btnSendMessage, btnDeletFriend, btnReturn, lnrShadou,btnBlockUser,btnUnblockUser;
    CircleImageView my_profile_pic;
    TextView username;

    DocumentReference UserRefMe, UserRefOther;

    String Username , UserPic;
    boolean isBlocked = false;
    boolean imBlocked = false;

    Dialog mdDialog;

    boolean isCheetVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_friend);

        mRecyclerView = findViewById(R.id.mrecyclerView);
        bootmoShes = findViewById(R.id.bootmoShes);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        lnrShadou = findViewById(R.id.lnrShadou);
        lnremptyReq = findViewById(R.id.lnremptyReq);
        txtNbFriend = findViewById(R.id.txtNbFriend);
        btnBlockUser = findViewById(R.id.btnBlockUser);
        btnUnblockUser = findViewById(R.id.btnUnblockUser);
        btnDeletFriend = findViewById(R.id.btnDeletFriend);
        btnReturn = findViewById(R.id.btnReturn);
        my_profile_pic = findViewById(R.id.my_profile_pic);
        username = findViewById(R.id.username);
        bottomSheetBehavior = BottomSheetBehavior.from(bootmoShes);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mdDialog = new Dialog(this);


        CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usrRef  = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserId).collection("Counter").document("friends");
        FriendList = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserId).collection("Friends");



        UserRefMe = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(CurrentUserId);


        //Friends REquest

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mListUser = new ArrayList<>();
        mAdapter = new FriendsListAdapter(this, mListUser);
        mRecyclerView.setAdapter(mAdapter);

        usrRef.addSnapshotListener(AllFriendActivity.this,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.get("nbfriends") !=null)
                {
                    txtNbFriend.setText(documentSnapshot.get("nbfriends").toString());
                }
                else {
                    txtNbFriend.setText("0");
                }

            }
        });

        LoadData();

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                lnrShadou.setVisibility(View.GONE);
                isCheetVisible = false;
            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                isCheetVisible =false;
                    lnrShadou.setVisibility(View.GONE);

                    Intent intent = new Intent(AllFriendActivity.this, ChatActivity.class);
                    intent.putExtra("key", RequestUserID);
                    intent.putExtra("username", Username);
                    intent.putExtra("pic", UserPic);
                    startActivity(intent);
                }

        });



        btnBlockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String, Object> usrblk = new HashMap<>();
                usrblk.put("dateblocked", FieldValue.serverTimestamp());
                UserRefMe.collection("UsersBlock").document(RequestUserID).set(usrblk).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AllFriendActivity.this, "User Blocked", Toast.LENGTH_SHORT).show();
                        isBlocked = true;
                       btnBlockUser.setVisibility(View.GONE);
                       btnUnblockUser.setVisibility(View.VISIBLE);

                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        lnrShadou.setVisibility(View.GONE);
                        isCheetVisible = false;
                    }
                });

            }
        });

        btnUnblockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserRefMe.collection("UsersBlock").document(RequestUserID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AllFriendActivity.this, "User Unblocked", Toast.LENGTH_SHORT).show();
                        isBlocked = false;

                        btnBlockUser.setVisibility(View.VISIBLE);
                        btnUnblockUser.setVisibility(View.GONE);

                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        lnrShadou.setVisibility(View.GONE);
                        isCheetVisible = false;

                    }
                });

            }
        });




        btnDeletFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mdDialog.setContentView(R.layout.delete_friend_confirmation);
                TextView btnYes = mdDialog.findViewById(R.id.btn_yes);
                TextView btnNo = mdDialog.findViewById(R.id.btn_no);


                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deletUser();

                        mdDialog.dismiss();
                    }

                });

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        mdDialog.dismiss();
                    }
                });


                mdDialog.show();



            }
        });

        //

        mAdapter.setOnItemClickMore(this);
        mAdapter.setOnItemClickListener(this);
    }

    public void back(View view) {
        onBackPressed();
    }

    private void LoadData() {

        Query query;

        if (lasDocument == null) {
            query = FriendList
                    .orderBy("datesend", Query.Direction.DESCENDING)
                    .limit(3);
        } else {
            query = FriendList.orderBy("datesend", Query.Direction.DESCENDING)
                    .startAfter(lasDocument)
                    .limit(3);
        }

        query.addSnapshotListener(AllFriendActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                mListUser.clear();

                for (QueryDocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                    Friends friend = documentSnapshots.toObject(Friends.class);
                    friend.setId(documentSnapshots.getId());
                    mListUser.add(friend);

                }
                if (mListUser.isEmpty()) {
                    lnremptyReq.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    lnremptyReq.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }


                if (queryDocumentSnapshots.size() > 0) {

                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                }


            }
        });


    }

    @Override
    public void onItemClickMore(int position) {
        lnrShadou.setVisibility(View.VISIBLE);
        RequestUserID = mListUser.get(position).getId();
        UserPic = mListUser.get(position).getPic();
        Username = mListUser.get(position).getUsername();


        UserRefOther = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(RequestUserID);

        CheckUserBlocked(RequestUserID);
        CheckBlockMe(RequestUserID);

        Picasso.get().load(UserPic)
                .placeholder(R.drawable.avatar_prf)
                .into(my_profile_pic);

        username.setText(Username);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        isCheetVisible = true;
    }

    private void CheckBlockMe(String ussrID) {

        UserRefOther = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(ussrID);



        UserRefOther.collection("UsersBlock").document(CurrentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    imBlocked = true;

                } else {

                    imBlocked = false;

                }
            }
        });
    }

    private void CheckUserBlocked(String ussrID) {

        UserRefMe.collection("UsersBlock").document(ussrID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {

                    isBlocked = true;

                    btnBlockUser.setVisibility(View.GONE);
                    btnUnblockUser.setVisibility(View.VISIBLE);

                } else {
                    btnBlockUser.setVisibility(View.VISIBLE);
                    btnUnblockUser.setVisibility(View.GONE);

                    isBlocked = false;
                }
            }
        });
    }

    @Override
    public void onItemClick(int position, CircleImageView my_profile_pic) {

        String selectedKey = mListUser.get(position).getId();
        String pic = mListUser.get(position).getPic();

        Intent intent = new Intent(AllFriendActivity.this, UserProfile.class);
        intent.putExtra("key", selectedKey);
        intent.putExtra("pic", pic);

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(AllFriendActivity.this, my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);
    }

    void deletUser() {
        FriendListOther = FirebaseFirestore.getInstance().collection("Users").document(RequestUserID).collection("Friends");

        FriendList.document(RequestUserID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                FriendListOther.document(CurrentUserId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AllFriendActivity.this, "Friend Deleted", Toast.LENGTH_SHORT).show();
                        usrRef.update("nbfriends", FieldValue.increment(-1));
                        decrimentOther();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        isCheetVisible = false;
                        lnrShadou.setVisibility(View.GONE);
                    }
                });

            }
        });

    }

    private void decrimentOther() {
        usrRefOther  = FirebaseFirestore.getInstance().collection("Users").document(RequestUserID).collection("Counter").document("friends");
        usrRefOther.update("nbfriends", FieldValue.increment(-1));
    }

    @Override
    public void onBackPressed() {
        if (isCheetVisible)
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            lnrShadou.setVisibility(View.GONE);
            isCheetVisible = false;
        }
        else {
            finish();
        }
    }
}
