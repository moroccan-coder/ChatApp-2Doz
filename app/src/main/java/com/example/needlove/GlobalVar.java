package com.example.needlove;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class GlobalVar extends Application implements LifecycleObserver {

    String Gender = "Female";
    SharedPreferences pref;

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        pref = getSharedPreferences("userInf", Context.MODE_PRIVATE);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {

       if (pref.contains("username")) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {

                String CurrentUserID = currentUser.getUid();
                DocumentReference UserRef = FirebaseFirestore
                        .getInstance()
                        .collection("Users")
                        .document(CurrentUserID);
                UserRef.update("online", false);
                UserRef.update("lastseen", FieldValue.serverTimestamp());

            }


        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        if (pref.contains("username")) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {

                String CurrentUserID = currentUser.getUid();
                DocumentReference UserRef = FirebaseFirestore
                        .getInstance()
                        .collection("Users")
                        .document(CurrentUserID);

                UserRef.update("online", true);

            }

        }



    }
}
