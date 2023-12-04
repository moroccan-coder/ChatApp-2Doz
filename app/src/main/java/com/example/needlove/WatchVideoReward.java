package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WatchVideoReward extends AppCompatActivity {

     RewardedAd Reward1,Reward2,Reward3,Reward4,Reward5;
     ProgressBar progress1,progress2,progress3,progress4,progress5;
     MaterialButton btn1,btn2,btn3,btn4,btn5;
     LinearLayout lnrWaitTimer1,lnrWaitTimer2,lnrWaitTimer3,lnrWaitTimer4,lnrWaitTimer5;

     TextView  txtTimeToActivit1,txtTimeToActivit2,txtTimeToActivit3,txtTimeToActivit4,txtTimeToActivit5;

    //firebase
    FirebaseAuth mAuth;
    String CurrentUserId;
    CollectionReference mCollection;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    TextView txtMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video_reward);

        pref = getSharedPreferences("Reward", Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        CurrentUserId = mAuth.getCurrentUser().getUid();

        //Firebase

        mCollection = FirebaseFirestore.getInstance().collection("voiceCall");



        progress1 = findViewById(R.id.progress1);
        progress2 = findViewById(R.id.progress2);
        progress3 = findViewById(R.id.progress3);
        progress4 = findViewById(R.id.progress4);
        progress5 = findViewById(R.id.progress5);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);


        txtMinutes = findViewById(R.id.txtMinutes);

        lnrWaitTimer1 = findViewById(R.id.lnrWaitTimer1);
        lnrWaitTimer2 = findViewById(R.id.lnrWaitTimer2);
        lnrWaitTimer3 = findViewById(R.id.lnrWaitTimer3);
        lnrWaitTimer4 = findViewById(R.id.lnrWaitTimer4);
        lnrWaitTimer5 = findViewById(R.id.lnrWaitTimer5);
        txtTimeToActivit1 = findViewById(R.id.txtTimeToActivit1);
        txtTimeToActivit2 = findViewById(R.id.txtTimeToActivit2);
        txtTimeToActivit3 = findViewById(R.id.txtTimeToActivit3);
        txtTimeToActivit4 = findViewById(R.id.txtTimeToActivit4);
        txtTimeToActivit5 = findViewById(R.id.txtTimeToActivit5);





            //check save Reward time in Preference

        if (pref.contains("reward1"))
        {
            renew1(null);

        }

        else {
            Reward1 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",1);
        }

        if (pref.contains("reward2"))
        {
            renew2(null);
        }
        else {
            Reward2 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",2);
        }

        if (pref.contains("reward3"))
        {
            renew3(null);
        }

        else {
            Reward3 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",3);
        }

        if (pref.contains("reward4"))
        {
            renew4(null);
        }
        else {
            Reward4 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",4);
        }

        if (pref.contains("reward5"))
        {
            renew5(null);
        }
        else {
            Reward5 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",5);
        }










    }



    public RewardedAd createAndLoadRewardedAd(String adUnitId,int nb) {
        RewardedAd rewardedAd = new RewardedAd(this, adUnitId);
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.

                switch (nb)
                {
                    case 1 :progress1.setVisibility(View.GONE);btn1.setVisibility(View.VISIBLE);lnrWaitTimer1.setVisibility(View.GONE);
                    break;
                    case 2 :progress2.setVisibility(View.GONE);btn2.setVisibility(View.VISIBLE);lnrWaitTimer2.setVisibility(View.GONE);
                        break;
                    case 3 :progress3.setVisibility(View.GONE);btn3.setVisibility(View.VISIBLE);lnrWaitTimer3.setVisibility(View.GONE);
                        break;
                    case 4 :progress4.setVisibility(View.GONE);btn4.setVisibility(View.VISIBLE);lnrWaitTimer4.setVisibility(View.GONE);
                        break;
                    case 5 :progress5.setVisibility(View.GONE);btn5.setVisibility(View.VISIBLE);lnrWaitTimer5.setVisibility(View.GONE);

                }
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }


    public void back(View view) {
        finish();
    }

    public void btn1(View view) {

        if (Reward1.isLoaded())
        {
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                   btn1.setEnabled(false);
                    Toast.makeText(WatchVideoReward.this, "+1 minute", Toast.LENGTH_SHORT).show();
                    AddMinutes();
                    editor = pref.edit();
                    editor.putString("reward1", nextDate());
                    editor.apply();

                    btn1.setVisibility(View.GONE);
                    lnrWaitTimer1.setVisibility(View.VISIBLE);
                    txtTimeToActivit1.setText(" "+"2 hours");

                }

            };

            Reward1.show(WatchVideoReward.this,adCallback);
        }

    }

    public void btn2(View view) {

        if (Reward2.isLoaded())
        {
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    btn2.setEnabled(false);
                    Toast.makeText(WatchVideoReward.this, "+1 minute", Toast.LENGTH_SHORT).show();
                    AddMinutes();
                    editor = pref.edit();
                    editor.putString("reward2",nextDate());
                    editor.apply();

                    btn2.setVisibility(View.GONE);
                    lnrWaitTimer2.setVisibility(View.VISIBLE);
                    txtTimeToActivit2.setText(" "+"2 hours");
                }

            };

            Reward2.show(WatchVideoReward.this,adCallback);
        }
    }

    public void btn3(View view) {
        if (Reward3.isLoaded())
        {
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    btn3.setEnabled(false);
                    Toast.makeText(WatchVideoReward.this, "+1 minute", Toast.LENGTH_SHORT).show();
                    AddMinutes();
                    editor = pref.edit();
                    editor.putString("reward3",nextDate());
                    editor.apply();

                    btn3.setVisibility(View.GONE);
                    lnrWaitTimer3.setVisibility(View.VISIBLE);
                    txtTimeToActivit3.setText(" "+"2 hours");


                }

            };

            Reward3.show(WatchVideoReward.this,adCallback);
        }
    }

    public void btn4(View view) {

        if (Reward4.isLoaded())
        {
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    btn4.setEnabled(false);
                    Toast.makeText(WatchVideoReward.this, "+1 minute", Toast.LENGTH_SHORT).show();
                    AddMinutes();
                    editor = pref.edit();
                    editor.putString("reward4",nextDate());
                    editor.apply();

                    btn4.setVisibility(View.GONE);
                    lnrWaitTimer4.setVisibility(View.VISIBLE);
                    txtTimeToActivit4.setText(" "+"2 hours");
                }

            };

            Reward4.show(WatchVideoReward.this,adCallback);
        }
    }

    public void btn5(View view) {

        if (Reward5.isLoaded())
        {
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    btn5.setEnabled(false);
                    Toast.makeText(WatchVideoReward.this, "+1 minute", Toast.LENGTH_SHORT).show();
                    AddMinutes();

                    editor = pref.edit();
                    editor.putString("reward5",nextDate());
                    editor.apply();

                    btn5.setVisibility(View.GONE);
                    lnrWaitTimer5.setVisibility(View.VISIBLE);
                    txtTimeToActivit5.setText(" "+"2 hours");
                }

            };

            Reward5.show(WatchVideoReward.this,adCallback);
        }
    }

    private void AddMinutes() {

        mCollection.document(CurrentUserId).update("balance_ms", FieldValue.increment(60000));
    }


    //1 minute = 60 seconds
