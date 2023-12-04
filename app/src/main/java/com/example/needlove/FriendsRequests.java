package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.needlove.Adapters.FriendReqAdapter;
import com.example.needlove.Adapters.newFriendsAdapter;
import com.example.needlove.Model.FriendsRequest;
import com.example.needlove.Model.newFriend;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsRequests extends AppCompatActivity implements FriendReqAdapter.OnItemClickListener, FriendReqAdapter.OnItemClickListenerAccept, FriendReqAdapter.OnItemClickListenerCancel, newFriendsAdapter.OnItemClickListener {


    CollectionReference myRequestRef;
    CollectionReference OthersRequestRef;


    FirebaseAuth mAuth;
    String CurrentUserId;


    FriendReqAdapter mAdapter;
    List<FriendsRequest> mListUser;
    RecyclerView mRecyclerView, recyclerHorizental;

    //request userId
    String RequestUserID = null;
    int currentPosition = 0;


    //bootom Sheet
    LinearLayout bottomChetLayout;
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout btnDeletUserReq, btnReturn;
    LinearLayout lnrShadou;

    CollectionReference FriendListMe, FriendListOther;

    //my Info
    String myName = null;
    String myPic = null;


    //Other User Info
    String OtherName = null;
    String OtherPic = null;

    //new friends
    newFriendsAdapter mAdapterFriend;
    List<newFriend> mLisnewFriend;

    LinearLayout lnremptyReq;

    DocumentSnapshot lasDocument;
    DocumentReference usrRef,incrementRequ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_requests);


        mRecyclerView = findViewById(R.id.RecyclerView);
        bottomChetLayout = findViewById(R.id.bootmoShes);
        recyclerHorizental = findViewById(R.id.recyclerHorizental);
        lnremptyReq = findViewById(R.id.lnremptyReq);
        lnrShadou = findViewById(R.id.lnrShadou);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomChetLayout);
        btnDeletUserReq = findViewById(R.id.btnDeletFriendReq);
        btnReturn = findViewById(R.id.btnReturn);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();

        usrRef  = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserId).collection("Counter").document("friends");

        myRequestRef = FirebaseFirestore.getInstance().collection("FriendsRequest")
                .document(CurrentUserId).collection("Receive");


        FriendListMe = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserId).collection("Friends");
        incrementRequ  = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserId).collection("Counter").document("requests");
        emptyIncremReq();

        //get my data
        SharedPreferences pref = getSharedPreferences("userInf", Context.MODE_PRIVATE);
        myName = pref.getString("username", "User");
        myPic = pref.getString("pic", "http://");

        //init RecyclerView


        //Friends REquest

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mListUser = new ArrayList<>();
        mAdapter = new FriendReqAdapter(this, mListUser);
        mRecyclerView.setAdapter(mAdapter);


        //

        //new friends

        recyclerHorizental.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerHorizental.setHasFixedSize(true);
        mLisnewFriend = new ArrayList<>();
        mAdapterFriend = new newFriendsAdapter(this, mLisnewFriend);
        recyclerHorizental.setAdapter(mAdapterFriend);
        mAdapterFriend.setOnItemClickListener(this);

        //
        LoadData();

        FriendListMe.orderBy("datesend", Query.Direction.DESCENDING).limit(10).addSnapshotListener(FriendsRequests.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                mLisnewFriend.clear();
                for (QueryDocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                    newFriend friend = documentSnapshots.toObject(newFriend.class);
                  friend.setId(documentSnapshots.getId());
                    mLisnewFriend.add(friend);
                }

                mAdapterFriend.notifyDataSetChanged();

            }
        });


        btnDeletUserReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DeleteUserRequest();

            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                lnrShadou.setVisibility(View.GONE);
            }
        });


        mAdapter.setOnItemClickListener(this);
        //  mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemClickAccept(this);
        mAdapter.setOnItemClickCancel(this);

    }

    private void emptyIncremReq() {
        incrementRequ.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists())
                {
                    incrementRequ.update("nbrequests",0);
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FriendsRequests.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void DeleteUserRequest() {

        if (RequestUserID != null) {

            myRequestRef.document(RequestUserID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(FriendsRequests.this, "Request Removed", Toast.LENGTH_SHORT).show();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    lnrShadou.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FriendsRequests.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void LoadData() {

        Query query;

        if (lasDocument == null) {
            query = myRequestRef
                    .orderBy("datesend", Query.Direction.DESCENDING)
                    .limit(10);
        } else {
            query = myRequestRef.orderBy("datesend", Query.Direction.DESCENDING)
                    .startAfter()
                    .limit(3);
        }

        query.addSnapshotListener(FriendsRequests.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                mListUser.clear();

                for (QueryDocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                    FriendsRequest friend = documentSnapshots.toObject(FriendsRequest.class);
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

                    mAdapterFriend.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                }


            }
        });


    }


    @Override
    public void onItemClick(int position, CircleImageView imageView) {


        String selectedKey = mListUser.get(position).getId();
        String pic = mListUser.get(position).getPic();

        Intent intent = new Intent(FriendsRequests.this, UserProfile.class);
        intent.putExtra("key", selectedKey);
        intent.putExtra("pic", pic);

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(FriendsRequests.this, imageView, imageView.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);

    }


    @Override
    public void onItemClickAccept(int position) {

        OtherName = mListUser.get(position).getUsername();
        OtherPic = mListUser.get(position).getPic();
        RequestUserID = mListUser.get(position).getId();

        AddAsFriend();
    }

    @Override
    public void onItemClickCancel(int position) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        lnrShadou.setVisibility(View.VISIBLE);
        RequestUserID = mListUser.get(position).getId();
    }


    void AddAsFriend() {
        if (RequestUserID != null) {
            OthersRequestRef = FirebaseFirestore.getInstance().collection("FriendsRequest")
                    .document(RequestUserID).collection("Send");


            FriendListOther = FirebaseFirestore.getInstance().collection("Users").document(RequestUserID).collection("Friends");

            myRequestRef.document(RequestUserID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful())
                    {
                        OthersRequestRef.document(CurrentUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()
                                ) {

                                    Map<String, Object> rMapReceiver = new HashMap<>();
                                    rMapReceiver.put("pic", OtherPic);
                                    rMapReceiver.put("username", OtherName);
                                    rMapReceiver.put("datesend", FieldValue.serverTimestamp());
                                    FriendListMe.document(RequestUserID).set(rMapReceiver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Map<String, Object> rMapReceiverMe = new HashMap<>();
                                            rMapReceiverMe.put("pic", myPic);
                                            rMapReceiverMe.put("username", myName);
                                            rMapReceiverMe.put("datesend", FieldValue.serverTimestamp());

                                            FriendListOther.document(CurrentUserId).set(rMapReceiverMe).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    IncrementFriends();
                                                    Toast.makeText(FriendsRequests.this, "Friend Added", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    });
                                }
                            }
                        });
                    }

                }
            });
        }
    }


    //new Friend Click
    @Override
    public void onItemClickNew(int position , CircleImageView imageView) {


        String selectedKey = mLisnewFriend.get(position).getId();
        String pic = mLisnewFriend.get(position).getPic();

        Intent intent = new Intent(FriendsRequests.this, UserProfile.class);
        intent.putExtra("key", selectedKey);
        intent.putExtra("pic", pic);

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(FriendsRequests.this, imageView, imageView.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);

    }

    public void back(View view) {
        onBackPressed();

    }

    public void showAllfriends(View view) {
        startActivity(new Intent(FriendsRequests.this,AllFriendActivity.class));

    }




    private void IncrementFriends() {


        usrRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists())
                {
                    Map<String, Object> mp = new HashMap();
                    mp.put("nbfriends", 1);
                    //Create the filed
                    usrRef.set(mp);
                }
                else {
                    usrRef.update("nbfriends", FieldValue.increment(1));
                }

                IncrementFriendsOther();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FriendsRequests.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void IncrementFriendsOther()
    {
       DocumentReference usrRefOther  = FirebaseFirestore.getInstance().collection("Users").document(RequestUserID).collection("Counter").document("friends");

        usrRefOther.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists())
                {
                    Map<String, Object> mp = new HashMap();
                    mp.put("nbfriends", 1);
                    //Create the filed
                    usrRefOther.set(mp);
                }
                else {
                    usrRefOther.update("nbfriends", FieldValue.increment(1));
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FriendsRequests.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
