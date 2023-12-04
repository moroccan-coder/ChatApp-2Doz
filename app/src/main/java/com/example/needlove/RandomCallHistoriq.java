package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.needlove.Adapters.CallHistoryAdapter;
import com.example.needlove.Model.CallHistoryModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RandomCallHistoriq extends AppCompatActivity implements CallHistoryAdapter.OnItemClickListener {

    RecyclerView mRecyclerView;
    CallHistoryAdapter mAdapter;
    DocumentSnapshot lasDocument;
    CollectionReference CallRef;
    List<CallHistoryModel> mCallList;
    SwipeRefreshLayout swipeRefresh;
    LinearLayout lnrEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_call_historiq);

        mRecyclerView = findViewById(R.id.mrecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        lnrEmpty = findViewById(R.id.lnrEmpty);


        String CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CallRef = FirebaseFirestore.getInstance().collection("voiceCall").document(CurrentUserId).collection("CallHistory");


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mCallList = new ArrayList<>();
        mAdapter = new CallHistoryAdapter(this, mCallList);
        mRecyclerView.setAdapter(mAdapter);

        LoadData();


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (lasDocument != null) {
                    getmoreData();
                } else {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(RandomCallHistoriq.this, "No message yet!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        mAdapter.setOnItemClickListener(this);
    }


    public void back(View view) {
        finish();
    }



    private void LoadData() {

        Query query;

        query = CallRef
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(20);


        query.addSnapshotListener(RandomCallHistoriq.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {


                mCallList.clear();


                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {


                    CallHistoryModel msg = documentSnapshot.toObject(CallHistoryModel.class);
                    mCallList.add(msg);


                }
                if (queryDocumentSnapshots.size() > 0) {

                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                }
                else {
                    lnrEmpty.setVisibility(View.VISIBLE);
                    swipeRefresh.setVisibility(View.GONE);
                }


                swipeRefresh.setRefreshing(false);

            }


        });





    }

    void getmoreData() {
        Query query = CallRef.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lasDocument)
                .limit(10);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {


                    CallHistoryModel msg = documentSnapshot.toObject(CallHistoryModel.class);
                    mCallList.add(msg);


                }
                if (queryDocumentSnapshots.size() > 0) {

                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                }


                swipeRefresh.setRefreshing(false);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RandomCallHistoriq.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onItemClick(int position, CircleImageView my_profile_pic) {

        String selectedKey = mCallList.get(position).getUid();
        String pic = mCallList.get(position).getPic();

        Intent intent = new Intent(RandomCallHistoriq.this, UserProfile.class);
        intent.putExtra("key", selectedKey);
        intent.putExtra("pic", pic);

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(RandomCallHistoriq.this, my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);

    }
}
