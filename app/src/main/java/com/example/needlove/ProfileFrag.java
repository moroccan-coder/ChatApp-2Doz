package com.example.needlove;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.needlove.Adapters.SliderPagerAdapter;
import com.example.needlove.Adapters.uploadImgSlideAdapter;
import com.example.needlove.Model.Slide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.example.needlove.Model.img_slide;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFrag extends Fragment implements uploadImgSlideAdapter.OnItemClickListener, uploadImgSlideAdapter.OnItemLongClickListenr, uploadImgSlideAdapter.OnItemTouchLiseener {
    FirebaseAuth mAuth;

    CircleImageView my_profile_pic;

    // Firebase
    private DocumentReference UserRef;
    CollectionReference image_slide_databaseREf;
    String current_user_id, downloadUrl;
    private StorageReference slide_image_Reference;

    private String imgURL;
    private Uri ImageUri;
    private int clickedEditImage = 0;
    private ProgressBar holderProgressBar;

    Point p;
    private ImageView holder;
    //edit pic linears
    int inndicc;
    private boolean isImagePressed = false;


    String immg;


    TextView userName, userStatus;
    ImageButton btnEdit, btnSettings;



    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 933;
    uploadImgSlideAdapter adapter;
    RecyclerView recyclerView;

    private ArrayList<String> imgUrlList;
    private ArrayList<String> imgID;

    public ProfileFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        //firebase

        current_user_id = mAuth.getCurrentUser().getUid();

        image_slide_databaseREf = FirebaseFirestore.getInstance().collection("Users").
                document(current_user_id).collection("Slider Profile image");

        UserRef = FirebaseFirestore.getInstance().collection("Users").document(current_user_id);
        // get Slides images Reference node
        slide_image_Reference = FirebaseStorage.getInstance().getReference();


        getProfileInfo();


        imgUrlList = new ArrayList<>();
        imgID = new ArrayList<>();

        imgUrlList.add("http://");
        imgID.add("http://");


    }

    private void getProfileInfo() {
       UserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e == null) {

                    if (documentSnapshot != null) {
                        if (documentSnapshot.contains("profileimage")) {
                            immg = documentSnapshot.get("profileimage").toString();
                            //using picasso lib to show the  image
                            Picasso.get().load(immg).placeholder(R.drawable.avatar_prf).into(my_profile_pic);

                            userName.setText("@" + documentSnapshot.get("username").toString() + ", " + documentSnapshot.get("age").toString());
                            userStatus.setText(documentSnapshot.get("aboutme").toString());

                        }
                    }
                }
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_profile, container, false);


        initViews(view);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new uploadImgSlideAdapter(getActivity(), imgUrlList);
        recyclerView.setAdapter(adapter);

        loadImageSlideFromDb();


        my_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfile = new Intent(getActivity(), MyProfileActivity.class);
                Bundle bundle = null;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
                }
                startActivity(editProfile, bundle);


            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfile = new Intent(getActivity(), MyProfileActivity.class);
                Bundle bundle = null;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
                }
                startActivity(editProfile, bundle);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfile = new Intent(getActivity(), SettingsActivity.class);
                startActivity(editProfile);
            }
        });


        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        adapter.setOnItemTouchLisener(this);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        return view;
    }


    private void initViews(View view) {


        //BindActivity
        userStatus = view.findViewById(R.id.userStatus);
        recyclerView = view.findViewById(R.id.card_recycler_view);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnEdit = view.findViewById(R.id.btnEdit);
        userName = view.findViewById(R.id.userName);

        my_profile_pic = view.findViewById(R.id.my_profile_pic);
        //  ImageView uploadImg= view.findViewById(R.id.uploadImg_btn);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                holderProgressBar.setVisibility(View.VISIBLE);
                ImageUri = result.getUri();
                ImageUri = getImageUri(ImageUri);
                Picasso.get().load("http://").placeholder(R.drawable.avatar_prf).into(holder);
                StorageImageToFirebaseStorage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getActivity(), "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                holderProgressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(getActivity(), "Error: Image can be Cropped ,try Again!", Toast.LENGTH_SHORT).show();
                holderProgressBar.setVisibility(View.GONE);
            }
        }
    }


    private Uri getImageUri(Uri inImage) {


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Bitmap bitmap = null;
        String path = null;

        bitmap = getBitmap(inImage);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "profile_" + System.currentTimeMillis(), null);

        return Uri.parse(path);
    }

    private Bitmap getBitmap(Uri path) {
        ContentResolver mContentResolver = getActivity().getContentResolver();
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


    private String imgName(Uri uri) {
        String path = uri.toString();
        String filename = path.substring(path.lastIndexOf("/") + 1);
        String file;
        if (filename.indexOf(".") > 0) {
            file = filename.substring(0, filename.lastIndexOf("."));
        } else {
            file = filename;
        }

        return file;
    }


    private void StorageImageToFirebaseStorage() {

        final StorageReference filePath = slide_image_Reference.child("Slide Images").child(current_user_id).child(imgName(ImageUri) + System.currentTimeMillis() + ".jpg");

        filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        if (uri != null) {
                            downloadUrl = uri.toString();

                            if (clickedEditImage == 1) {

                                updateImage();
                                clickedEditImage = 0;

                            } else {
                                SavingImagesInformationToDatabase();
                            }

                        } else {
                            Toast.makeText(getActivity(), "Error: image link not valid", Toast.LENGTH_SHORT).show();
                            // loadingBar.dismiss();
                        }


                    }
                });
            }


        });

    }

    private void SavingImagesInformationToDatabase() {


        HashMap slideImage = new HashMap();
        slideImage.put("indice", inndicc);
        slideImage.put("slideimage", downloadUrl);
        slideImage.put("time", FieldValue.serverTimestamp());


        image_slide_databaseREf.add(slideImage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                // dismis progress
                Toast.makeText(getActivity(), "uploaded successfully", Toast.LENGTH_SHORT).show();
                // notifyDataSetChanged();
                holderProgressBar.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    public void updateImage() {
        // image_slide_databaseREf

        imgURL = imgUrlList.get(inndicc);

        image_slide_databaseREf.document(imgID.get(inndicc)).update("slideimage", downloadUrl).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DeleteImageFromStorage(1);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "error:" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


        clickedEditImage = 0;


    }

    private void loadImageSlideFromDb() {


        Query query = image_slide_databaseREf.orderBy("indice", Query.Direction.ASCENDING)
                .orderBy("time", Query.Direction.DESCENDING);

         query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e == null) {
                    if (!queryDocumentSnapshots.isEmpty()) {


                        new LongOperation().execute(queryDocumentSnapshots);

                    }
                }

            }
        });

    }

    @Override
    public void onItemClick(int wichClick, int position, ImageView imgv, ProgressBar progress, LinearLayout lnr) {


        inndicc = position;


        if (wichClick == 1) {
            if (!imgUrlList.get(position).equals("http://") && !imgUrlList.get(position).equals("http://")) {


                int[] location = new int[2];
                imgv.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                p = new Point();
                p.x = location[0];
                p.y = location[1];

                //Open popup window
                if (p != null) {
                    holder = imgv;
                    holderProgressBar = progress;
                    lnr.setVisibility(View.VISIBLE);
                }

            } else {

                holder = imgv;

                if (checkAndRequestPermissions()) {
                    OpenGallery();
                }
                holderProgressBar = progress;

            }
        } else if (wichClick == 2) {
            clickedEditImage = 1;
            // gallery opened to chose edited photo
            if (checkAndRequestPermissions()) {
                OpenGallery();
            }

            lnr.setVisibility(View.GONE);
        } else {
            holderProgressBar.setVisibility(View.VISIBLE);
            DeleteImgSlideFromdatabase(0);
            lnr.setVisibility(View.GONE);
        }


    }

    @Override
    public void onItemLongClick(int position, View imgv) {

        imgv.setBackgroundResource(R.drawable.border_image);

        isImagePressed = true;

    }

    @Override
    public void onItemTouchClick(int position, View itemView, View pView, MotionEvent pEvent) {


        pView.onTouchEvent(pEvent);
        // We're only interested in when the button is released.
        if (pEvent.getAction() == MotionEvent.ACTION_UP) {
            // We're only interested in anything if our speak button is currently pressed.
            if (isImagePressed) {
                // Do something when the button is released.
                isImagePressed = false;
                itemView.setBackgroundResource(R.drawable.border_image_transparent);


            }
        }

    }


    private class LongOperation extends AsyncTask<QuerySnapshot, Void, String> {

        @Override
        protected String doInBackground(QuerySnapshot... params) {


            for (QueryDocumentSnapshot documentSnapshot : params[0]) {

                imgUrlList.add(documentSnapshot.get("slideimage").toString());

                imgID.add(documentSnapshot.getId());
            }


            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            if (imgUrlList == null) {

                imgUrlList.add("http://");
                imgID.add("http://");

            } else {

                imgUrlList.add("http://");
                imgID.add("http://");

            }

            adapter.notifyDataSetChanged();

        }

        @Override
        protected void onPreExecute() {

            imgUrlList.clear();
            imgID.clear();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private boolean checkAndRequestPermissions() {

        int permissionWriteStorage = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionReadStorage = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
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
            OpenGallery();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            /*If We Decline permission*/
            Toast.makeText(getActivity(), "Permission Declined", Toast.LENGTH_SHORT).show();
        }
    }


    void OpenGallery() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("My Crop")
                //.setCropMenuCropButtonTitle("Done")
                // .setRequestedSize(400, 400)
                .start(getActivity());

    }


    private void DeleteImgSlideFromdatabase(final int indicc) {
        // image_slide_databaseREf
        imgURL = imgUrlList.get(inndicc);

        image_slide_databaseREf.document(imgID.get(inndicc)).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "successfully deleted...", Toast.LENGTH_SHORT).show();
                holderProgressBar.setVisibility(View.GONE);
                DeleteImageFromStorage(indicc);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }


    private void DeleteImageFromStorage(final int indiic) {

        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imgURL);

        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully

                if (indiic == 0) {
                    holder.setImageResource(R.drawable.addpicc);
                }



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });

    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT
            | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {


            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

            if (!imgUrlList.get(fromPosition).equals("http://") && !imgUrlList.get(toPosition).equals("http://")) {
                image_slide_databaseREf.document(imgID.get(fromPosition)).update("indice", toPosition);
                image_slide_databaseREf.document(imgID.get(fromPosition)).update("time", FieldValue.serverTimestamp());

                viewHolder.itemView.setBackgroundResource(R.drawable.border_image_transparent);


            }

            Collections.swap(imgUrlList, fromPosition, toPosition);
            Collections.swap(imgID, fromPosition, toPosition);


            return false;


        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };


}
