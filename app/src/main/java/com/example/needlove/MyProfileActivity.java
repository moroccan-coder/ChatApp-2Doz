package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileActivity extends AppCompatActivity {

    private static final Pattern UserName_PATTERN =
            Pattern.compile("^[\\p{Alnum}]{3,20}$");
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 833;

    ProgressBar progress_heart_profile, progress_heart;
    LinearLayout date_picker_layout;
    ImageView updown;
    EditText userName, userDOB, userAboutme;
    TextInputLayout Username_profile_text_input;
    RadioGroup radioGroup_gender, radioGroup_lookingfor;
    RadioButton radio_male, radio_female, radio_lookingforman, radio_lookingforwoman, radio_lookingforboth;
    ImageView img_Rin, img_Rsingle, img_Rcomp;
    Spinner spinnerDay, spinnerMonth, spinnerYear;
    String day = null, month = null, year = null;
    CountryCodePicker ccp;
    private String RelationShip = null;
    ChipGroup chipGroup;
    CircleImageView my_profile_pic;
    boolean profileimgCheck = false;
    int UserAge = 18;
    int ageCat = 0;
    String countrySelectName = null;
    GlobalVar globalVar;

    //chip Arraylist
    ArrayList<String> myinterstChip;

    //firebase
    FirebaseAuth mAuth;
    StorageReference UserProfileImageRef;
    String CurrentUserID;
    private DocumentReference UserRef;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        globalVar = (GlobalVar) getApplicationContext();
        pref = getSharedPreferences("userInf", Context.MODE_PRIVATE);
        //firebase intialise
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = UserRef = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserID);
        //Create a profile image Folder
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        radioGroup_gender = findViewById(R.id.radioGroup_gender);
        radioGroup_lookingfor = findViewById(R.id.radioGroup_lookingfor);
        radio_male = findViewById(R.id.gender_male);
        radio_female = findViewById(R.id.gender_female);
        radio_lookingforman = findViewById(R.id.lookingfor_man);
        radio_lookingforwoman = findViewById(R.id.lookingfor_woman);
        radio_lookingforboth = findViewById(R.id.lookingforboth);
        chipGroup = findViewById(R.id.chipsGroup);
        myinterstChip = new ArrayList<>();

        progress_heart_profile = findViewById(R.id.progress_heart_profile);
        progress_heart = findViewById(R.id.progress_heart);
        date_picker_layout = findViewById(R.id.date_picker_layout);
        updown = findViewById(R.id.up_down);
        Username_profile_text_input = findViewById(R.id.Username_profile_text_input);
        userName = findViewById(R.id.Username_profile_edit_text);
        userDOB = findViewById(R.id.birth_profile_edit_text);
        userAboutme = findViewById(R.id.aboume_profile_edit_text);
        img_Rin = findViewById(R.id.imgV_relation_in);
        img_Rsingle = findViewById(R.id.imgV_relation_single);
        img_Rcomp = findViewById(R.id.imgV_relation_comp);
        my_profile_pic = findViewById(R.id.my_profile_pic);
        spinnerDay = findViewById(R.id.day_spinner);
        spinnerMonth = findViewById(R.id.month_spinner);
        spinnerYear = findViewById(R.id.year_spinner);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.raleway_medium);
        ccp.setTypeFace(typeface);


        spinnerYearIntialis();
        spinnerDayIntialis();
        spinnerMonthIntialis();

