package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.needlove.Adapters.SliderPagerAdapter;
import com.example.needlove.Model.Slide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    boolean doubleBackToExitPressedOnce = false;
    ImageView imgbg;
    private TranslateAnimation moveDownwards;
    RelativeLayout relativeLayout;
    AnimationDrawable animationDrawable;
    //slider


    //firebase
    private FirebaseAuth mAuth;

    // google sign in
    MaterialButton btnSignup;
    GoogleSignInClient mGoogleSignInClient;


    private DocumentReference UsersRef;
    //facebook Login
    CallbackManager mCallbackManager;
    LoginButton loginButton;
    MaterialButton fblogin;

    LinearLayout lnrProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        relativeLayout = findViewById(R.id.mainRootLayout);

        // Firebase Authantication
        mAuth = FirebaseAuth.getInstance();

        initViews();


        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();


        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile", "user_friends");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("tag", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());


            }

            @Override
            public void onCancel() {
                Log.d("ff", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("fd", "facebook:onError", error);
                Toast.makeText(StartActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                // ...
            }
        });


        //google Sign in
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignup.setOnClickListener(this);

    }

    private void initViews() {

        btnSignup = findViewById(R.id.sign_in_button);
        loginButton = findViewById(R.id.login_button);
        fblogin = findViewById(R.id.fblogin);
        lnrProgress = findViewById(R.id.lnrProgress);


        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);

        btnSignup.startAnimation(myAnim);
        fblogin.startAnimation(myAnim);


    }


    @Override
    protected void onRestart() {
        super.onRestart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            finish();
            startActivity(new Intent(StartActivity.this, MainActivity.class));
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

    public void onclickFacebookButton(View view) {
        if (view == fblogin) {
            loginButton.performClick();
        }

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("dd", "handleFacebookAccessToken:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            CheckRegistration(user);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "signInWithCredential:failure", task.getException());
                            lnrProgress.setVisibility(View.GONE);

                        }

                        // ...
                    }
                });
    }

    private void CheckRegistration(final FirebaseUser user) {

        //get Current UserID

        FirebaseAuth mAuthh = FirebaseAuth.getInstance();
        String CurrentUserId = mAuthh.getCurrentUser().getUid();


        // Root Parent Node "create Users if not Exist"
        UsersRef = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserId);

        UsersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        startActivity(new Intent(StartActivity.this, MainActivity.class));

                        if (document.contains("username")) {
                            SharedPreferences pref;
                            SharedPreferences.Editor editor;
                            pref = getSharedPreferences("userInf", Context.MODE_PRIVATE);
                            //save username into shared
                            editor = pref.edit();
                            editor.putString("username", document.get("username").toString());
                            editor.putString("country", document.get("countrycode").toString());
                            editor.putString("pic", document.get("profileimage").toString());
                            editor.apply();
                        }

                        subscriptionToTopic();
                        finish();


                    } else {
                        Intent intent = new Intent(StartActivity.this, SignupFBgoogle.class);
                        intent.putExtra("name", user.getDisplayName());
                        startActivity(intent);
                        finish();
                    }

                } else {
                    Toast.makeText(StartActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    lnrProgress.setVisibility(View.GONE);
                }

            }
        });
    }

    private void subscriptionToTopic() {

        String CurrentUser = mAuth.getCurrentUser().getUid();
        FirebaseMessaging.getInstance().subscribeToTopic(CurrentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                DocumentReference UserRef = FirebaseFirestore
                        .getInstance()
                        .collection("Users")
                        .document(CurrentUser);


                Map<String,Object> muuser = new HashMap<>();
                muuser.put("login", FieldValue.serverTimestamp());

                UserRef.set(muuser, SetOptions.merge());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StartActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 750) {
            lnrProgress.setVisibility(View.VISIBLE);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account);
                } else {
                    lnrProgress.setVisibility(View.GONE);
                }


            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(StartActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                lnrProgress.setVisibility(View.GONE);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            CheckRegistration(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            //  Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(StartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            lnrProgress.setVisibility(View.GONE);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onClick(View view) {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 750);
    }

    public void loggin(View view) {
        startActivity(new Intent(StartActivity.this, LoginActivity.class));
        finish();
    }

    public void termsActivity(View view) {
        Intent intent = new Intent(StartActivity.this, Privacy_Terms_AboutUs.class);
        intent.putExtra("witch", "terms");
        startActivity(intent);

    }

    public void serviceActivity(View view) {
        Intent intent = new Intent(StartActivity.this, Privacy_Terms_AboutUs.class);
        intent.putExtra("witch", "privacy");
        startActivity(intent);
    }

    public void Signupp(View view) {
        startActivity(new Intent(StartActivity.this,SignUpActivity.class));
    }
}
