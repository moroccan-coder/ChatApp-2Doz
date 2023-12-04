package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.needlove.Adapters.SliderPagerAdapter;
import com.example.needlove.Adapters.TestSlideAdapter;
import com.example.needlove.Model.Slide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity implements TestSlideAdapter.imgClick {
    CircleImageView my_profile_pic;
    //slider
    private ViewPager2 viewPager2;
    //slider
    private List<Slide> listSlide;

    ArrayList<String> linkList;
    ArrayList<String> linkList_empty;

    // Firebase
    CollectionReference image_slide_databaseREf;

    //is a friend
    DocumentReference isFriendRef;
    DocumentReference isFriendOther;

    DocumentReference incrementRequ;
    StorageReference UserSlideImages;

    FloatingActionButton floatBtnUp, floatBtnDown;

    TextView txtUsernameAge, txtCountryCity, txtAbout, txtRelation;

    ChipGroup chipsGroup;
    ScrollView scrollHide;
    ImageView ImgRelation;
    String immg;
    String userid;

    ImageView ImgSocialBtn;
    TextView txtSocial;

    //bootom Sheet
    LinearLayout bottomChetLayout;
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout btnDeletUser, btnReturn, btnDeletRequest;

    FirebaseAuth mAuth;
    String CurrentUsrId;
    //FriendReguest DocumentsReference
    DocumentReference Req_Current_usr;
    DocumentReference Req_user_receiver;
    String username;

    // check friends and request
    boolean userHaveReq = false;
    boolean userHaveFriend = false;

    boolean isBlocked = false;
    boolean imBlocked = false;

    DocumentReference UserRefMe, UserRefOther;

    TextView txtBlockUser, txtBlockMe;
    RelativeLayout lnrShadowSheet;

    boolean isCheetVisible = false;
    DocumentReference usrRef, usrRefOther;

    Dialog mdDialog;

    TestSlideAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        userid = getIntent().getExtras().getString("key");
        immg = getIntent().getExtras().getString("pic");

        mAuth = FirebaseAuth.getInstance();
        CurrentUsrId = mAuth.getCurrentUser().getUid();

        UserRefMe = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(CurrentUsrId);

        UserRefOther = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(userid);


        usrRef = FirebaseFirestore.getInstance().collection("Users").document(CurrentUsrId).collection("Counter").document("friends");

        UserSlideImages = FirebaseStorage.getInstance().getReference().child("Slide Images");


        image_slide_databaseREf = FirebaseFirestore.getInstance().collection("Users").document(userid).collection("Slider Profile image");


        mdDialog = new Dialog(this);
        //counter Request

        //Friends Request
        Req_Current_usr = FirebaseFirestore.getInstance().collection("FriendsRequest")
                .document(CurrentUsrId).collection("Send").document(userid);

        Req_user_receiver = FirebaseFirestore.getInstance().collection("FriendsRequest")
                .document(userid).collection("Receive").document(CurrentUsrId);

        //Is a Friend me
        isFriendRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(CurrentUsrId)
                .collection("Friends").document(userid);

        //is a friend Other
        isFriendOther = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userid)
                .collection("Friends").document(CurrentUsrId);


        initViews();
        //hide sheet bottom

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        Picasso.get().load(immg).into(my_profile_pic);
        isCheetVisible = false;


        UserRefOther.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    username = document.get("username").toString();
                    String age = document.get("age").toString();
                    String country = document.get("country").toString();

                    String city = "";
                    if (document.contains("city") && document.get("city") != null) {
                        city = document.get("city").toString();
                    }


                    txtUsernameAge.setText("@" + username + ", " + age);
                    txtCountryCity.setText(country + ", " + city);
                    txtAbout.setText(document.get("aboutme").toString());
                    getinterestfromDb(document.get("myinterest").toString());
                    getrelationfromdb(document.get("relationship").toString());

                } else {
                    Toast.makeText(UserProfile.this, "Error Retrieve Data", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Check if Fave Friend/Request
        CheckIfUserIfHaveReq();


        btnDeletUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mdDialog.setContentView(R.layout.delete_friend_confirmation);
                TextView btnYes = mdDialog.findViewById(R.id.btn_yes);
                TextView btnNo = mdDialog.findViewById(R.id.btn_no);


                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deleteFriend();

                        mdDialog.dismiss();
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

        btnDeletRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                lnrShadowSheet.setVisibility(View.GONE);
                isCheetVisible = false;
                CancelRequest();
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                isCheetVisible = false;
                lnrShadowSheet.setVisibility(View.GONE);
            }
        });


        UserRefMe.collection("UsersBlock").document(userid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    isBlocked = true;
                    txtBlockUser.setVisibility(View.VISIBLE);

                } else {
                    isBlocked = false;
                    txtBlockUser.setVisibility(View.GONE);


                }
            }
        });


        UserRefOther.collection("UsersBlock").document(CurrentUsrId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    imBlocked = true;
                    txtBlockMe.setVisibility(View.VISIBLE);

                } else {

                    imBlocked = false;
                    txtBlockMe.setVisibility(View.GONE);

                }
            }
        });

        //slider list
        listSlide = new ArrayList<>();
        mAdapter = new TestSlideAdapter(listSlide, viewPager2);
        viewPager2.setAdapter(mAdapter);


        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {

                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        viewPager2.setPageTransformer(compositePageTransformer);


        mAdapter.setonImageClickLisener(this);

    }

    private void deleteFriend() {
        ImgSocialBtn.setImageResource(R.drawable.ic_person_add_black);
        ImgSocialBtn.setBackgroundResource(R.drawable.btn_social_bg);
        txtSocial.setTextColor(getResources().getColor(R.color.color_blak));

        txtSocial.setText("Add Friend");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        isCheetVisible = false;
        //delete friend
        isFriendRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                isFriendOther.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        usrRef.update("nbfriends", FieldValue.increment(-1));
                        decrimentOther();
                        userHaveReq = false;
                        userHaveFriend = false;
                        lnrShadowSheet.setVisibility(View.GONE);
                        Toast.makeText(UserProfile.this, "Removed from friend list", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }

    private void decrimentOther() {
        usrRefOther = FirebaseFirestore.getInstance().collection("Users").document(userid).collection("Counter").document("friends");
        usrRefOther.update("nbfriends", FieldValue.increment(-1));
    }


    @Override
    public void onStart() {
        super.onStart();
        loadImageSlideFromDb();
    }

    private void initViews() {
        my_profile_pic = findViewById(R.id.my_profile_pic);
        // Slider
        viewPager2 = findViewById(R.id.viewpagerImageSlider2);


        //btn Hide / show Card
        floatBtnUp = findViewById(R.id.floatBtnUp);
        floatBtnDown = findViewById(R.id.floatBtnDown);


        txtCountryCity = findViewById(R.id.txtCountryCity);
        txtUsernameAge = findViewById(R.id.txtUsernameAge);
        lnrShadowSheet = findViewById(R.id.lnrShadowSheet);
        txtAbout = findViewById(R.id.txtAbout);
        chipsGroup = findViewById(R.id.chipsGroup);
        txtRelation = findViewById(R.id.txtRelation);
        ImgRelation = findViewById(R.id.ImgRelation);
        scrollHide = findViewById(R.id.scrollHide);
        bottomChetLayout = findViewById(R.id.bottomChetLayout);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomChetLayout);
        btnDeletUser = findViewById(R.id.btnDeletUser);
        btnDeletRequest = findViewById(R.id.btnDeletFriendReq);
        btnReturn = findViewById(R.id.btnReturn);
        ImgSocialBtn = findViewById(R.id.ImgSocialBtn);
        txtSocial = findViewById(R.id.txtSocial);
        txtBlockUser = findViewById(R.id.txtBlockUser);
        txtBlockMe = findViewById(R.id.txtBlockMe);
    }

    private void loadImageSlideFromDb() {

        Query query = image_slide_databaseREf.orderBy("indice", Query.Direction.ASCENDING)
                .orderBy("time", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    new LongOperation().execute(queryDocumentSnapshots);
                } else {
                    listSlide.clear();
                    listSlide.add(new Slide(immg));
                    mAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    @SuppressLint("RestrictedApi")
    public void ShowCard(View view) {


        view.animate().alpha(0f).setDuration(50).start();
        viewPager2.animate().translationY(0).alpha(1f).setDuration(300).start();
        floatBtnUp.animate().alpha(1f).setDuration(300).start();

        scrollHide.animate().alpha(0f).setDuration(50).start();
        scrollHide.setVisibility(View.GONE);
    }

    @SuppressLint("RestrictedApi")
    public void HidCard(View view) {
        //cardv.setVisibility(View.GONE);


        view.animate().alpha(0f).setDuration(50).start();
        viewPager2.animate().translationY(-2000).alpha(0f).setDuration(300).start();
        floatBtnDown.animate().alpha(1f).setDuration(300).start();

        scrollHide.setVisibility(View.VISIBLE);
        scrollHide.animate().alpha(1f).setDuration(2000).start();
    }

    public void btnAddFriend(View view) {

        if (!imBlocked) {

            if (userHaveReq) {
                btnDeletRequest.setVisibility(View.VISIBLE);
                btnDeletUser.setVisibility(View.GONE);
                lnrShadowSheet.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                isCheetVisible = true;

            } else if (userHaveFriend) {
                btnDeletRequest.setVisibility(View.GONE);
                btnDeletUser.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                lnrShadowSheet.setVisibility(View.VISIBLE);
                isCheetVisible = true;
            } else {

                setCancelRequestIcon();

                Map<String, Object> rMap = new HashMap<>();
                rMap.put("pic", immg);
                rMap.put("username", username);
                rMap.put("datesend", FieldValue.serverTimestamp());

                Req_Current_usr.set(rMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        SharedPreferences pref = getSharedPreferences("userInf", Context.MODE_PRIVATE);

                        String usernamePref;
                        String pic;

                        if (pref.contains("username")) {
                            usernamePref = pref.getString("username", "User");
                            pic = pref.getString("pic", "http://");
                        } else {
                            usernamePref = "User";
                            pic = "http://";
                        }


                        Map<String, Object> rMapReceiver = new HashMap<>();
                        rMapReceiver.put("pic", pic);
                        rMapReceiver.put("username", usernamePref);
                        rMapReceiver.put("datesend", FieldValue.serverTimestamp());

                        Req_user_receiver.set(rMapReceiver).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                IncrementRequest();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(UserProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(this, "You\'re Blocked by the User", Toast.LENGTH_SHORT).show();
        }


    }

    private void IncrementRequest() {

        incrementRequ = FirebaseFirestore.getInstance().collection("Users").document(userid).collection("Counter").document("requests");

        incrementRequ.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists()) {
                    Map<String, Object> mp = new HashMap();
                    mp.put("nbrequests", 1);
                    //Create the filed
                    incrementRequ.set(mp);
                } else {
                    incrementRequ.update("nbrequests", FieldValue.increment(1));
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void sendmsg(View view) {


        Intent intent = new Intent(UserProfile.this, ChatActivity.class);
        intent.putExtra("key", userid);
        intent.putExtra("username", username);
        intent.putExtra("pic", immg);
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(UserProfile.this, my_profile_pic, my_profile_pic.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);
    }

    @Override
    public void onImageClick(int position, ImageView img) {


        Intent intent = new Intent(UserProfile.this, ShowImageZoom.class);
        intent.putExtra("pic", listSlide.get(position).getImageLink());

        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions
                    .makeSceneTransitionAnimation(UserProfile.this, img, img.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);

    }


    private class LongOperation extends AsyncTask<QuerySnapshot, Void, String> {

        @Override
        protected String doInBackground(QuerySnapshot... params) {


            for (QueryDocumentSnapshot documentSnapshot : params[0]) {

                listSlide.add(new Slide(documentSnapshot.get("slideimage").toString()));
                linkList.add(documentSnapshot.get("slideimage").toString());
            }


            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mAdapter.notifyDataSetChanged();


        }

        @Override
        protected void onPreExecute() {
            //sended slider list
            linkList = new ArrayList<>();
            linkList_empty = new ArrayList<>();

            listSlide.clear();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private void getinterestfromDb(String myinterestt) {

        String[] itnterestt = myinterestt.split(",");


        for (int ii = 0; ii < itnterestt.length; ii++) {
            if (!itnterestt[ii].equals("interest") && !itnterestt[ii].equals(",") && !itnterestt[ii].equals("")) {
                Chip chip = new Chip(UserProfile.this);
                chip.setText(itnterestt[ii]);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
                chipsGroup.addView(chip);
            }

        }


    }

    //get relationShip from db
    void getrelationfromdb(String relattion) {

        if (relattion.equals("Single")) {

            ImgRelation.setImageResource(R.drawable.statussi);
            txtRelation.setText("Single");

        } else if (relattion.equals("In RelationShip")) {

            ImgRelation.setImageResource(R.drawable.statusin);
            txtRelation.setText("In RelationShip");

        } else if (relattion.equals("Complicated")) {

            ImgRelation.setImageResource(R.drawable.statusincomp);
            txtRelation.setText("Complicated");

        }


    }


    private void CheckIfUserIfHaveReq() {
        Req_Current_usr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (!documentSnapshot.exists()) {
                    CheckUserHaveFriend();
                } else {
                    setCancelRequestIcon();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void CheckUserHaveFriend() {
        isFriendRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    ImgSocialBtn.setImageResource(R.drawable.ic_person_blue);
                    txtSocial.setTextColor(getResources().getColor(R.color.blluee));
                    ImgSocialBtn.setBackgroundResource(R.drawable.btn_social_blue);
                    txtSocial.setText("Friend");
                    userHaveReq = false;
                    userHaveFriend = true;
                    btnDeletRequest.setVisibility(View.GONE);
                }
            }
        });


    }


    private void setCancelRequestIcon() {
        ImgSocialBtn.setImageResource(R.drawable.ic_hourglass_empty_blue);
        txtSocial.setTextColor(getResources().getColor(R.color.blluee));
        ImgSocialBtn.setBackgroundResource(R.drawable.btn_social_blue);
        txtSocial.setText("Cancel Request");
        userHaveReq = true;
    }

    private void AfterCancelRequest() {
        ImgSocialBtn.setImageResource(R.drawable.ic_person_add_black);
        ImgSocialBtn.setBackgroundResource(R.drawable.btn_social_bg);
        txtSocial.setTextColor(getResources().getColor(R.color.color_blak));
        txtSocial.setText("Add Friend");
        userHaveReq = false;
    }

    //Cancel Request
    private void CancelRequest() {
        AfterCancelRequest();
        Req_Current_usr.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Req_user_receiver.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfile.this, "Friend Request Canceled", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void showUserBlock(View view) {

        if (!imBlocked) {
            //create popup Menu
            PopupMenu popupMenu = new PopupMenu(this, view);
            //infalte menu from xml resources
            popupMenu.inflate(R.menu.block_user_menu);

            popupMenu.getMenu().findItem(R.id.user_clearMsg).setVisible(false);

            if (isBlocked) {
                popupMenu.getMenu().findItem(R.id.user_block).setVisible(false);

            } else {
                popupMenu.getMenu().findItem(R.id.user_unblock).setVisible(false);
            }


            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    switch (menuItem.getItemId()) {
                        case R.id.user_block:

                            Map<String, Object> usrblk = new HashMap<>();
                            usrblk.put("dateblocked", FieldValue.serverTimestamp());
                            UserRefMe.collection("UsersBlock").document(userid).set(usrblk).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UserProfile.this, "User Blocked", Toast.LENGTH_SHORT).show();
                                    isBlocked = true;
                                    txtBlockUser.setVisibility(View.VISIBLE);
                                }
                            });
                            break;

                        case R.id.user_unblock:

                            UserRefMe.collection("UsersBlock").document(userid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UserProfile.this, "User Unblocked", Toast.LENGTH_SHORT).show();
                                    isBlocked = false;
                                    txtBlockUser.setVisibility(View.GONE);

                                }
                            });
                            break;

                    }


                    return false;
                }
            });
            popupMenu.show();
        } else {
            Toast.makeText(this, "You\'re Blocked by the User ", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {
        if (isCheetVisible) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            isCheetVisible = false;
            lnrShadowSheet.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