//we add "this" to block eventlistener work on background when we leave the activity
        UserRef.addSnapshotListener(MyProfileActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Toast.makeText(MyProfileActivity.this, "Error While Loading", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot.exists()) {

                   /* String title = documentSnapshot.getString(KEY_TITLE);
                    String description = documentSnapshot.getString(KEY_DESCRIPTION);

                     */
                    //  Map<String,Object> note  = documentSnapshot.getData();

                    String image = documentSnapshot.get("profileimage").toString();

                    //save pic into shared
                    editor = pref.edit();
                    editor.putString("pic",image);
                    editor.apply();

                    //using picasso lib to show the  image
                    Picasso.get().load(image).placeholder(R.drawable.avatar_prf).into(my_profile_pic);
                    profileimgCheck = true;


                    String myUserName = documentSnapshot.get("username").toString();
                    String Gender = documentSnapshot.get("gender").toString();
                    UserAge = Integer.parseInt(documentSnapshot.get("age").toString());
                    String myDOB = documentSnapshot.get("dateofbirth").toString();
                    String relationShip = documentSnapshot.get("relationship").toString();
                    String lookingfor = documentSnapshot.get("lookingfor").toString();
                    String myinterestt = documentSnapshot.get("myinterest").toString();
                    String aboutme = documentSnapshot.get("aboutme").toString();


                    getgenderfromDb(Gender);
                    getlookingfor(lookingfor);
                    getrelationfromdb(relationShip);
                    getDOBfromdb(myDOB);
                    getinterestfromDb(myinterestt);
                    userName.setText(myUserName);
                    userAboutme.setText(aboutme);
                    userDOB.setText(myDOB);

                } else {

                }

            }
        });



        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year = adapterView.getSelectedItem().toString();
                if (validdate()) {
                    userDOB.setText(day + "-" + month + "-" + year);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                day = adapterView.getSelectedItem().toString();
                if (validdate()) {
                    userDOB.setText(day + "-" + month + "-" + year);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                month = adapterView.getSelectedItem().toString();
                if (validdate()) {
                    userDOB.setText(day + "-" + month + "-" + year);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //Chip

        for (int i = 0; i < chipGroup.getChildCount(); i++) {

            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        myinterstChip.add(compoundButton.getText().toString());
                    } else {
                        myinterstChip.remove(compoundButton.getText().toString());
                    }

                }
            });


        }


    }

    public void profImage(View view) {

        if (checkAndRequestPermissions()) {
            OpenGallery();
        }


    }

    void OpenGallery() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2, 3)
                .setActivityTitle("My Crop")
                //.setCropMenuCropButtonTitle("Done")
                // .setRequestedSize(400, 400)
                .start(this);

    }


    private boolean checkAndRequestPermissions() {

        int permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
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
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            OpenGallery();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            /*If We Decline permission*/
            Toast.makeText(this, "Permission Declined", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                progress_heart_profile.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();
                resultUri = getImageUri(resultUri);

                // Save the imageProfile into Storage Profile Image Folder
                final StorageReference fielPath = UserProfileImageRef.child(CurrentUserID + ".jpg");


                fielPath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fielPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();

                                Map<String, Object> imgval = new HashMap<>();
                                imgval.put("profileimage", downloadUrl);

                                //save pic into shared
                                editor = pref.edit();
                                editor.putString("pic",downloadUrl);
                                editor.apply();

                                UserRef.set(imgval, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progress_heart_profile.setVisibility(View.GONE);

                                        profileimgCheck = true;
                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.avatar_prf).into(my_profile_pic);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MyProfileActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        progress_heart.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                    }
                });

            } else {
                Toast.makeText(MyProfileActivity.this, "Error: Image can be Cropped ,try Again!", Toast.LENGTH_SHORT).show();
                progress_heart_profile.setVisibility(View.GONE);
            }
        }
    }

    private Uri getImageUri(Uri inImage) {


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Bitmap bitmap = null;
        String path = null;

        bitmap = getBitmap(inImage);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        path = MediaStore.Images.Media.insertImage(MyProfileActivity.this.getContentResolver(), bitmap,
                "profile" + System.currentTimeMillis(), null);

        return Uri.parse(path);
    }

    private Bitmap getBitmap(Uri path) {
        ContentResolver mContentResolver = this.getContentResolver();
        Uri uri = path;
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1000000; // 1MP
            in = mContentResolver.openInputStream(uri);

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();


            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }


            Bitmap resultBitmap = null;
            in = mContentResolver.openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                resultBitmap = BitmapFactory.decodeStream(in, null, options);

                // resize to desired dimensions
                int height = resultBitmap.getHeight();
                int width = resultBitmap.getWidth();


                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x,
                        (int) y, true);
                resultBitmap.recycle();
                resultBitmap = scaledBitmap;

                System.gc();
            } else {
                resultBitmap = BitmapFactory.decodeStream(in);
            }
            in.close();


            return resultBitmap;
        } catch (IOException e) {

            return null;
        }


    }

    public void rSingle(View view) {
        RelationShip = "Single";
        img_Rsingle.setBackgroundResource(R.drawable.status_bg_active);
        img_Rin.setBackgroundResource(R.drawable.status_bg_inactive);
        img_Rcomp.setBackgroundResource(R.drawable.status_bg_inactive);
    }

    public void Rinrela(View view) {
        RelationShip = "In RelationShip";
        img_Rin.setBackgroundResource(R.drawable.status_bg_active);
        img_Rsingle.setBackgroundResource(R.drawable.status_bg_inactive);
        img_Rcomp.setBackgroundResource(R.drawable.status_bg_inactive);
    }

    public void RCompl(View view) {

        RelationShip = "Complicated";
        img_Rcomp.setBackgroundResource(R.drawable.status_bg_active);
        img_Rin.setBackgroundResource(R.drawable.status_bg_inactive);
        img_Rsingle.setBackgroundResource(R.drawable.status_bg_inactive);

    }


    void spinnerYearIntialis() {
        ArrayList<String> years = new ArrayList<String>();
        years.add("Year");
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        thisYear = thisYear - 16;
        for (int i = thisYear; i >= 1900; i--) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.color_spinner_layout, years);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        spinnerYear.setAdapter(adapter);
    }

    void spinnerDayIntialis() {
        ArrayList<String> Days = new ArrayList<String>();
        Days.add("Day");
        for (int i = 1; i <= 31; i++) {
            Days.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.color_spinner_layout, Days);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        spinnerDay.setAdapter(adapter);
    }

    void spinnerMonthIntialis() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.born_month, R.layout.color_spinner_layout);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        // Apply the adapter to the spinner
        spinnerMonth.setAdapter(adapter);
    }


    //gender radio button get from database
    void getgenderfromDb(String gendeeer) {

        if (gendeeer.equals("Male")) {
            radio_male.setChecked(true);
        } else {
            radio_female.setChecked(true);
        }
    }


    private void getlookingfor(String lookingfor) {
        if (lookingfor.equals("Male")) {
            radio_lookingforman.setChecked(true);
        } else if (lookingfor.equals("Female")) {
            radio_lookingforwoman.setChecked(true);
        } else {
            radio_lookingforboth.setChecked(true);
        }

    }


    //get relationShip from db
    void getrelationfromdb(String relattion) {

        if (relattion.equals("Single")) {
            RelationShip = "Single";
            img_Rsingle.setBackgroundResource(R.drawable.status_bg_active);
            img_Rin.setBackgroundResource(R.drawable.status_bg_inactive);
            img_Rcomp.setBackgroundResource(R.drawable.status_bg_inactive);
        } else if (relattion.equals("In RelationShip")) {
            RelationShip = "In RelationShip";
            img_Rin.setBackgroundResource(R.drawable.status_bg_active);
            img_Rsingle.setBackgroundResource(R.drawable.status_bg_inactive);
            img_Rcomp.setBackgroundResource(R.drawable.status_bg_inactive);
        } else if (relattion.equals("Complicated")) {
            RelationShip = "Complicated";
            img_Rin.setBackgroundResource(R.drawable.status_bg_inactive);
            img_Rsingle.setBackgroundResource(R.drawable.status_bg_inactive);
            img_Rcomp.setBackgroundResource(R.drawable.status_bg_active);
        }


    }

    //get DOB from db
    private void getDOBfromdb(String myDOB) {

    }



    //get my interest from Db
    private void getinterestfromDb(String myinterestt) {

        String[] itnterestt = myinterestt.split(",");


        for (int i = 0; i < chipGroup.getChildCount(); i++) {

            Chip chip = (Chip) chipGroup.getChildAt(i);

            for (int ii = 0; ii < itnterestt.length; ii++) {
                if (itnterestt[ii].equals(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }


        }


    }


    public String interestList() {
        String interst = "interest,";
        if (!(myinterstChip == null)) {

            for (int i = 0; i < myinterstChip.size(); i++) {
                interst += "," + myinterstChip.get(i);
            }
        }
        return interst;
    }


    private boolean validateUsername() {
        String usernameInput = userName.getText().toString().trim();

        if (usernameInput.isEmpty()) {
            Username_profile_text_input.setError("Field can't be empty!");
            return false;
        } else if (!UserName_PATTERN.matcher(usernameInput).matches()) {
            Username_profile_text_input.setError("- at lease 3 Charachters \n" +
                    "- no special charachter\n" +
                    "- no white space");
            return false;
        } else {
            Username_profile_text_input.setError(null);
            return true;
        }

    }

    private boolean validdate() {

        if (date_picker_layout.getVisibility() == View.GONE || day.equals("Day") || month.equals("Month") || year.equals("Year")) {
            return false;
        } else {
            return true;
        }

    }

    public void showPicker(View view) {

        date_picker_layout.setVisibility(View.VISIBLE);

    }


    public void saveprofile(final View view) {




        if (validateUsername()) {
            view.setEnabled(false);

            String usernameText = userName.getText().toString();
            String userAbountmeText = userAboutme.getText().toString();
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            String LastUpdate = currentDate.format(calForDate.getTime());

            progress_heart.setVisibility(View.VISIBLE);

            HashMap userMap = new HashMap();

            if (validdate()) {
                int ddy = Integer.parseInt(day);
                int mnd = Integer.parseInt(month);
                int yyer = Integer.parseInt(year);
                String dateofbirth = ddy + "-" + mnd + "-" + yyer;
                userMap.put("dateofbirth", dateofbirth);
                UserAge = getAge(yyer, mnd, ddy);
                int range = GetAgeRange(UserAge);
                userMap.put("age", UserAge);
                userMap.put("agerange", range);
            }


            userMap.put("username", usernameText);
            userMap.put("countrycode", ccp.getSelectedCountryNameCode());
            userMap.put("country", ccp.getSelectedCountryName());
            userMap.put("relationship", RelationShip);
            userMap.put("lastupdate", LastUpdate);
            userMap.put("myinterest", interestList());
            userMap.put("aboutme", userAbountmeText);
            userMap.put("gender", getGender());
            userMap.put("lookingfor", getLookingFor());

            //save username into shared
            editor = pref.edit();
            editor.putString("username",usernameText);
            editor.putString("country",ccp.getSelectedCountryNameCode());
            editor.apply();


            UserRef.set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progress_heart.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "Updated Successfully", Toast.LENGTH_LONG).show();
                    view.setEnabled(true);
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MyProfileActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    view.setEnabled(true);
                }
            });

        }


    }

    private int GetAgeRange(int userAge) {

        if (userAge <= 28) {
            ageCat = 1;
        } else if (userAge <= 38) {
            ageCat = 2;
        } else if (userAge <= 48) {
            ageCat = 3;
        } else if (userAge <= 58) {
            ageCat = 4;
        } else if (userAge <= 68) {
            ageCat = 5;
        } else if (userAge <= 78) {
            ageCat = 6;
        } else if (userAge <= 88) {
            ageCat = 7;
        } else if (userAge <= 98) {
            ageCat = 8;
        }

        return ageCat;


    }

    private int getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }


    String getLookingFor() {
        int lookingradio = radioGroup_lookingfor.getCheckedRadioButtonId();

        if (lookingradio == R.id.lookingfor_man) {
            return "Male";
        } else if (lookingradio == R.id.lookingfor_woman) {
            return "Female";
        } else {
            return "Both";
        }

    }


    String getGender() {
        int genderradio = radioGroup_gender.getCheckedRadioButtonId();

        if (genderradio == R.id.gender_male) {
            globalVar.setGender("Female");
            return "Male";
        } else {
            globalVar.setGender("Male");
            return "Female";
        }

    }

    public void finnishh(View view) {
        finish();
    }
}
