package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.needlove.Adapters.LikesAdapter;
import com.example.needlove.Adapters.MatcherAdapter;
import com.example.needlove.Adapters.newFriendsAdapter;
import com.example.needlove.Model.FriendsRequest;
import com.example.needlove.Model.LikesModel;
import com.example.needlove.Model.MatcherModel;
import com.example.needlove.Model.newFriend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikesActivity extends AppCompatActivity implements MatcherAdapter.OnItemClickListener, LikesAdapter.OnItemClickListener, LikesAdapter.OnItemClickListenerAccept, LikesAdapter.OnItemClickListenerCancel {
    List<LikesModel> mLisLikes;
    List<MatcherModel> mLisMatcher;
    RecyclerView recyclerV, recyclerHorizental;

    //firebase
    FirebaseAuth mAuth;
    String CurrentUserID;
    CollectionReference UserRef;

    LikesAdapter mAdapter;
    MatcherAdapter mAdapterM;
    DocumentSnapshot lasDocument;

    //Sheard user profil
    String usernamePref, pic;
    LinearLayout progressBr;

    // empty likes counter"
    DocumentReference incrementLikes,incremLik;

    LinearLayout lnremptyLikes;
    boolean firstList = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        recyclerV = findViewById(R.id.recyclerV);
        progressBr = findViewById(R.id.progressBr);
        recyclerHorizental = findViewById(R.id.recyclerHorizental);
        lnremptyLikes = findViewById(R.id.lnremptyLikes);

        //intilise firebase
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseFirestore.getInstance().collection("Users");
        incrementLikes  = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserID).collection("Counter").document("Likes");
        emptyIncremLikes();


        //Matchers

        recyclerHorizental.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerHorizental.setHasFixedSize(true);
        mLisMatcher = new ArrayList<>();
        mAdapterM = new MatcherAdapter(this, mLisMatcher);
        recyclerHorizental.setAdapter(mAdapterM);
        mAdapterM.setOnItemClickListener(this);

        //Friends REquest

        recyclerV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerV.setHasFixedSize(true);
        mLisLikes = new ArrayList<>();
        mAdapter = new LikesAdapter(this, mLisLikes);
        recyclerV.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemClickAccept(this);
        mAdapter.setOnItemClickCancel(this);


        LoadData();


        SharedPreferences pref = this.getSharedPreferences("userInf", Context.MODE_PRIVATE);


        if (pref.contains("username")) {
            usernamePref = pref.getString("username", "User");
            pic = pref.getString("pic", "http://");
        } else {
            usernamePref = "User";
            pic = "http://";
        }

        UserRef.document(CurrentUserID).collection("Matchers").orderBy("time", Query.Direction.DESCENDING).limit(10).addSnapshotListener(LikesActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                mLisMatcher.clear();
                for (QueryDocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                    MatcherModel match = documentSnapshots.toObject(MatcherModel.class);
                    match.setId(documentSnapshots.getId());
                    mLisMatcher.add(match);
                }

                mAdapterM.notifyDataSetChanged();

            }
        });


        //RecyclerHandl Last item to load more
        recyclerV.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (isLastItemDisplaying(recyclerView)) {
                    LoadData();

                }
            }
        });


    }


    private void LoadData() {
        Query query;


        if (lasDocument == null) {
            query = UserRef.document(CurrentUserID)
                    .collection("swipe")
                    .whereEqualTo("action", "yeps")
                    .whereEqualTo("show", true)
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(10);
        } else {
            query = UserRef.document(CurrentUserID)
                    .collection("swipe")
                    .whereEqualTo("action", "yeps")
                    .whereEqualTo("show", true)
                    .orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lasDocument)
                    .limit(5);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressBr.setVisibility(View.GONE);
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                    LikesModel likes = documentSnapshot.toObject(LikesModel.class);
                    likes.setId(documentSnapshot.getId());
                    mLisLikes.add(likes);


                }
                if (queryDocumentSnapshots.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                   firstList = false;
                }
                else {
                    if (firstList) {
                        lnremptyLikes.setVisibility(View.VISIBLE);
                        recyclerV.setVisibility(View.GONE);
                    } else {
                        lnremptyLikes.setVisibility(View.GONE);
                        recyclerV.setVisibility(View.VISIBLE);
                    }
                }


            }
        });

    }


    private boolean isLastItemDisplaying(androidx.recyclerview.widget.RecyclerView recyclerView) {

        //Check if the adapter item count is greater than 0
        if (recyclerView.getAdapter().getItemCount() != 0) {
            //get the last visible item on screen using the layout manager
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();

            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                return true;
            }

        }
        return false;
    }

    public void back(View view) {
        finish();
    }

    public void showAllMatcher(View view) {
        startActivity(new Intent(LikesActivity.this,AllMatchers.class));
    }


    //Click Match Icon
    @Override
    public void onItemClickMatch(int position, CircleImageView img) {
        String selectedKey = mLisMatcher.get(position).getId();
        String pic = mLisMatcher.get(position).getPic();

        Intent intent = new Intent(LikesActivity.this, UserProfile.class);
        intent.putExtra("key", selectedKey);
        intent.putExtra("pic", pic);

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(LikesActivity.this, img, img.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);
    }

    @Override
    public void onItemClick(int position, CircleImageView my_profile_pic) {

        String selectedKey = mLisLikes.get(position).getId();
        String pic = mLisLikes.get(position).getPic();

        Intent intent = new Intent(LikesActivity.this, UserProfile.class);
        intent.putExtra("key", selectedKey);
        intent.putExtra("pic", pic);

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(LikesActivity.this, my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);


    }

    @Override
    public void onItemClickAccept(int position) {
        progressBr.setVisibility(View.VISIBLE);
        String userId = mLisLikes.get(position).getId();
        String userNam = mLisLikes.get(position).getUsername();
        String userPicc = mLisLikes.get(position).getPic();

        HashMap usermtch = new HashMap();
        usermtch.put("show", false);
        UserRef.document(CurrentUserID).collection("swipe").document(userId).set(usermtch, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                progressBr.setVisibility(View.GONE);
                //refresh recyclerview
                mLisLikes.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mLisLikes.size());
                //

                final HashMap userSwip = new HashMap();

                userSwip.put("action", "yeps");
                userSwip.put("time", FieldValue.serverTimestamp());
                userSwip.put("username", usernamePref);
                userSwip.put("pic", pic);
                userSwip.put("show", false);
                UserRef.document(userId).collection("swipe").document(CurrentUserID).set(userSwip).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        AddMatcher(userId, userNam, userPicc);
                        IncrementLikees(userId);
                    }
                });
            }
        });


    }

    @Override
    public void onItemClickCancel(int position) {
        progressBr.setVisibility(View.VISIBLE);
        String userId = mLisLikes.get(position).getId();


        HashMap usermtch = new HashMap();
        usermtch.put("show", false);
        UserRef.document(CurrentUserID).collection("swipe").document(userId).set(usermtch, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //refresh recyclerview
                mLisLikes.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mLisLikes.size());
                //
                progressBr.setVisibility(View.GONE);

                HashMap userSwip = new HashMap();
                userSwip.put("action", "nope");
                UserRef.document(userId).collection("swipe").document(CurrentUserID).set(userSwip);
            }
        });


    }


    private void AddMatcher(String userId, String name, String ppic) {

        Map<String, Object> mtchrMap = new HashMap<>();
        mtchrMap.put("time", FieldValue.serverTimestamp());
        mtchrMap.put("username", name);
        mtchrMap.put("pic", ppic);

        UserRef.document(CurrentUserID).collection("Matchers").document(userId).set(mtchrMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> mtchrMapMe = new HashMap<>();
                mtchrMapMe.put("time", FieldValue.serverTimestamp());
                mtchrMapMe.put("username", usernamePref);
                mtchrMapMe.put("pic", pic);

                UserRef.document(userId).collection("Matchers").document(CurrentUserID).set(mtchrMapMe);
            }
        });


    }


    private void emptyIncremLikes() {
        incrementLikes.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists())
                {
                    incrementLikes.update("nblikes",0);
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LikesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void IncrementLikees(String userid) {

        incremLik  = FirebaseFirestore.getInstance().collection("Users").document(userid).collection("Counter").document("Likes");

        incremLik.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists())
                {
                    Map<String, Object> mp = new HashMap();
                    mp.put("nblikes", 1);
                    //Create the filed
                    incremLik.set(mp);
                }
                else {
                    incremLik.update("nblikes", FieldValue.increment(1));
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LikesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