//1 hour = 60 x 60 = 3600
//1 day = 3600 x 24 = 86400
    public void printDifference(Date endDate,int indic) {
        //milliseconds
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Date startDate = null;
        try {
            startDate = currentDate.parse(currentDate.format(calForDate.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long different = endDate.getTime() - startDate.getTime();


        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;



        if (elapsedDays<0 || elapsedHours <0 || elapsedMinutes<0 || elapsedSeconds<0)
        {

            switch (indic)
            {
                case 1 : Reward1 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",1);progress1.setVisibility(View.VISIBLE);
                    break;

                case 2 :Reward2 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",2);progress2.setVisibility(View.VISIBLE);
                    break;

                case 3:Reward3 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",3);progress3.setVisibility(View.VISIBLE);
                    break;

                case 4 :Reward4 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",4);progress4.setVisibility(View.VISIBLE);
                    break;

                case 5:Reward5 = createAndLoadRewardedAd("ca-app-pub-3940256099942544/5224354917",5);progress5.setVisibility(View.VISIBLE);
            }

        }
        else {

             switch (indic)
             {

                 case 1 :

                     progress1.setVisibility(View.GONE);
                     lnrWaitTimer1.setVisibility(View.VISIBLE);
                     if (elapsedHours>0)
                 {
                   txtTimeToActivit1.setText(" "+elapsedHours+" h "+elapsedMinutes+" minutes "+elapsedSeconds+" s");

                 }
                 else if (elapsedMinutes>0)
                 {
                     txtTimeToActivit1.setText(" "+elapsedMinutes+" minutes "+elapsedSeconds+" s");
                 }
                 else {
                         txtTimeToActivit1.setText(" "+elapsedSeconds+" seconds");
                 }

                     break;

                 case 2 :

                     progress2.setVisibility(View.GONE);
                     lnrWaitTimer2.setVisibility(View.VISIBLE);
                     if (elapsedHours>0)
                     {
                         txtTimeToActivit2.setText(" "+elapsedHours+" h "+elapsedMinutes+" minutes "+elapsedSeconds+" s");

                     }
                     else if (elapsedMinutes>0)
                     {
                         txtTimeToActivit2.setText(" "+elapsedMinutes+" minutes "+elapsedSeconds+" s");
                     }
                     else {
                         txtTimeToActivit2.setText(" "+elapsedSeconds+" seconds");
                     }

                     break;

                 case 3 :

                     progress3.setVisibility(View.GONE);
                     lnrWaitTimer3.setVisibility(View.VISIBLE);
                     if (elapsedHours>0)
                     {
                         txtTimeToActivit3.setText(" "+elapsedHours+" h "+elapsedMinutes+" minutes "+elapsedSeconds+" s");

                     }
                     else if (elapsedMinutes>0)
                     {
                         txtTimeToActivit3.setText(" "+elapsedMinutes+" minutes "+elapsedSeconds+" s");
                     }
                     else {
                         txtTimeToActivit3.setText(" "+elapsedSeconds+" seconds");
                     }

                     break;

                 case 4 :

                     progress4.setVisibility(View.GONE);
                     lnrWaitTimer4.setVisibility(View.VISIBLE);
                     if (elapsedHours>0)
                     {
                         txtTimeToActivit4.setText(" "+elapsedHours+" h "+elapsedMinutes+" minutes "+elapsedSeconds+" s");

                     }
                     else if (elapsedMinutes>0)
                     {
                         txtTimeToActivit4.setText(" "+elapsedMinutes+" minutes "+elapsedSeconds+" s");
                     }
                     else {
                         txtTimeToActivit4.setText(" "+elapsedSeconds+" seconds");
                     }

                     break;

                 case 5 :

                     progress5.setVisibility(View.GONE);
                     lnrWaitTimer5.setVisibility(View.VISIBLE);
                     if (elapsedHours>0)
                     {
                         txtTimeToActivit5.setText(" "+elapsedHours+" h "+elapsedMinutes+" minutes "+elapsedSeconds+" s");

                     }
                     else if (elapsedMinutes>0)
                     {
                         txtTimeToActivit5.setText(" "+elapsedMinutes+" minutes "+elapsedSeconds+" s");
                     }
                     else {
                         txtTimeToActivit5.setText(" "+elapsedSeconds+" seconds");
                     }
             }

        }


    }


    public String nextDate()
    {
        Calendar calForDate = Calendar.getInstance();
        calForDate.add(Calendar.MINUTE,3);
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String RegisteredDate = currentDate.format(calForDate.getTime());

        return RegisteredDate;
    }


    public void renew1(View view) {

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String calForDate =  pref.getString("reward1", "");
        try {
            Date endDate = currentDate.parse(calForDate);
            printDifference(endDate,1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public void renew2(View view) {
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String calForDate =  pref.getString("reward2", "");
        try {
            Date endDate = currentDate.parse(calForDate);
            printDifference(endDate,2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void renew3(View view) {
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String calForDate =  pref.getString("reward3", "");
        try {
            Date endDate = currentDate.parse(calForDate);
            printDifference(endDate,3);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void renew4(View view) {
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String calForDate =  pref.getString("reward4", "");
        try {
            Date endDate = currentDate.parse(calForDate);
            printDifference(endDate,4);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void renew5(View view) {

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String calForDate =  pref.getString("reward5", "");
        try {
            Date endDate = currentDate.parse(calForDate);
            printDifference(endDate,5);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        mCollection.document(CurrentUserId).addSnapshotListener(WatchVideoReward.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {

                    String Minutes = documentSnapshot.get("balance_ms").toString();
                    int mints =(Integer.parseInt(Minutes) / 1000) / 60;
                    txtMinutes.setText(String.valueOf(mints));
                }
            }
        });
    }
}
