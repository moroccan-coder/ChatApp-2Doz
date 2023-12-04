package com.example.needlove;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.needlove.Adapters.SwipAdapter;
import com.example.needlove.Model.UsersDiscover;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.model.Distance;
import uk.co.mgbramwell.geofire.android.model.DistanceUnit;
import uk.co.mgbramwell.geofire.android.model.QueryLocation;
import uk.co.mgbramwell.geofire.android.query.GeoFireQuery;


public class MatcherFrag extends Fragment {

    private static final int REQUEST_LOCATION = 900;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION_FINE = 593;
    private ArrayList<UsersDiscover> swipeUsers;
    GlobalVar globalVar;
    private SwipAdapter swipAdapter;

    private int i;
    ImageView likeemoji, dislikemoji;
    Animation animation;
    List<UsersDiscover> userModels;

    //Location
    Double latitude = 0.0;
    Double longitude = 0.0;
    Location gpsLocation = null, networkLocation = null, finalLocation = null;
    LocationManager locationManager;
    SharedPreferences.Editor editor;
    SharedPreferences pref;

    private boolean allowRefresh = false;
    //firebase
    FirebaseAuth mAuth;
    String CurrentUserID;
    CollectionReference UserRef;

    //locationResuest


    //Geofire Nearby
    private GeoFire geoFire;

    private DocumentSnapshot lasResult = null;

    View layoutAnim;
    CircleImageView my_profile_pic;
    ImageView imgAniim, imgAniim2;
    Handler handlerAnimationLoading;

    int countnb = 0;

    LinearLayout lntbuttonn;

    LinearLayout lnrEmpty;

    int passDocuments = 0;
    String usernamePref;
    String pic;


    //increment likes
    DocumentReference incrementLikes;

