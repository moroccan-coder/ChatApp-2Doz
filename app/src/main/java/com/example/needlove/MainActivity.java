package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;

    private FirebaseAuth mAuth;
    private String CurrentUserId;
    GlobalVar globalVar;
    //Boolean variable to mark if the transaction is safe
    private boolean isTransactionSafe =true;

    //Boolean variable to mark if there is any transaction pending
    private boolean isTransactionPending;
    View badge;

    ListenerRegistration registration;

    public static BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       bottomNav = findViewById(R.id.bottom_navigation);


        bottomNav.setOnNavigationItemSelectedListener(navListener);


        // Firebase Authantication
        mAuth = FirebaseAuth.getInstance();
        globalVar = (GlobalVar) getApplicationContext();

        CurrentUserId = mAuth.getCurrentUser().getUid();

        intialFragment();


        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomNav.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        badge = LayoutInflater.from(MainActivity.this).inflate(R.layout.notification_badge, itemView, true);

        CheckMessages();

    }




    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            Fragment selectedFrag = null;

            switch (menuItem.getItemId()) {
                case R.id.bottomnav_profile:
                    selectedFrag = new ProfileFrag();
                    break;

                case R.id.bottomnav_message:

                    selectedFrag = new MessagesFrag();
                    break;

                case R.id.bottomnav_moments:

                    selectedFrag = new MomentsFrag();
                    break;

                case R.id.bottomnav_swipe:
                    selectedFrag = new NoGpsFrag();

                    break;

                case R.id.bottomnav_discover:
                    selectedFrag = new DiscoverFrag();
                    break;

            }

            getSupportFragmentManager().beginTransaction().disallowAddToBackStack()
                    .replace(R.id.fragment_container, selectedFrag).commit();
            return true;
        }
    };



    private void intialFragment() {

        if (isTransactionSafe) {
            //showing Discover Fragment when the activity loaded

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiscoverFrag()).commit();

        } else {
                        /*
                        If any transaction is not done because the activity is in background. We set the isTransactionPending
                        variable to true so that we can pick this up when we come back to foreground
                        */
            isTransactionPending = true;
        }
    }

    private void CheckMessages() {

        CollectionReference MsgRef;
        MsgRef = FirebaseFirestore.getInstance().collection("Chat").document(CurrentUserId).collection("ChatingUser");

        Query query;

        query = MsgRef
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1);

        registration= query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e==null) {

                    if (!queryDocumentSnapshots.isEmpty() && queryDocumentSnapshots.getDocuments().get(0).contains("msgUnread"))
                    {
                        String increm = queryDocumentSnapshots.getDocuments().get(0).get("msgUnread").toString();

                        if (Integer.parseInt(increm) > 0) {
                            badge.findViewById(R.id.notifications_badge).setVisibility(View.VISIBLE);
                        } else {
                            badge.findViewById(R.id.notifications_badge).setVisibility(View.GONE);
                        }
                    }

                }

            }
        });


    }



    /*
    onPostResume is called only when the activity's state is completely restored. In this we will
    set our boolean variable to true. Indicating that transaction is safe now
    */
    public void onPostResume() {
        super.onPostResume();
        isTransactionSafe = true;

        /* Here after the activity is restored we check if there is any transaction pending from the last restoration
         */
      /*  if (isTransactionPending) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiscoverFrag()).commit();
        }*/
    }
        /*
        onPause is called just before the activity moves to background and also before onSaveInstanceState. In this
        we will mark the transaction as unsafe
        */

    public void onPause() {
        super.onPause();
        isTransactionSafe = false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);


    }

    @Override
    protected void onStop() {
        super.onStop();
        registration.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckMessages();
    }
}
