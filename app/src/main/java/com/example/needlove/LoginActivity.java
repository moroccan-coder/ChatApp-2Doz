package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    Drawable img;
ProgressBar progressBar;
    TextInputLayout textInputEmail;
    EditText UserEmail, UserPassword;
    //firebase
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        img = getBaseContext().getResources().getDrawable(R.drawable.ic_close_black_24dp);

        textInputEmail = findViewById(R.id.Login_Email_text_input);
        UserEmail = findViewById(R.id.Login_Email_edit_text);
        UserPassword = findViewById(R.id.Login_password_edit_text);

        progressBar = findViewById(R.id.progress_heart);

        UserEmail.setOnTouchListener(onTouchListener);

        mAuth = FirebaseAuth.getInstance();



    }


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_TOP = 1;
        final int DRAWABLE_RIGHT = 2;
        final int DRAWABLE_BOTTOM = 3;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int leftEdgeOfRightDrawable = UserEmail.getRight()
                        - UserEmail.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                // when EditBox has padding, adjust leftEdge like
                // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                if (event.getRawX() >= leftEdgeOfRightDrawable) {
                    // clicked on clear icon
                    UserEmail.setText("");
                    return true;
                }
            }
            return false;
        }
    };


    private boolean validateEmail() {
        String emailInput = UserEmail.getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Field can't be empty!");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches())
        {
            textInputEmail.setError("Please enter a valid email address!");
            return false;
        }
        else {
             textInputEmail.setError(null);
             return true;
        }

    }


    public void backpressd(View view) {
        finish();
    }


    public void forget_password_dialog(View view) {

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.forget_password, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();


        dialogView.findViewById(R.id.dismis_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.forget_pass_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Send...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void loginto(View view) {
        if (validateEmail())
        {
            String email = UserEmail.getText().toString();
            String password = UserPassword.getText().toString();

                // ProgressDialog
               progressBar.setVisibility(View.VISIBLE);

                // End ProgressDialog

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    SendUserToMainActivity();
                                    subscriptionToTopic();
                                } else {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(LoginActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                   progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

        }
    }

    private void subscriptionToTopic() {

        String CurrentUser = mAuth.getCurrentUser().getUid();
        FirebaseMessaging.getInstance().subscribeToTopic(CurrentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(LoginActivity.this, "Subscrib To Topic", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SendUserToMainActivity() {

        String CurrentUserID;
        DocumentReference UserRef;

        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserID);


        UserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.contains("username"))
                {
                    SharedPreferences pref;
                    SharedPreferences.Editor editor;
                    pref = getSharedPreferences("userInf", Context.MODE_PRIVATE);
                    //save username into shared
                    editor = pref.edit();
                    editor.putString("username", documentSnapshot.get("username").toString());
                    editor.putString("country", documentSnapshot.get("countrycode").toString());
                    editor.putString("pic", documentSnapshot.get("profileimage").toString());
                    editor.apply();
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                }



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });





    }


}
