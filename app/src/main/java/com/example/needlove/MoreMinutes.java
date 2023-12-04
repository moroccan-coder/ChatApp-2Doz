package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MoreMinutes extends AppCompatActivity {
    LinearLayout lnrRateUs, lnrShare;
    View viewRateus, viewShare;


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //firebase
    FirebaseAuth mAuth;
    String CurrentUserId;
    CollectionReference mCollection;

    TextView txtMinutes;
    MaterialButton btnWatchAds;

    LinearLayout progresLove;

    boolean isRate = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_minutes);

        pref = getSharedPreferences("Reward", Context.MODE_PRIVATE);


        lnrRateUs = findViewById(R.id.lnrRateUs);
        txtMinutes = findViewById(R.id.txtMinutes);
        lnrShare = findViewById(R.id.lnrShare);
        btnWatchAds = findViewById(R.id.btnWatchAds);
        viewRateus = findViewById(R.id.viewRateus);
        viewShare = findViewById(R.id.viewShare);
        progresLove = findViewById(R.id.progresLove);


        mAuth = FirebaseAuth.getInstance();

        CurrentUserId = mAuth.getCurrentUser().getUid();

        //Firebase

        mCollection = FirebaseFirestore.getInstance().collection("voiceCall");
    }

    public void back(View view) {
        finish();
    }

    public void watchVide(View view) {
        startActivity(new Intent(MoreMinutes.this, WatchVideoReward.class));
    }

    public void rteus(View view) {

        Uri uri = Uri.parse("market://details?id=" + MoreMinutes.this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + MoreMinutes.this.getPackageName())));
        }

        isRate = true;
    }


    public void shrepp(View view) {

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = "Let me Recommend you the Best Dating App";
            shareMessage = shareMessage + ":\n\n https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivityForResult(Intent.createChooser(shareIntent, "Choose one"), 119);
        } catch (Exception e) {
            //e.toString();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 119) {

            AddMinutesTodb(60000, 2);

        }


    }

    private void AddMinutesTodb(int mills, int vl) {

        Map<String, Object> hashMin = new HashMap<>();
        if (vl == 1) {
            hashMin.put("rate", true);
        } else {
            hashMin.put("share", true);
        }

        mCollection.document(CurrentUserId).set(hashMin, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mCollection.document(CurrentUserId).update("balance_ms", FieldValue.increment(mills));
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        mCollection.document(CurrentUserId).addSnapshotListener(MoreMinutes.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    if (documentSnapshot.getBoolean("rate").booleanValue()) {
                        lnrRateUs.setVisibility(View.GONE);
                        viewRateus.setVisibility(View.GONE);
                    } else {
                        lnrRateUs.setVisibility(View.VISIBLE);
                        viewRateus.setVisibility(View.VISIBLE);
                    }


                    if (documentSnapshot.getBoolean("share").booleanValue()) {
                        lnrShare.setVisibility(View.GONE);
                        viewShare.setVisibility(View.GONE);
                    } else {
                        lnrShare.setVisibility(View.VISIBLE);
                        viewShare.setVisibility(View.VISIBLE);
                    }


                    String Minutes = documentSnapshot.get("balance_ms").toString();
                    int mints =(Integer.parseInt(Minutes) / 1000) / 60;

                    txtMinutes.setText(String.valueOf(mints));

                    btnWatchAds.setVisibility(View.VISIBLE);

                    progresLove.setVisibility(View.GONE);

                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isRate) {
            AddMinutesTodb(180000, 1);

            isRate = false;
        }
    }


}