    FloatingActionButton right,left;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_matcher, container, false);

        SharedPreferences pref = getActivity().getSharedPreferences("userInf", Context.MODE_PRIVATE);



        if(pref.contains("username"))
        {
            usernamePref = pref.getString("username", "User");
            pic = pref.getString("pic", "http://");
        }
        else {
            usernamePref = "User";
            pic = "http://";
        }



        globalVar = (GlobalVar) getActivity().getApplicationContext();
        //intilise firebase
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseFirestore.getInstance().collection("Users");

        // Setup GeoFire Collection --> Nearby
        geoFire = new GeoFire(UserRef);


        //add the view via xml or programmatically
        final SwipeFlingAdapterView flingContainer = view.findViewById(R.id.swipcardd);
        swipeUsers = new ArrayList<>();

        userModels = new ArrayList<>();

        CheckPermissions();


        swipAdapter = new SwipAdapter(getContext(), swipeUsers);
        //buttons init
        likeemoji = view.findViewById(R.id.like_emoji);
        dislikemoji = view.findViewById(R.id.dislike_emoji);
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeanim);
        //init for Animation Loading
        handlerAnimationLoading = new Handler();
        layoutAnim = view.findViewById(R.id.layoutAnim);
        my_profile_pic = view.findViewById(R.id.my_profile_pic);
        imgAniim = view.findViewById(R.id.imgAniim);
        imgAniim2 = view.findViewById(R.id.imgAniim2);
        right = view.findViewById(R.id.right);
        left = view.findViewById(R.id.left);
        lntbuttonn = view.findViewById(R.id.lntbuttonn);
        lnrEmpty = view.findViewById(R.id.lnrEmpty);

        startTask();

        flingContainer.setAdapter(swipAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {


            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                swipeUsers.remove(0);
                swipAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject


                left.animate().scaleX(0.7f).scaleY(0.7f).setDuration(50).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        left.animate().scaleX(1f).scaleY(1f);
                    }
                });

                UsersDiscover obj = (UsersDiscover) dataObject;
                String userId = obj.getUid();

                final HashMap userSwip = new HashMap();

                userSwip.put("action", "nope");
                UserRef.document(userId).collection("swipe").document(CurrentUserID).set(userSwip);

                if (swipAdapter.isEmpty()) {
                    //getUsers_nearly();
                    lntbuttonn.setVisibility(View.GONE);
                    lnrEmpty.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onRightCardExit(Object dataObject) {



                right.animate().scaleX(0.7f).scaleY(0.7f).setDuration(50).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        right.animate().scaleX(1f).scaleY(1f);
                    }
                });

                UsersDiscover obj = (UsersDiscover) dataObject;
                String userId = obj.getUid();
                String userNam = obj.getUsername();
                String userPicc = obj.getProfileimage();

                final HashMap userSwip = new HashMap();

                userSwip.put("action", "yeps");
                userSwip.put("time", FieldValue.serverTimestamp());
                userSwip.put("username",usernamePref);
                userSwip.put("pic",pic);
                userSwip.put("show",true);
                UserRef.document(userId).collection("swipe").document(CurrentUserID).set(userSwip).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //check Matcher
                        UserRef.document(CurrentUserID)
                                .collection("swipe")
                                .document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    if (documentSnapshot.get("action").toString().equals("yeps")) {
                                        // i Add this to prevent showing in likes list of the other user
                                        final HashMap usermtch = new HashMap();
                                        usermtch.put("show", false);
                                        UserRef.document(userId).collection("swipe").document(CurrentUserID).set(usermtch,SetOptions.merge());
                                        UserRef.document(CurrentUserID).collection("swipe").document(userId).set(usermtch,SetOptions.merge());

                                        ////////

                                        Intent intent = new Intent(getActivity(), MatchActivity.class);

                                        intent.putExtra("userId", userId);
                                        intent.putExtra("username", userNam);
                                        intent.putExtra("pic", userPicc);

                                        startActivity(intent);
                                        getActivity().overridePendingTransition(R.anim.bottom_up, R.anim.nothing);

                                        //Add User Matcher
                                        AddMatcher(userId,userPicc,userNam);
                                    }


                                }


                            }
                        });

                        IncrementLikees(userId);

                    }

                });






                if (swipAdapter.isEmpty()) {
                    lntbuttonn.setVisibility(View.GONE);
                    lnrEmpty.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
              /*  swipeUsers.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;*/

                if (itemsInAdapter == 1) {
                    userModels.clear();
                    getUsers_nearly();
                }


            }

            @Override
            public void onScroll(float v) {

            }
        });

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {


                UsersDiscover obj = (UsersDiscover) dataObject;
                String userId = obj.getUid();

                Intent intent = new Intent(getActivity(), UserProfile.class);
                intent.putExtra("key", userId);
                intent.putExtra("pic", obj.getProfileimage());


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                }

                else {
                    startActivity(intent);
                }


            }
        });


       right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.animate().scaleX(0.7f).scaleY(0.7f).setDuration(50).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate().scaleX(1f).scaleY(1f);
                        flingContainer.getTopCardListener().selectRight();
                    }
                });

            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.animate().scaleX(0.7f).scaleY(0.7f).setDuration(50).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate().scaleX(1f).scaleY(1f);
                        flingContainer.getTopCardListener().selectLeft();
                    }
                });


            }
        });

        return view;

    }


    private void AddMatcher(String userId,String ppic , String name) {

        Map<String,Object> mtchrMap = new HashMap<>();
        mtchrMap.put("time",FieldValue.serverTimestamp());
        mtchrMap.put("username",name);
        mtchrMap.put("pic",ppic);

        UserRef.document(CurrentUserID).collection("Matchers").document(userId).set(mtchrMap);

        Map<String,Object> mtchrMapMe = new HashMap<>();
        mtchrMapMe.put("time",FieldValue.serverTimestamp());
        mtchrMapMe.put("username",usernamePref);
        mtchrMapMe.put("pic",pic);

        UserRef.document(userId).collection("Matchers").document(CurrentUserID).set(mtchrMapMe);

    }


    void startTask() {
        runnableAnim.run();
        layoutAnim.setVisibility(View.VISIBLE);
    }

    void stopTask() {
        handlerAnimationLoading.removeCallbacks(runnableAnim);
        layoutAnim.setVisibility(View.GONE);
        lntbuttonn.setVisibility(View.VISIBLE);
    }

    void stopTaskWithNoData() {
        handlerAnimationLoading.removeCallbacks(runnableAnim);
        layoutAnim.setVisibility(View.GONE);
        lnrEmpty.setVisibility(View.VISIBLE);
    }

    Runnable runnableAnim = new Runnable() {
        @Override
        public void run() {
            imgAniim.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    imgAniim.setScaleX(1f);
                    imgAniim.setScaleY(1f);
                    imgAniim.setAlpha(1f);
                }
            });


            imgAniim2.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(700).withEndAction(new Runnable() {
                @Override
                public void run() {
                    imgAniim2.setScaleX(1f);
                    imgAniim2.setScaleY(1f);
                    imgAniim2.setAlpha(1f);
                }
            });

            handlerAnimationLoading.postDelayed(runnableAnim, 1500);
        }
    };


    @SuppressLint("MissingPermission")
    void getlocationAfterGranted() {

        //test distence between two latitude/languitude


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        if (gpsLocation != null) {
            finalLocation = gpsLocation;
            latitude = finalLocation.getLatitude();
            longitude = finalLocation.getLongitude();
        } else if (networkLocation != null) {
            finalLocation = networkLocation;
            latitude = finalLocation.getLatitude();
            longitude = finalLocation.getLongitude();
        }

        getPositionTask myLocation = new getPositionTask();
        myLocation.execute();


    }


    class getPositionTask extends AsyncTask<Void, Void, Void> {

        String city;

        @Override
        protected Void doInBackground(Void... voids) {


            try {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);


                if (addresses != null && addresses.size() > 0) {
                    String addresse = addresses.get(0).getAddressLine(0);
                    city = addresses.get(0).getLocality();
                    String country = addresses.get(0).getCountryName();
                    String state = addresses.get(0).getAdminArea();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();


                    // final location

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            final HashMap LocationMp = new HashMap();

            LocationMp.put("latitude", latitude);
            LocationMp.put("longitude", longitude);
            LocationMp.put("city", city);

            UserRef.document(CurrentUserID).set(LocationMp, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    geoFire.setLocation(CurrentUserID, latitude, longitude);
                    getUsers_nearly();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }


    boolean checkDistence(String latitud2, String longtud2) {
        if (latitud2.equals("0.0")) {
            return false;
        } else {
            int totalDistence = 0;
            float latituud2 = Float.parseFloat(latitud2);
            float langitd2 = Float.parseFloat(longtud2);

            Location lot2 = new Location("");
            lot2.setLatitude(latituud2);
            lot2.setLongitude(langitd2);

            totalDistence = (int) finalLocation.distanceTo(lot2);

            if (totalDistence < 1001) {

                return true;
            } else {
                return false;
            }
        }


    }


    private void getUsers_nearly() {


        QueryLocation queryLocation = QueryLocation.fromDegrees(latitude, longitude);
        Distance searchDistance = new Distance(100000.0, DistanceUnit.KILOMETERS);
        GeoFireQuery query;
        if (lasResult == null) {
            query = geoFire.query()
                    .whereEqualTo("gender", globalVar.getGender())
                    .whereNearTo(queryLocation, searchDistance)
                    .orderBy("registered")
                    .limit(3);
        } else {
            query = geoFire.query()
                    .whereEqualTo("gender", globalVar.getGender())
                    .whereNearTo(queryLocation, searchDistance)
                    .orderBy("registered")
                    .startAfter(lasResult)
                    .limit(3);
        }


        query.build().get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


                for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {

                    UserRef.document(documentSnapshot1.getId()).collection("swipe").document(CurrentUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if (!documentSnapshot.exists()) {
                                userModels.add(documentSnapshot1.toObject(UsersDiscover.class));

                                countnb++;

                            } else {
                                countnb++;

                            }

                            if (countnb == 3) {
                                countnb = 0;
                                swipAdapter.addAll(userModels);

                               checkEmpty();

                               if(userModels.size() >0)
                               {
                                   stopTask();
                               }

                            }

                        }
                    });



                }


                if (queryDocumentSnapshots.size() > 0) {
                    lasResult = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    if (queryDocumentSnapshots.size() <3)
                    {
                        countnb = 0;
                        swipAdapter.addAll(userModels);

                        if(userModels.size() >0)
                        {
                            stopTask();
                        }
                        else {
                            stopTaskWithNoData();
                        }
                    }


                } else {

                /*  if (userModels.size() ==0)
                  {
                      Toast.makeText(getActivity(), "users lis = 0", Toast.LENGTH_SHORT).show();
                      stopTaskWithNoData();

                  }

                  */

                    stopTaskWithNoData();

                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                stopTask();
            }
        });


    }

    private void checkEmpty() {

        if (userModels.size() == 0)
        {
            getUsers_nearly();
        }
    }


    public void CheckPermissions() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            // No explanation needed; request the permission
            getActivity().getSupportFragmentManager().beginTransaction().disallowAddToBackStack()
                    .replace(R.id.fragment_container, new NoGpsFrag()).commit();

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            // the code after permession granted :
            getlocationAfterGranted();

        }

    }


    void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MatcherFrag()).addToBackStack(null).commit();
    }


    private void IncrementLikees(String userid) {

        incrementLikes  = FirebaseFirestore.getInstance().collection("Users").document(userid).collection("Counter").document("Likes");

        incrementLikes.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists())
                {
                    Map<String, Object> mp = new HashMap();
                    mp.put("nblikes", 1);
                    //Create the filed
                    incrementLikes.set(mp);
                }
                else {
                    incrementLikes.update("nblikes", FieldValue.increment(1));
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}
