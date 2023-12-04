package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupFBgoogle extends AppCompatActivity {

    private static final Pattern UserName_PATTERN =
            Pattern.compile("^[\\p{Alnum} ]{3,20}$");

    CircleImageView profileImage;
    EditText usernameTxt;
    private String gender = null;
    ImageView imgGenderM,imgGenderF;
    //firebase global var
    FirebaseAuth mAuth;
    private CollectionReference UserRef;
    String CurrentUserID;
    ProgressBar progressBar;
    TextInputLayout usernameLayoutInput;
    String lookingfor=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_fbgoogle);

        imgGenderM = findViewById(R.id.imgGenderM);
        imgGenderF = findViewById(R.id.imgGenderF);

        usernameLayoutInput = findViewById(R.id.Username_text_input);
        usernameTxt = findViewById(R.id.Username_edit_text);

        profileImage = findViewById(R.id.my_profile_pic);
        progressBar = findViewById(R.id.progress_heart);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseFirestore.getInstance().collection("Users");

        usernameTxt.setText(getIntent().getExtras().getString("name"));
    }

    private boolean validateUsername() {
        String usernameInput = usernameTxt.getText().toString().trim();

        if (usernameInput.isEmpty()) {
            usernameLayoutInput.setError("Field can't be empty!");
            return false;
        }
        else if(!UserName_PATTERN.matcher(usernameInput).matches())
        {
            usernameLayoutInput.setError("- at lease 3 Charachters \n" +
                    "- no special charachter\n" +
                    "- no white space");
            return false;
        }
        else {
            usernameLayoutInput.setError(null);
            return true;
        }

    }




    private boolean valiGender() {

        if (gender==null) {
            Toast.makeText(SignupFBgoogle.this, "PLZ select your Gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }

    }

    public void CreateNewAccount(final View view) {

        if(validateUsername() && valiGender())
        {

            view.setEnabled(false);
            final String username =usernameTxt.getText().toString();

            progressBar.setVisibility(View.VISIBLE);

            CurrentUserID = mAuth.getCurrentUser().getUid();

            HashMap userMap = new HashMap();
            userMap.put("uid", CurrentUserID);
            userMap.put("username", username);
            userMap.put("gender", gender);
            userMap.put("lookingfor", lookingfor);
            userMap.put("latitude", "0.0");
            userMap.put("longitude", "0.0");



            UserRef.document(CurrentUserID).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(SignupFBgoogle.this, "Successfully Created", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    SendUserToSetupActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignupFBgoogle.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    view.setEnabled(true);
                }
            });

        }



    }

    private void SendUserToSetupActivity() {
        startActivity(new Intent(SignupFBgoogle.this,SetupActivity.class));
        finish();
    }


    public void gendermSelect(View view) {
        gender = "Male";
        imgGenderM.setImageResource(R.drawable.gender_male_checked);
        imgGenderF.setImageResource(R.drawable.gender_female);
        lookingfor="Female";
    }

    public void genderfSelect(View view) {

        gender = "Female";
        imgGenderF.setImageResource(R.drawable.gender_female_checked);
        imgGenderM.setImageResource(R.drawable.gender_male);
        lookingfor="Male";


    }


    public void backpressd(View view) {
        finish();
    }


    public void signout(View view) {
        finish();
    }
}
