package com.example.needlove;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private LinearLayout lnrFriends,lnrMatchers,lnrShare,lnrRate,lnrLogout,lnrAbout;
    Dialog mdDialog;

    FirebaseAuth mAuth;

    GoogleSignInClient mGoogleSignInClient;
    String CurrentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lnrLogout = findViewById(R.id.logoutbtn);
        lnrFriends = findViewById(R.id.lnrFriends);
        lnrMatchers = findViewById(R.id.lnrMatchers);
        lnrShare = findViewById(R.id.lnrShare);
        lnrRate = findViewById(R.id.lnrRate);
        lnrAbout = findViewById(R.id.lnrAbout);

        mdDialog = new Dialog(this);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        lnrLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mdDialog.setContentView(R.layout.logout_confirmation);
                TextView btnYes = mdDialog.findViewById(R.id.btn_yes);
                TextView btnNo = mdDialog.findViewById(R.id.btn_no);


                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                            String CurrentUserID = mAuth.getCurrentUser().getUid();
                            DocumentReference UserRef = FirebaseFirestore
                                    .getInstance()
                                    .collection("Users")
                                    .document(CurrentUserID);


                        Map<String,Object> muuser = new HashMap<>();
                        muuser.put("logout", FieldValue.serverTimestamp());
                        muuser.put("online", false);

                        UserRef.set(muuser, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                try{

                                    mAuth.signOut();
                                    mGoogleSignInClient.signOut();
                                    LoginManager.getInstance().logOut();


                                    Intent intent = new Intent(getApplicationContext(), Splash.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                    mdDialog.dismiss();

                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });




                    }

                });

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mdDialog.dismiss();
                    }
                });


                mdDialog.show();







            }
        });


        lnrFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this,AllFriendActivity.class));
            }
        });

        lnrMatchers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this,AllMatchers.class));
            }
        });


        lnrShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String shareMessage = "Let me Recommend you the Best Dating App";
                    shareMessage = shareMessage + ":\n\n https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        });


        lnrRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + SettingsActivity.this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + SettingsActivity.this.getPackageName())));
                }
            }
        });




        lnrAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this,About.class));
            }
        });
    }

    public void back(View view) {
        finish();
    }
}
