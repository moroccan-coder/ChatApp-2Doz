package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class SetupActivity extends AppCompatActivity {
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 11;
    CountryCodePicker ccp;
    private Calendar myCalendar = Calendar.getInstance();
    EditText etDate;
    String countrySelectName = null;
    String countrySelectNamecode = null;
    Spinner spinnerDay, spinnerMonth, spinnerYear;
    FirebaseAuth mAuth;
    private String RelationShip = "Single";

    ImageView img_Rin, img_Rsingle,img_Rcomp, my_profile_pic;
    ProgressBar progress_profile, progress_heart;
    StorageReference UserProfileImageRef;
    String CurrentUserID;

    String day = null, month = null, year = null;
    boolean profileimgCheck = false;
    int ageCat = 0;

    String gender = null;
    String lookingfor = null;
    Random myrandom;

    //firestore
    private DocumentReference UserRef;


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
         pref = getSharedPreferences("userInf",Context.MODE_PRIVATE);


        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseFirestore.getInstance().collection("Users").document(CurrentUserID);
        //Create a profile image Folder
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.raleway_medium);
        ccp.setTypeFace(typeface);
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countrySelectName = ccp.getSelectedCountryName();
                countrySelectNamecode = ccp.getSelectedCountryNameCode();
            }
        });


        initUI();

        // Show the Image Profile after adding it successfully into the storage folder

        UserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.contains("profileimage")) {
                            String image = document.get("profileimage").toString();
                            //using picasso lib to show the  image
                            Picasso.get().load(image).placeholder(R.drawable.avatar_prf).into(my_profile_pic);
                            profileimgCheck = true;
                        }

                        if (document.contains("username"))
                        {
                            //save username into shared
                            editor = pref.edit();
                            editor.putString("username",document.get("username").toString());
                            editor.apply();
                        }

                    } else {
                        Toast.makeText(SetupActivity.this, "No user With this data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SetupActivity.this, "Error Retrieve Data", Toast.LENGTH_SHORT).show();
                }

            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                day = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                month = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void initUI() {
        spinnerDay = findViewById(R.id.day_spinner);
        spinnerMonth = findViewById(R.id.month_spinner);
        spinnerYear = findViewById(R.id.year_spinner);
        img_Rin = findViewById(R.id.imgV_relation_in);
        img_Rsingle = findViewById(R.id.imgV_relation_single);
        img_Rcomp = findViewById(R.id.imgV_relation_comp);
        progress_profile = findViewById(R.id.progress_heart_profile);
        progress_heart = findViewById(R.id.progress_heart);
        my_profile_pic = findViewById(R.id.my_profile_pic);

        spinnerYearIntialis();
        spinnerDayIntialis();
        spinnerMonthIntialis();
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


    public void profImage(View view) {
            if (checkAndRequestPermissions()) {
                OpenGallery();
            }
    }


    public void signout(View view) {
        mAuth.signOut();
        startActivity(new Intent(SetupActivity.this, MainActivity.class));
    }

    private boolean validRelation() {

        if (RelationShip == null) {
            Toast.makeText(SetupActivity.this, "PLZ select your RelationShip", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

    private boolean validCountry() {

        countrySelectName = ccp.getSelectedCountryName();
        countrySelectNamecode = ccp.getSelectedCountryNameCode();
        if (countrySelectName == null) {
            Toast.makeText(SetupActivity.this, "PLZ select your Country", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

    private boolean validProfile() {

        if (!profileimgCheck) {
            Toast.makeText(SetupActivity.this, "PLZ select your your profile pictures", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

    private boolean validdate() {

        if (day.equals("Day") || month.equals("Month") || year.equals("Year")) {
            Toast.makeText(SetupActivity.this, "PLZ select your birth Day", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
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




    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                progress_profile.setVisibility(View.VISIBLE);

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
                                UserRef.set(imgval,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progress_profile.setVisibility(View.GONE);
                                        profileimgCheck = true;
                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.avatar_prf).into(my_profile_pic);

                                        //save pic into shared
                                        editor = pref.edit();
                                        editor.putString("pic",downloadUrl);
                                        editor.apply();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SetupActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        progress_heart.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                    }
                });

            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(SetupActivity.this, "Error: Image can be Cropped ,try Again!", Toast.LENGTH_SHORT).show();
                progress_profile.setVisibility(View.GONE);
            }
        }
    }


    public void saveprofile(final View view) {


        if (validProfile() && validCountry() && validdate() && validRelation()) {
            view.setEnabled(false);

            int ddy = Integer.parseInt(day);
            int mnd = Integer.parseInt(month);
            int yyer = Integer.parseInt(year);

            String dateofbirth = ddy + "-" + mnd + "-" + yyer;

            int UserAge = getAge(yyer, mnd, ddy);
            int range =GetAgeRange(UserAge);

            progress_heart.setVisibility(View.VISIBLE);

            HashMap userMap = new HashMap();
            userMap.put("dateofbirth", dateofbirth);
            userMap.put("age", UserAge);
            userMap.put("agerange", range);
            userMap.put("country", countrySelectName);
            userMap.put("countrycode", countrySelectNamecode);
            userMap.put("relationship", RelationShip);
            userMap.put("registered", FieldValue.serverTimestamp());
            userMap.put("lastupdate", FieldValue.serverTimestamp());
            userMap.put("login", FieldValue.serverTimestamp());
            userMap.put("myinterest", "interest,");
            userMap.put("aboutme", "Hey let's be friends \uD83D\uDE42");
            userMap.put("online", true);
            userMap.put("onlinechating", false);
            userMap.put("lastseen", FieldValue.serverTimestamp());






            UserRef.set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progress_heart.setVisibility(View.GONE);
                    Toast.makeText(SetupActivity.this, "Your account is Created Successefully", Toast.LENGTH_LONG).show();

                    //save username into shared
                    editor = pref.edit();
                    editor.putString("country",countrySelectNamecode);
                    editor.apply();
                    subscriptionToTopic();
                    SendUserToMainActivity();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SetupActivity.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void SendUserToMainActivity() {

        startActivity(new Intent(SetupActivity.this, MainActivity.class));
        finish();

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


    private Uri getImageUri(Uri inImage) {


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Bitmap bitmap = null;
        String path = null;

        bitmap = getBitmap(inImage);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        path = MediaStore.Images.Media.insertImage(SetupActivity.this.getContentResolver(), bitmap, "profile_" + System.currentTimeMillis(), null);

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


    void OpenGallery() {
        // carry on the normal flow, as the case of  permissions  granted.
     /*   Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, 1);*/

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2, 3)
                .setActivityTitle("My Crop")
                //.setCropMenuCropButtonTitle("Done")
               // .setRequestedSize(400, 400)
                .start(this);

    }

    private void subscriptionToTopic() {

        String CurrentUser = mAuth.getCurrentUser().getUid();
        FirebaseMessaging.getInstance().subscribeToTopic(CurrentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
