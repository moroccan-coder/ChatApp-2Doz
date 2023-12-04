package com.example.needlove;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.needlove.Adapters.UserAdapter;

import com.example.needlove.Model.UsersDiscover;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DiscoverFrag extends Fragment implements UserAdapter.OnadaptrChenged, UserAdapter.OnItemClickListener {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION_FINE = 12;
    RecyclerView recyclerView;
    SharedPreferences pref;

    private boolean allowRefresh = false;

    private UserAdapter mAdapter;

    ImageView discover_settinngs;

    int increm = 4;
    ConstraintLayout BtnRequest, BtnNotif;

    FirestorePagingOptions<UsersDiscover> options;


    //firebase
    FirebaseAuth mAuth;
    String CurrentUserID;
    CollectionReference UsersRef;
    private DocumentSnapshot lasResult;

    int startreferech = 0;
    LinearLayout appbarDiscover;

    int y = 0;

    DocumentReference incrementRequ, incrementLikes;
    TextView textFriendNotif, textNotif;
    RelativeLayout lnrProgress;


    ListenerRegistration lisenerRequest;
    ListenerRegistration lisenerLikes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_discover, container, false);
        //intilise firebase
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseFirestore.getInstance().collection("Users");

        incrementRequ = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserID).collection("Counter").document("requests");
        incrementLikes = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserID).collection("Counter").document("Likes");


        // get Preference
        pref = getActivity().getSharedPreferences("discover_filter", Context.MODE_PRIVATE);


        recyclerView = view.findViewById(R.id.RecyclerView);
        appbarDiscover = view.findViewById(R.id.appbarDiscover);

        textFriendNotif = view.findViewById(R.id.textFriendNotif);
        textNotif = view.findViewById(R.id.textNotif);
        discover_settinngs = view.findViewById(R.id.discover_settinngs);
        BtnNotif = view.findViewById(R.id.BtnNotif);
        BtnRequest = view.findViewById(R.id.BtnRequest);
        lnrProgress = view.findViewById(R.id.lnrProgress);

        setupRecycler();


        requestLisener();
        likesLisener();


        //filter Activity

        discover_settinngs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), discover_settings.class));
                btnAnim(discover_settinngs);
                allowRefresh = true;
            }
        });


        BtnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAnim(BtnRequest);

                Intent intent = new Intent(getActivity(), FriendsRequests.class);
                Bundle bundle = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, view.getTransitionName()).toBundle();
                }
                startActivity(intent, bundle);


            }
        });

        BtnNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAnim(BtnNotif);

                Intent intent = new Intent(getActivity(), LikesActivity.class);
                Bundle bundle = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, view.getTransitionName()).toBundle();
                }
                startActivity(intent, bundle);

            }
        });

        // Refresh Action on Swipe Refresh Layout


        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnadapterStateChange(this);

        return view;

    }

    private void likesLisener() {

        lisenerLikes = incrementLikes.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e == null) {
                    if (documentSnapshot.contains("nblikes")) {
                        if (Integer.parseInt(documentSnapshot.get("nblikes").toString()) > 0) {
                            textNotif.setVisibility(View.VISIBLE);
                            textNotif.setText(documentSnapshot.get("nblikes").toString());
                        } else {
                            textNotif.setVisibility(View.GONE);
                        }
                    }
                }


            }
        });
    }

    private void requestLisener() {

        lisenerRequest = incrementRequ.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e == null) {
                    if (documentSnapshot.contains("nbrequests")) {
                        if (Integer.parseInt(documentSnapshot.get("nbrequests").toString()) > 0) {
                            textFriendNotif.setVisibility(View.VISIBLE);
                            textFriendNotif.setText(documentSnapshot.get("nbrequests").toString());
                        } else {
                            textFriendNotif.setVisibility(View.GONE);
                        }
                    }
                }


            }
        });
    }

    void btnAnim(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        view.startAnimation(myAnim);
    }

    private void setupRecycler() {
        Query baseQuery = null;

        if (pref.contains("country_code")) {
            //## user With Filter

            switch (pref.getInt("filter_cat", 0)) {
                case 1:
                    baseQuery = UsersRef.whereEqualTo("gender", pref.getString("looking_for", "Female"))
                            .whereEqualTo("agerange", pref.getInt("age_range", 1));
                    break;

                case 2:
                    baseQuery = UsersRef.whereEqualTo("gender", pref.getString("looking_for", "Female"));
                    break;

                case 3:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco"))
                            .whereEqualTo("gender", pref.getString("looking_for", "Female"));
                    break;

                case 4:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco"))
                            .whereEqualTo("agerange", pref.getInt("age_range", 1));
                    break;

                case 5:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco"));
                    break;

                case 6:
                    baseQuery = UsersRef.whereEqualTo("agerange", pref.getInt("age_range", 1));
                    break;

                case 7:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco"))
                            .whereEqualTo("gender", pref.getString("looking_for", "Female"));
                    break;
                case 8:
                    baseQuery = UsersRef.whereEqualTo("gender", pref.getString("looking_for", "Female"))
                            .whereEqualTo("agerange", pref.getInt("age_range", 1)).whereEqualTo("online", true);
                    break;

                case 9:
                    baseQuery = UsersRef.whereEqualTo("gender", pref.getString("looking_for", "Female")).whereEqualTo("online", true);
                    break;

                case 10:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco"))
                            .whereEqualTo("gender", pref.getString("looking_for", "Female")).whereEqualTo("online", true);
                    break;

                case 11:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco"))
                            .whereEqualTo("agerange", pref.getInt("age_range", 1)).whereEqualTo("online", true);
                    break;

                case 12:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco")).whereEqualTo("online", true);
                    break;

                case 13:
                    baseQuery = UsersRef.whereEqualTo("agerange", pref.getInt("age_range", 1)).whereEqualTo("online", true);
                    break;

                case 14:
                    baseQuery = UsersRef.whereEqualTo("country", pref.getString("country", "morocco"))
                            .whereEqualTo("gender", pref.getString("looking_for", "Female")).whereEqualTo("online", true);
                    break;
                case 15:
                    baseQuery = UsersRef.whereEqualTo("online", true);
                    break;
            }


        } else {
            //## user With No Filter
            baseQuery = UsersRef.orderBy("uid");
        }


// This configuration comes from the Paging Support Library
        // https://developer.android.com/reference/android/arch/paging/PagedList.Config.html
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(4)
                .setPageSize(6)
                .build();

        options = new FirestorePagingOptions.Builder<UsersDiscover>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, UsersDiscover.class)
                .build();


        mAdapter = new UserAdapter(options, CurrentUserID);
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), 2, recyclerView.VERTICAL, false);
        //  final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
    }


    private void getUsers_nearly() {


   /*     DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("Users");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UsersDiscover user;
                userModels = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    if (userSnapshot.hasChild("latitude") &&
                            userSnapshot.hasChild("longitude")) {

                        String lattd = userSnapshot.child("latitude").getValue().toString();
                        String langgt = userSnapshot.child("longitude").getValue().toString();

                        // if (!userSnapshot.child("uid").getValue().toString().equals(CurrentUserID) && checkDistence(lattd, langgt)) {

                        userModels.add(userSnapshot.getValue(UsersDiscover.class));
                        // }
                    }
                }

                mAdapter.addAll(userModels);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        */
    }


    void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new DiscoverFrag()).addToBackStack(null).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Initialize();
        if (allowRefresh) {
            allowRefresh = false;
            refreshFragment();
        }

        requestLisener();
        likesLisener();
    }


    @Override
    public void onStart() {
        super.onStart();

        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        mAdapter.stopListening();


        lisenerLikes.remove();
        lisenerRequest.remove();
    }


    @Override
    public void onStateChengged(LoadingState state) {
        switch (state) {
            case LOADING_INITIAL:
            case LOADING_MORE:
                //swipeRefreshLayout.setRefreshing(true);
                lnrProgress.setVisibility(View.VISIBLE);
                break;

            case LOADED:
                //swipeRefreshLayout.setRefreshing(false);
                lnrProgress.setVisibility(View.GONE);
                break;

            case ERROR:
                mAdapter.retry();
                Toast.makeText(getContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();
                //swipeRefreshLayout.setRefreshing(false);
                lnrProgress.setVisibility(View.GONE);
                break;

            case FINISHED:
                //swipeRefreshLayout.setRefreshing(false);
                lnrProgress.setVisibility(View.GONE);
                break;

        }

    }

    @Override
    public void onItemClick(int position, ImageView imageView) {


        DocumentSnapshot user = options.getData().getValue().get(position);
        String selectedKey = user.getId();

        if (!selectedKey.equals(CurrentUserID)) {
            Intent intent = new Intent(getActivity(), UserProfile.class);
            intent.putExtra("key", selectedKey);
            intent.putExtra("pic", user.get("profileimage").toString());
            Bundle bundle = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imageView, imageView.getTransitionName()).toBundle();
            }
            startActivity(intent, bundle);
        } else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new ProfileFrag()).addToBackStack(null).commit();

            MainActivity.bottomNav.setSelectedItemId(R.id.bottomnav_profile);
        }


    }


}
