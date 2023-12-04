package com.example.needlove;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.needlove.Adapters.FriendsListAdapter;
import com.example.needlove.Adapters.MessageListAdapter;
import com.example.needlove.Model.Friends;
import com.example.needlove.Model.MessageChat;
import com.example.needlove.Model.MessagesList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesFrag extends Fragment implements MessageListAdapter.OnItemClickListener {


    RecyclerView mRecyclerView;
    MessageListAdapter mAdapter;
    DocumentSnapshot lasDocument;
    CollectionReference MsgRef;
    List<MessagesList> mListMsg;
    SwipeRefreshLayout swipeRefresh;
    LinearLayout lnrEmpty;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_messages, container, false);


        mRecyclerView = view.findViewById(R.id.mrecyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        lnrEmpty = view.findViewById(R.id.lnrEmpty);


        String CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        MsgRef = FirebaseFirestore.getInstance().collection("Chat").document(CurrentUserId).collection("ChatingUser");


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mListMsg = new ArrayList<>();
        mAdapter = new MessageListAdapter(getActivity(), mListMsg);
        mRecyclerView.setAdapter(mAdapter);

        LoadData();


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (lasDocument != null) {
                    getmoreData();
                } else {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(getActivity(), "No message yet!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;
    }

    private void LoadData() {

        Query query;

        query = MsgRef
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(20);


         query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {


                mListMsg.clear();


                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {


                    MessagesList msg = documentSnapshot.toObject(MessagesList.class);
                    msg.setId(documentSnapshot.getId());
                    mListMsg.add(msg);


                }
                if (queryDocumentSnapshots.size() > 0) {

                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                } else {
                    lnrEmpty.setVisibility(View.VISIBLE);
                    swipeRefresh.setVisibility(View.GONE);
                }


                swipeRefresh.setRefreshing(false);

            }


        });


        mAdapter.setOnItemClickListener(this);

    }


    void getmoreData() {
        Query query = MsgRef.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lasDocument)
                .limit(10);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {


                    MessagesList msg = documentSnapshot.toObject(MessagesList.class);
                    msg.setId(documentSnapshot.getId());
                    mListMsg.add(msg);


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
                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onItemClick(int position, CircleImageView my_profile_pic) {

        MessagesList messagesList = mListMsg.get(position);

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("key", messagesList.getId());
        intent.putExtra("username", messagesList.getUsername());
        intent.putExtra("pic", messagesList.getPic());
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);
    }
}
