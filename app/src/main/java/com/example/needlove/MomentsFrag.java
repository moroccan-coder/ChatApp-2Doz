package com.example.needlove;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.hbb20.CountryCodePicker;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MomentsFrag extends Fragment {


    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 189;
    SinchClient sinchClient;
    CallClient callClient;
    Button BtnStart, BtnStop;
    LottieAnimationView earhAnim;
    LottieAnimationView progressAnim;
    ImageView call_settinngs;
    ProgressBar progressMinutes;
    Dialog mdDialog;
    Call call;

    ImageView imgCalHistory;


    CircleImageView img_user;
    boolean isplaying = false;


    //firebase
    FirebaseAuth mAuth;
    String CurrentUserId;
    CollectionReference mCollection, mCollectionCalls;
    ListenerRegistration registration;
    boolean userWait = false;

    //pref
    String usernamePref;
    String pic;
    String country;


    //views
    LinearLayout lnrTop, dotted_line, lnrBtnHangaoutCall, rewardLnt;
    FloatingActionButton btnHanginCall;
    CircleImageView img_other_user;
    Chronometer chronometer;
    boolean chnrono_runing;
    long PauseOffset = 0;
    CountryCodePicker ccp;
    TextView OtherUserName;

    SharedPreferences PrefSettings;

    Query baseQuery = null;
    GlobalVar globalVar;

    String Gender = "Female";


    //other User
    String OtherUsername;
    String OtherPic;
    String OtherCountry;
    String OtherID;

    Button btnMinuts;

    TextView txtMinutes, txtSecondes, txtS;


    //timer
    TextView mTextviewCountDown;
    private CountDownTimer mCountDownTimer;
    boolean mTimerRunning;
    private long mTimeLeftInMillis = 0;

    //

    private boolean isOnStop = false;

    boolean enableCall = false;

    ImageView img_speaker;
    boolean speakerOn = false;
    //check if screen is ON
    PowerManager pm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_moments, container, false);

        BtnStart = view.findViewById(R.id.btnStart);
        OtherUserName = view.findViewById(R.id.OtherUserName);
        ccp = view.findViewById(R.id.ccp);
        chronometer = view.findViewById(R.id.chronometer);
        img_other_user = view.findViewById(R.id.img_other_user);
        rewardLnt = view.findViewById(R.id.rewardLnt);
        img_speaker = view.findViewById(R.id.img_speaker);
        txtMinutes = view.findViewById(R.id.txtBalance);
        txtSecondes = view.findViewById(R.id.txtSecondes);
        txtS = view.findViewById(R.id.txtS);
        btnHanginCall = view.findViewById(R.id.btnHanginCall);
        progressMinutes = view.findViewById(R.id.progressMinutes);
        dotted_line = view.findViewById(R.id.dotted_line);
        lnrBtnHangaoutCall = view.findViewById(R.id.lnrBtnHangaoutCall);
        lnrTop = view.findViewById(R.id.lnrTop);
        BtnStop = view.findViewById(R.id.btnStop);
        earhAnim = view.findViewById(R.id.earhAnim);
        btnMinuts = view.findViewById(R.id.btnMinuts);
        progressAnim = view.findViewById(R.id.progressAnim);
        img_user = view.findViewById(R.id.img_user);
        call_settinngs = view.findViewById(R.id.call_settinngs);
        imgCalHistory = view.findViewById(R.id.imgCalHistory);
        //timer
        mTextviewCountDown = view.findViewById(R.id.text_view_countdown);


        globalVar = (GlobalVar) getActivity().getApplicationContext();
        Gender = globalVar.getGender();

        mdDialog = new Dialog(getContext());


        //Firebase

        mCollection = FirebaseFirestore.getInstance().collection("voiceCall");


        // Instantiate a SinchClient using the SinchClientBuilder.
        mAuth = FirebaseAuth.getInstance();

        CurrentUserId = mAuth.getCurrentUser().getUid();


        mCollectionCalls = mCollection.document(CurrentUserId).collection("CallHistory");


        // get Preference

        // Pref Call Settings
        PrefSettings = getActivity().getSharedPreferences("call_filter", Context.MODE_PRIVATE);

        if (PrefSettings.contains("country_code")) {
            switch (PrefSettings.getInt("filter_cat", 0)) {
                case 1:
                    baseQuery = mCollection
                            .whereEqualTo("type", "free")
                            .whereEqualTo("gender", PrefSettings.getString("looking_for", "Female"))
                            .orderBy("time", Query.Direction.ASCENDING)
                            .limit(1);

                    break;

                case 2:
                    baseQuery = mCollection
                            .whereEqualTo("type", "free")
                            .whereEqualTo("country", PrefSettings.getString("country", "morocco"))
                            .orderBy("time", Query.Direction.ASCENDING)
                            .limit(1);

                    break;

                case 3:
                    baseQuery = mCollection
                            .whereEqualTo("type", "free")
                            .whereEqualTo("gender", PrefSettings.getString("looking_for", "Female"))
                            .whereEqualTo("country", PrefSettings.getString("country", "morocco"))
                            .orderBy("time", Query.Direction.ASCENDING)
                            .limit(1);
            }
        } else {
            baseQuery = mCollection
                    .whereEqualTo("type", "free")
                    .orderBy("time", Query.Direction.ASCENDING)
                    .limit(1);

        }


        // Pref User Info
        SharedPreferences pref = getActivity().getSharedPreferences("userInf", Context.MODE_PRIVATE);


        if (pref.contains("username")) {
            usernamePref = pref.getString("username", "User");
            pic = pref.getString("pic", "http://");
            country = pref.getString("country", "US");
        } else {
            usernamePref = "User";
            pic = "http://";
            country = "US";
        }


        Picasso.get().load(pic).placeholder(R.drawable.avatar_prf).into(img_user);


        checkAndRequestPermissions();


        sinchClient = Sinch.getSinchClientBuilder()
                .context(getActivity())
                .userId(CurrentUserId)
                .applicationKey("40f42302-7cf9-482f-b6f7-118ac5a02571")
                .applicationSecret("DLzVcOQ4N0+ttcHYE0kjtA==")
                .environmentHost("clientapi.sinch.com")
                .build();


        sinchClient.setSupportCalling(true);

        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener() {

                                                          }


        );


        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkAndRequestPermissions()) {
                    StartCallBtn();
                }

            }
        });


        BtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StoptLisener();

                progressAnim.cancelAnimation();
                earhAnim.cancelAnimation();

                BtnStop.setVisibility(View.GONE);
                BtnStart.setVisibility(View.VISIBLE);

            }
        });


        btnHanginCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call.hangup();
                if (mCountDownTimer!=null)
                {
                    pauseTimer();
                }

            }
        });


        call_settinngs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getActivity(), RandomCallSettings.class));
                btnAnim(call_settinngs);

            }
        });


        imgCalHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), RandomCallHistoriq.class));
                btnAnim(imgCalHistory);
            }
        });

        btnMinuts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MoreMinutes.class));
            }
        });

        img_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!speakerOn) {
                    sinchClient.getAudioController().enableSpeaker();
                    img_speaker.setImageResource(R.drawable.ic_speaker_on_24dp);
                    speakerOn = true;
                } else {
                    sinchClient.getAudioController().disableSpeaker();
                    img_speaker.setImageResource(R.drawable.ic_speaker_off_24dp);
                    speakerOn = false;
                }
            }
        });


        //check screen ON
        pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);

        return view;
    }


    //timer
    private void startTimer() {

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {


                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;

                if (call != null) {
                    mCollection.document(CurrentUserId).update("balance_ms", 0);
                    if (getActivity() != null) {
                        getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
                    }
                    call.hangup();
                    refreshFragment();

                }




            }
        }.start();

        mTimerRunning = true;

    }

    private void pauseTimer() {


        mCountDownTimer.cancel();
        mTimerRunning = false;


    }


    public void StartCallBtn() {
        if (enableCall) {
            StartLisener();
            progressAnim.playAnimation();
            earhAnim.playAnimation();
            BtnStop.setVisibility(View.VISIBLE);
            BtnStart.setVisibility(View.GONE);
        } else {
            dialogMinutes();
        }
    }


    private boolean checkAndRequestPermissions() {

        int permissionWriteStorage = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        int permissionReadStorage = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || grantResults == null) {
            /*If result is null*/
        } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            /*If We accept permission*/
            Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
            StartCallBtn();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            /*If We Decline permission*/
            Toast.makeText(getActivity(), "Permission Declined", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetTimer() {

        updateCountDownText();

    }


    private void updateCountDownText() {


        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        mTextviewCountDown.setText(timeLeftFormatted);

    }

    private void StoptLisener() {
        sinchClient.getAudioController().disableSpeaker();
        mCollection.document(CurrentUserId).update("type", "busy");
        if (userWait) {
            registration.remove();
        }

    }

    // this lisener will be start when click the button start to shearch for free user voice call
    private void StartLisener() {


        baseQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    // call the free user
                    if (call == null) {
                        call = sinchClient.getCallClient().callUser(queryDocumentSnapshots.getDocuments().get(0).getId());
                        call.addCallListener(new SinchCallListener());
                        if (getActivity()!=null)
                        {
                            getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                        }

                        //openCallDialog(call);

                        // Picasso.get().load(queryDocumentSnapshots.getDocuments().get(0).get("pic").toString()).placeholder(R.drawable.avatar_prf).into(img_other_user);
                        // ccp.setCountryForNameCode(queryDocumentSnapshots.getDocuments().get(0).get("country").toString());
                        // OtherUserName.setText(queryDocumentSnapshots.getDocuments().get(0).get("username").toString());

                    }


                } else {
                    AddmyIdToWaitList();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void AddmyIdToWaitList() {

        Map<String, Object> mMap = new HashMap<>();
        mMap.put("type", "free");
        mMap.put("time", FieldValue.serverTimestamp());

        mCollection.document(CurrentUserId).set(mMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //startWait Lisener for Free user to call
                WaitingLisener();

            }
        });
    }

    private void WaitingLisener() {

        userWait = true;


        registration = baseQuery.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty() && !queryDocumentSnapshots.getDocuments().get(0).getId().equals(CurrentUserId)) {

                            // call the free user
                            if (call == null) {
                                call = sinchClient.getCallClient().callUser(queryDocumentSnapshots.getDocuments().get(0).getId());
                                call.addCallListener(new SinchCallListener());
                                if (getActivity()!=null)
                                {
                                    getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                                }


                                StoptLisener();
                                //openCallDialog(call);

                                // Picasso.get().load(queryDocumentSnapshots.getDocuments().get(0).get("pic").toString()).placeholder(R.drawable.avatar_prf).into(img_other_user);
                                // ccp.setCountryForNameCode(queryDocumentSnapshots.getDocuments().get(0).get("country").toString());
                                // OtherUserName.setText(queryDocumentSnapshots.getDocuments().get(0).get("username").toString());

                            }
                        }
                    }

                });
    }


    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(Call call) {
        }

        @Override
        public void onCallEstablished(Call call) {
            // Toast.makeText(getActivity(), "Call Established", Toast.LENGTH_SHORT).show();
            mCollection.document(CurrentUserId).update("type", "busy");
            if (getActivity() != null) {
                getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            }


            progressAnim.cancelAnimation();
            earhAnim.cancelAnimation();
            rewardLnt.setVisibility(View.GONE);
            BtnStop.setVisibility(View.GONE);

            lnrBtnHangaoutCall.setVisibility(View.VISIBLE);
            lnrTop.setVisibility(View.VISIBLE);
            mTextviewCountDown.setVisibility(View.VISIBLE);
            img_other_user.setVisibility(View.VISIBLE);
            dotted_line.setVisibility(View.VISIBLE);

            BtnStop.setVisibility(View.GONE);
            earhAnim.setVisibility(View.GONE);
            earhAnim.cancelAnimation();
            progressAnim.setVisibility(View.GONE);
            progressAnim.cancelAnimation();
            startChronometre();
            //timer
            updateCountDownText();
            startTimer();


            mCollection.document(call.getRemoteUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    OtherID = documentSnapshot.getId();
                    OtherCountry = documentSnapshot.get("country").toString();
                    OtherPic = documentSnapshot.get("pic").toString();
                    OtherUsername = documentSnapshot.get("username").toString();

                    Picasso.get().load(OtherPic).placeholder(R.drawable.avatar_prf).into(img_other_user);
                    ccp.setCountryForNameCode(OtherCountry);
                    OtherCountry = ccp.getSelectedCountryName();
                    OtherUserName.setText(OtherUsername);


                }
            });


        }

        @Override
        public void onCallEnded(Call endedCall) {
            //  Toast.makeText(getActivity(), "Call Ended", Toast.LENGTH_SHORT).show();
            call = null;
            endedCall.hangup();
            StoptLisener();
            //timer
            mCollection.document(CurrentUserId).update("balance_ms", mTimeLeftInMillis);


            String chronometerValue = chronometer.getText().toString();

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            PauseOffset = 0;
            if (mCountDownTimer !=null)
            {
                pauseTimer();
            }


            if (getActivity() != null) {
                getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            }


            progressAnim.cancelAnimation();
            earhAnim.cancelAnimation();
            lnrBtnHangaoutCall.setVisibility(View.GONE);
            lnrTop.setVisibility(View.GONE);
            mTextviewCountDown.setVisibility(View.GONE);
            dotted_line.setVisibility(View.GONE);
            img_other_user.setVisibility(View.GONE);

            BtnStart.setVisibility(View.VISIBLE);
            earhAnim.setVisibility(View.VISIBLE);
            progressAnim.setVisibility(View.VISIBLE);
            BtnStop.setVisibility(View.GONE);
            BtnStart.setVisibility(View.VISIBLE);

            if (!chronometerValue.equals("00:00")) {
                addToCallHistory(chronometerValue);
            }


            refreshFragment();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    private void addToCallHistory(String chronometerValue) {

        Map<String, Object> callMap = new HashMap<>();

        callMap.put("uid", OtherID);
        callMap.put("username", OtherUsername);
        callMap.put("pic", OtherPic);
        callMap.put("country", OtherCountry);
        callMap.put("callingtime", chronometerValue);
        callMap.put("time", FieldValue.serverTimestamp());

        mCollectionCalls.add(callMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            call.answer();
            call.addCallListener(new SinchCallListener());

        }
    }


    @Override
    public void onStop() {
        super.onStop();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (pm.isInteractive()) {
                StoptLisener();
                if (call != null) {
                    mCollection.document(CurrentUserId).update("balance_ms", mTimeLeftInMillis);
                    call.hangup();

                    if (getActivity() != null) {
                        getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
                    }

                }

                isOnStop = true;
            }
        } else {
            StoptLisener();
            if (call != null) {
                mCollection.document(CurrentUserId).update("balance_ms", mTimeLeftInMillis);
                call.hangup();

                if (getActivity() != null) {
                    getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
                }

            }

            isOnStop = true;
        }

        if (mCountDownTimer !=null)
        {
            pauseTimer();
        }


    }


    @Override
    public void onStart() {
        super.onStart();


        mCollection.document(CurrentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {

                    Map<String, Object> mMap = new HashMap<>();
                    mMap.put("type", "busy");
                    mMap.put("time", FieldValue.serverTimestamp());
                    mMap.put("username", usernamePref);
                    mMap.put("pic", pic);
                    mMap.put("country", country);
                    mMap.put("gender", Gender);
                    mMap.put("balance_ms", 600000);
                    mMap.put("rate", false);
                    mMap.put("share", false);

                    mCollection.document(CurrentUserId).set(mMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            txtMinutes.setText("10");
                            progressMinutes.setVisibility(View.GONE);
                            txtMinutes.setVisibility(View.VISIBLE);
                            btnMinuts.setVisibility(View.VISIBLE);
                            enableCall = true;
                            mTimeLeftInMillis = 600000;
                            updateCountDownText();
                        }
                    });

                } else {

                    if (!documentSnapshot.get("country").toString().equals(country)) {
                        mCollection.document(CurrentUserId).update("country", country);
                    }

                    String millsec = documentSnapshot.get("balance_ms").toString();
                    mTimeLeftInMillis = Long.parseLong(millsec);
                    updateCountDownText();
                    int minutes = getMinutes(Integer.parseInt(millsec));
                    int secnd = getSeconds(Integer.parseInt(millsec));

                    if (minutes < 1 && secnd < 1) {
                        enableCall = false;
                    } else {
                        enableCall = true;
                    }

                    if (minutes > 0) {
                        if (secnd > 0) {
                            txtMinutes.setText(String.valueOf(minutes));
                            txtSecondes.setText(String.valueOf(secnd));

                            txtS.setVisibility(View.VISIBLE);
                            txtSecondes.setVisibility(View.VISIBLE);
                        } else {
                            txtMinutes.setText(String.valueOf(minutes));
                            txtS.setVisibility(View.GONE);
                            txtSecondes.setVisibility(View.GONE);
                        }
                    } else {
                        if (secnd > 0) {
                            txtMinutes.setText("0");
                            txtSecondes.setText(String.valueOf(secnd));
                            txtS.setVisibility(View.VISIBLE);
                            txtSecondes.setVisibility(View.VISIBLE);
                        } else {
                            txtMinutes.setText("0");
                        }
                    }

                    progressMinutes.setVisibility(View.GONE);
                    txtMinutes.setVisibility(View.VISIBLE);
                    btnMinuts.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private int getSeconds(int milliss) {

        return (milliss / 1000) % 60;
    }

    private int getMinutes(int milliss) {

        return (milliss / 1000) / 60;
    }


    void startChronometre() {
        chronometer.setBase(SystemClock.elapsedRealtime() - PauseOffset);
        chronometer.start();
        chnrono_runing = true;

    }


    void btnAnim(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        view.startAnimation(myAnim);
    }




    void refreshFragment() {

        if (!isOnStop) {
            FragmentTransaction ft;
            if (getFragmentManager() != null) {
                ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new MomentsFrag()).addToBackStack(null).commit();
            }

        }

        if (mCountDownTimer!=null)
        {
            mCountDownTimer.cancel();
            mTimerRunning = false;
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        if (isOnStop) {
            isOnStop = false;
            refreshFragment();
        }
    }

    void dialogMinutes() {
        mdDialog.setContentView(R.layout.dialog_minutes);
        TextView txt_close = mdDialog.findViewById(R.id.txt_close);
        Button btnFreeMinutes = mdDialog.findViewById(R.id.btnFreeMinutes);

        txt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdDialog.dismiss();
            }
        });

        btnFreeMinutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MoreMinutes.class));
                mdDialog.dismiss();
            }
        });

        mdDialog.show();
    }
}
