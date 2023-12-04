package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Splash extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DocumentReference UsersRef;
    private String CurrentUserId;

    GlobalVar globalVar;

    Animation animation;
    ImageView logoo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        logoo = findViewById(R.id.logo);
        animation = AnimationUtils.loadAnimation(this, R.anim.logo_alfa);
        logoo.startAnimation(animation);


        // Firebase Authantication
        mAuth = FirebaseAuth.getInstance();
        globalVar = (GlobalVar) getApplicationContext();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                inLoadinActivity();

            }
        }, 2000);


    }

    private void inLoadinActivity() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToStartActivity();
        } else {

            SharedPreferences pref;
            SharedPreferences.Editor editor;
            pref = getSharedPreferences("discover_filter", Context.MODE_PRIVATE);
            editor = pref.edit();

            if (pref.contains("locationOn")) {
                editor.clear();
                editor.apply();
            }

            CheckUserExictence();


        }
    }


    private void SendUserToSetupActivity() {
        startActivity(new Intent(Splash.this, SetupActivity.class));
        finish();
    }

    public void SendUserToStartActivity() {
        startActivity(new Intent(Splash.this, StartActivity.class));
        finish();
    }


    private void CheckUserExictence() {


        //get Current UserID
        CurrentUserId = mAuth.getCurrentUser().getUid();

        // Root Parent Node "create Users if not Exist"
        UsersRef = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserId);

        UsersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.contains("country")) {

                                if (document.get("gender").toString().equals("Male")) {
                                    globalVar.setGender("Female");
                                } else {
                                    globalVar.setGender("Male");
                                }

                                sendUserToMainActivity();


                        } else {
                            SendUserToSetupActivity();
                        }
                    } else {
                        SendUserToSetupActivity();
                    }
                } else {
                    Toast.makeText(Splash.this, "Error Retrieve Data", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(Splash.this, MainActivity.class));
        finish();
    }
}
