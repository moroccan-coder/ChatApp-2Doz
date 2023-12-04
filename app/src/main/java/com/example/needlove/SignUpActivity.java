package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private static final Pattern UserName_PATTERN =
            Pattern.compile("^[\\p{Alnum} ]{3,20}$");

    private static final Pattern Password_PATTERN =
            Pattern.compile("^[\\p{Alnum}@#$%&]{6,30}$");
    CircleImageView profileImage;
    EditText Useremail, Userpassword, UserconformPassword, usernameTxt;
    private String gender = null;
    ImageView imgGenderM,imgGenderF;
    //firebase global var
    FirebaseAuth mAuth;
    private CollectionReference  UserRef;
    String CurrentUserID;
    StorageReference UserProfileImageRef;
   ProgressBar progressBar;
    TextInputLayout usernameLayoutInput,emailLayoutInput,passwordLayoutInput,passwordConfirmLayoutInput;
    String lookingfor=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        imgGenderM = findViewById(R.id.imgGenderM);
        imgGenderF = findViewById(R.id.imgGenderF);


        emailLayoutInput = findViewById(R.id.Email_text_input);
        Useremail = findViewById(R.id.Email_edit_text);
        usernameLayoutInput = findViewById(R.id.Username_text_input);
        usernameTxt = findViewById(R.id.Username_edit_text);
        passwordLayoutInput = findViewById(R.id.password_text_input);
        Userpassword = findViewById(R.id.password_edit_text);
        passwordConfirmLayoutInput = findViewById(R.id.password_confirm_text_input);
        UserconformPassword = findViewById(R.id.password_confirm_edit_text);
        profileImage = findViewById(R.id.my_profile_pic);
        progressBar = findViewById(R.id.progress_heart);


        Useremail.setFocusableInTouchMode(true);
        Useremail.requestFocus();
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseFirestore.getInstance().collection("Users");

        //Create a profile image Folder
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

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

    private boolean validatePassword() {
        String passwordInput = Userpassword.getText().toString().trim();

        if (passwordInput.isEmpty()) {
            passwordLayoutInput.setError("Field can't be empty!");
            return false;
        }
        else if(!Password_PATTERN.matcher(passwordInput).matches())
        {
            passwordLayoutInput.setError("- at lease 6 Charachters\n" +
                    "- Only @#$%& specialCharacter allowed\n" +
                    "- no white space");
            return false;
        }
        else {
            passwordLayoutInput.setError(null);
            return true;
        }

    }


    private boolean validatePasswordConfirm() {
        String passwordConfirmInput = UserconformPassword.getText().toString().trim();

        if (passwordConfirmInput.isEmpty()) {
            passwordConfirmLayoutInput.setError("Field can't be empty!");
            return false;
        }

        if (!passwordConfirmInput.equals(Userpassword.getText().toString())) {
            passwordConfirmLayoutInput.setError("should be the same of your password above!");
            return false;
        }
        else if(!Password_PATTERN.matcher(passwordConfirmInput).matches())
        {
            passwordConfirmLayoutInput.setError("- at lease 6 Charachters\n" +
                    "- Only @#$%& specialCharacter allowed\n" +
                    "- no white space");
            return false;
        }
        else {
            passwordConfirmLayoutInput.setError(null);
            return true;
        }

    }

    private boolean validateEmail() {
        String emailInput = Useremail.getText().toString().trim();

        if (emailInput.isEmpty()) {
            emailLayoutInput.setError("Field can't be empty!");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches())
        {
            emailLayoutInput.setError("Please enter a valid email address!");
            return false;
        }
        else {
            emailLayoutInput.setError(null);
            return true;
        }

    }

    private boolean valiGender() {

        if (gender==null) {
            Toast.makeText(SignUpActivity.this, "PLZ select your Gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }

    }

    public void CreateNewAccount(final View view) {

        if(validateEmail() && validateUsername() && validatePassword() && validatePasswordConfirm() && valiGender())
        {

            view.setEnabled(false);
            String email = Useremail.getText().toString();
            String password = Userpassword.getText().toString();
            final String username =usernameTxt.getText().toString();

            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

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
                                       Toast.makeText(SignUpActivity.this, "Successfully Created", Toast.LENGTH_SHORT).show();
                                       progressBar.setVisibility(View.GONE);
                                       SendUserToSetupActivity();
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       Toast.makeText(SignUpActivity.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                       progressBar.setVisibility(View.GONE);
                                       view.setEnabled(true);
                                   }
                               });


                            } else {
                                progressBar.setVisibility(View.GONE);
                                String message = task.getException().getMessage();
                                Toast.makeText(SignUpActivity.this, "Error :" + message, Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                            }
                        }
                    });

        }



    }

    private void SendUserToSetupActivity() {
        startActivity(new Intent(SignUpActivity.this,SetupActivity.class));
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



}
