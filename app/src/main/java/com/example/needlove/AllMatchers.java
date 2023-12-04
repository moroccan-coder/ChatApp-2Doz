package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.needlove.Adapters.AllMatcherAdapter;
import com.example.needlove.Adapters.LikesAdapter;
import com.example.needlove.Adapters.MatcherAdapter;
import com.example.needlove.Model.LikesModel;
import com.example.needlove.Model.MatcherModel;
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

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllMatchers extends AppCompatActivity implements AllMatcherAdapter.OnItemClickListener {

    List<MatcherModel> mLisMatcher;
    RecyclerView recyclerV;

    //firebase
    FirebaseAuth mAuth;
    String CurrentUserID;
    CollectionReference UserRef;

    AllMatcherAdapter mAdapter;
    DocumentSnapshot lasDocument;

    LinearLayout progressBr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_matchers);

        recyclerV = findViewById(R.id.recyclerV);
        progressBr = findViewById(R.id.progressBr);


        //intilise firebase
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserID).collection("Matchers");


        //Friends REquest

        recyclerV.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerV.setHasFixedSize(true);
        mLisMatcher = new ArrayList<>();
        mAdapter = new AllMatcherAdapter(this, mLisMatcher);
        recyclerV.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);


        LoadData();


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
                    progressBr.setVisibility(View.VISIBLE);
                    LoadData();

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

    private void LoadData() {
        Query query;


        if (lasDocument == null) {
            query = UserRef
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(3);
        } else {
            query = UserRef
                    .orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lasDocument)
                    .limit(3);
        }


        query.addSnapshotListener(AllMatchers.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for (QueryDocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                    MatcherModel match = documentSnapshots.toObject(MatcherModel.class);
                    match.setId(documentSnapshots.getId());
                    mLisMatcher.add(match);
                }

                progressBr.setVisibility(View.GONE);
                if (queryDocumentSnapshots.size() > 0) {

                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                }

            }
        });

    }

    public void back(View view) {
        finish();
    }

    @Override
    public void onItemClick(int position, CircleImageView my_profile_pic) {

        String selectedKey = mLisMatcher.get(position).getId();
        String pic = mLisMatcher.get(position).getPic();

        Intent intent = new Intent(AllMatchers.this, UserProfile.class);
        intent.putExtra("key", selectedKey);
        intent.putExtra("pic", pic);

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(AllMatchers.this, my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);

    }
}
