package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.needlove.Adapters.MessagesAdapter;
import com.example.needlove.Model.MessageChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.needlove.SetupActivity.REQUEST_ID_MULTIPLE_PERMISSIONS;

public class ChatActivity extends AppCompatActivity implements MessagesAdapter.OnItemClickListener, MessagesAdapter.OnImgClickListener {

    TextView txtMessage;
    CircleImageView profile_user;
    TextView txtUsername, txtOnline;
    SwipeRefreshLayout swipeRefresh;
    ImageView imgMsg;
    FloatingActionButton btnSend;

    String CurrentUserID;
    String OtherUserID;
    String OtherUsername;
    String OtherPictures;

    //Firebase
    CollectionReference myChat, otherChat;


    //recyclerv

    private List<MessageChat> mMsg = new ArrayList<>();
    private MessagesAdapter mAdapter;
    DocumentSnapshot lasDocument;
    RecyclerView recyclerV;
    LinearLayout lnrProgressImageSend, lnrUnblock, lnrChat;

    StorageReference messageImageStorage;
    DocumentReference UserRefMe, UserRefOther;
    DocumentReference myChatInfo, otherChatInfo;

    boolean isBlocked = false;

    boolean firstCheck = true;
    boolean firstCheckIncrem = true;

    boolean ifisInChating = false;

    boolean ishere = true;

    boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initviews();

        // get Extras from Previous Activity
        OtherUserID = getIntent().getExtras().getString("key");
        OtherUsername = getIntent().getExtras().getString("username");
        OtherPictures = getIntent().getExtras().getString("pic");
        txtUsername.setText(OtherUsername);
        Picasso.get().load(OtherPictures).into(profile_user);

        // Firestore References
        CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserRefMe = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(CurrentUserID);

        UserRefMe.update("online", true);

        UserRefOther = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(OtherUserID);


        myChat = FirebaseFirestore.getInstance()
                .collection("Chat")
                .document(CurrentUserID)
                .collection("ChatingUser")
                .document(OtherUserID)
                .collection("Messages");

        otherChat = FirebaseFirestore.getInstance()
                .collection("Chat")
                .document(OtherUserID)
                .collection("ChatingUser")
                .document(CurrentUserID)
                .collection("Messages");


        // this references is to add user info in chat
        myChatInfo = FirebaseFirestore.getInstance()
                .collection("Chat")
                .document(CurrentUserID)
                .collection("ChatingUser")
                .document(OtherUserID);

        otherChatInfo = FirebaseFirestore.getInstance()
                .collection("Chat")
                .document(OtherUserID)
                .collection("ChatingUser")
                .document(CurrentUserID);


//Create a messages image Folder
        messageImageStorage = FirebaseStorage.getInstance().getReference().child("Messages Image");


        final LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);


        mAdapter = new MessagesAdapter(this, mMsg, CurrentUserID, OtherUserID);
        recyclerV.setHasFixedSize(true);
        recyclerV.setLayoutManager(manager);
        recyclerV.setAdapter(mAdapter);


        UserRefOther.collection("UsersBlock").document(CurrentUserID).addSnapshotListener(ChatActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    disableButtons();
                    txtMessage.setText("Chat Restricted!");
                    txtMessage.setTextColor(Color.RED);
                } else {
                    enableButtons();
                    txtMessage.setText("");
                    txtMessage.setTextColor(getResources().getColor(R.color.colorMessage));
                }
            }
        });

        LoadData();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (lasDocument != null) {
                    getmoreData();
                } else {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(ChatActivity.this, "No message yet!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        DocumentReference UserRef = FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(OtherUserID);

        UserRef.addSnapshotListener(ChatActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.getBoolean("online").booleanValue()) {
                    txtOnline.setText("Online");
                    isOnline = true;
                } else {
                    long time = documentSnapshot.getTimestamp("lastseen").toDate().getTime();
                    txtOnline.setText(TimeSince.getTimeAgo(time));
                    isOnline = false;
                }

                if (documentSnapshot.getBoolean("onlinechating").booleanValue()) {
                    ifisInChating = true;
                } else {
                    ifisInChating = false;
                }


            }
        });


        UserRefMe.collection("UsersBlock").document(OtherUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    isBlocked = true;
                    lnrUnblock.setVisibility(View.VISIBLE);
                    lnrChat.setVisibility(View.GONE);

                } else {
                    isBlocked = false;
                    lnrChat.setVisibility(View.VISIBLE);

                }
            }
        });

        mAdapter.setOnItemClickListener(ChatActivity.this);
        mAdapter.setOnImageClickLisener(ChatActivity.this);

    }


    void getmoreData() {
        Query query = myChat.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lasDocument)
                .limit(10);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {


                    MessageChat msg = documentSnapshot.toObject(MessageChat.class);
                    msg.setMsgID(documentSnapshot.getId());
                    mMsg.add(msg);


                }
                if (queryDocumentSnapshots.size() > 0) {

                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                }


                swipeRefresh.setRefreshing(false);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void LoadData() {

        Query query;

        query = myChat
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(20);


        query.addSnapshotListener(ChatActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                mMsg.clear();


                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {


                    MessageChat msg = documentSnapshot.toObject(MessageChat.class);
                    msg.setMsgID(documentSnapshot.getId());
                    mMsg.add(msg);


                }
                if (queryDocumentSnapshots.size() > 0) {

                    mAdapter.notifyDataSetChanged();
                    lasDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    if (ishere) {
                        myChatInfo.update("msgUnread", 0);
                    }


                }


                swipeRefresh.setRefreshing(false);

            }


        });


    }

    private void initviews() {
        txtMessage = findViewById(R.id.txtMessage);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerV = findViewById(R.id.recyclerV);
        profile_user = findViewById(R.id.profile_user);
        txtUsername = findViewById(R.id.txtUsername);
        txtOnline = findViewById(R.id.txtOnline);
        btnSend = findViewById(R.id.btnSend);
        imgMsg = findViewById(R.id.imgMsg);
        lnrUnblock = findViewById(R.id.lnrUnblock);
        lnrChat = findViewById(R.id.lnrChat);
        lnrProgressImageSend = findViewById(R.id.lnrProgressImageSend);
    }

    public void back(View view) {
        onBackPressed();
    }

    public void sendbtn(View view) {

        if (!txtMessage.getText().toString().isEmpty()) {

            if (firstCheck) {
                firstCheck = false;


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


                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", usernamePref);
                userInfo.put("pic", pic);

                otherChatInfo.set(userInfo, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> userme = new HashMap<>();
                        userme.put("username", OtherUsername);
                        userme.put("pic", OtherPictures);
                        myChatInfo.set(userme, SetOptions.merge());

                    }
                });
            }

            int randVal;
            String timee;
            String MSGid;
            timee = String.valueOf(System.currentTimeMillis());
            randVal = new Random().nextInt(100);

            MSGid = timee + randVal;

            String txtMsg = txtMessage.getText().toString();
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("time", FieldValue.serverTimestamp());
            msgMap.put("from", CurrentUserID);
            msgMap.put("message", txtMsg);
            msgMap.put("pic", "none");
            msgMap.put("type", "text");
            msgMap.put("seen", false);
            txtMessage.setText("");
            myChat.document(MSGid).set(msgMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    otherChat.document(MSGid).set(msgMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            recyclerV.scrollToPosition(0);
                            incrementMsgSend(txtMsg);
                        }
                    });

                }
            });

        }


    }


    private void incrementMsgSend(String mssg) {


        String mssgo = mssg;
        if (mssgo.length() > 50) {
            mssgo = mssg.substring(0, 50);
        }

        Map<String, Object> lastchatingTime = new HashMap<>();
        lastchatingTime.put("time", FieldValue.serverTimestamp());
        lastchatingTime.put("msgUnreadIndice", 1);
        lastchatingTime.put("lastmsg", mssgo + "...");
        otherChatInfo.set(lastchatingTime, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                myChatInfo.set(lastchatingTime, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (!ifisInChating) {
                            incremNotif();
                        }
                    }
                });

            }
        });


    }

    private void incremNotif() {

        if (firstCheckIncrem) {
            otherChatInfo.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.contains("pic")) {
                        otherChatInfo.update("msgUnread", FieldValue.increment(1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (!isOnline) {
                                    otherChatInfo.update("msgUnreadIndice", 0);
                                }

                            }
                        });

                    } else {

                        Map<String, Object> incrMsg = new HashMap<>();
                        incrMsg.put("msgUnread", 1);


                        otherChatInfo.set(incrMsg, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (!isOnline) {
                                    otherChatInfo.update("msgUnreadIndice", 0);
                                }
                            }
                        });

                    }

                }
            });

            firstCheckIncrem = false;
        } else {

            otherChatInfo.update("msgUnread", FieldValue.increment(1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (!isOnline) {
                        otherChatInfo.update("msgUnreadIndice", 0);
                    }
                }
            });
        }
    }

    public void addPic(View view) {
        if (checkAndRequestPermissions()) {
            OpenGallery();
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

    private void OpenGallery() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("My Crop")
                //.setCropMenuCropButtonTitle("Done")
                // .setRequestedSize(400, 400)
                .start(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                lnrProgressImageSend.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();
                resultUri = getImageUri(resultUri);

                // Save the imageProfile into Storage Profile Image Folder
                final StorageReference fielPath = messageImageStorage.child(System.currentTimeMillis() + ".jpg");


                fielPath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fielPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();

                                int randVal;
                                String timee;
                                String MSGid;
                                timee = String.valueOf(System.currentTimeMillis());
                                randVal = new Random().nextInt(100);

                                MSGid = timee + randVal;

                                Map<String, Object> imgval = new HashMap<>();

                                imgval.put("time", FieldValue.serverTimestamp());
                                imgval.put("from", CurrentUserID);
                                imgval.put("message", "none");
                                imgval.put("pic", downloadUrl);
                                imgval.put("type", "image");
                                imgval.put("seen", false);

                                myChat.document(MSGid).set(imgval).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        otherChat.document(MSGid).set(imgval).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid1) {
                                                //recyclerV.scrollToPosition(0);
                                                LoadData();
                                                lnrProgressImageSend.setVisibility(View.GONE);
                                                incrementMsgSend("image");
                                                //

                                            }
                                        });

                                    }
                                });
                            }
                        });
                    }
                });

            } else {
                Toast.makeText(ChatActivity.this, "Error: Image can be Cropped ,try Again!", Toast.LENGTH_SHORT).show();
                lnrProgressImageSend.setVisibility(View.GONE);
            }
        }
    }

    private Uri getImageUri(Uri inImage) {


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Bitmap bitmap = null;
        String path = null;

        bitmap = getBitmap(inImage);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        path = MediaStore.Images.Media.insertImage(ChatActivity.this.getContentResolver(), bitmap,
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

    @Override
    public void onItemMsgLongClick(int position, LinearLayout lnr) {
        MessageChat mssg = mMsg.get(position);
        //create popup Menu
        PopupMenu popupMenu = new PopupMenu(this, lnr);
        //infalte menu from xml resources
        popupMenu.inflate(R.menu.message_menu);


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.msg_delete:



                        String msgID = mssg.getMsgID();
                        if (mssg.getType().equals("text")) {
                            myChat.document(msgID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    otherChat.document(msgID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ChatActivity.this, "Message Deleted...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        } else {

                            StorageReference storageReference = FirebaseStorage
                                    .getInstance().
                                            getReferenceFromUrl(mssg.getPic());

                            myChat.document(msgID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            otherChat.document(msgID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ChatActivity.this, "Image Deleted...", Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                        }
                                    });

                                }
                            });
                        }

                        break;

                    case R.id.msg_copy:
                        // Gets a handle to the clipboard service.
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                        // Creates a new text clip to put on the clipboard
                        ClipData clip = ClipData.newPlainText("message text", mssg.getMessage());
                        // Set the clipboard's primary clip.
                        clipboard.setPrimaryClip(clip);
                }


                return false;
            }
        });

        popupMenu.show();

    }

    @Override
    public void onImgClick(int position, ImageView img) {

        String imgg = mMsg.get(position).getPic();

        Intent intent = new Intent(ChatActivity.this, ShowMsgPic.class);
        intent.putExtra("pic", imgg);
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions
                    .makeSceneTransitionAnimation(ChatActivity.this, img, img.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);
    }

    public void showUser(View view) {

        Intent intent = new Intent(ChatActivity.this, UserProfile.class);
        intent.putExtra("key", OtherUserID);
        intent.putExtra("pic", OtherPictures);
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(ChatActivity.this, profile_user, profile_user.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);
    }

    public void showUserBlock(View view) {
        //create popup Menu
        PopupMenu popupMenu = new PopupMenu(this, view);
        //infalte menu from xml resources
        popupMenu.inflate(R.menu.block_user_menu);

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
                        UserRefMe.collection("UsersBlock").document(OtherUserID).set(usrblk).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ChatActivity.this, "User Blocked", Toast.LENGTH_SHORT).show();
                                isBlocked = true;
                                lnrUnblock.setVisibility(View.VISIBLE);
                                lnrChat.setVisibility(View.GONE);
                            }
                        });
                        break;

                    case R.id.user_unblock:

                        UserRefMe.collection("UsersBlock").document(OtherUserID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ChatActivity.this, "User Unblocked", Toast.LENGTH_SHORT).show();
                                isBlocked = false;
                                lnrUnblock.setVisibility(View.GONE);
                                lnrChat.setVisibility(View.VISIBLE);

                            }
                        });
                        break;

                    case R.id.user_clearMsg:

                        ClearAllMessages();
                        break;
                }


                return false;
            }
        });
        popupMenu.show();


    }

    private void ClearAllMessages() {

//
        myChat.whereEqualTo("from", CurrentUserID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot querdocument : queryDocumentSnapshots) {
                    querdocument.getReference().delete();
                }

            }
        });


        otherChat.whereEqualTo("from", CurrentUserID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot querdocument : queryDocumentSnapshots) {
                    querdocument.getReference().delete();
                }

            }
        });


    }

    public void unBlockUsr(View view) {
        UserRefMe.collection("UsersBlock").document(OtherUserID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ChatActivity.this, "User Unblocked", Toast.LENGTH_SHORT).show();
                isBlocked = false;
                lnrUnblock.setVisibility(View.GONE);
                lnrChat.setVisibility(View.VISIBLE);
            }
        });
    }


    void enableButtons() {
        btnSend.setEnabled(true);
        imgMsg.setEnabled(true);
        txtMessage.setEnabled(true);
    }

    void disableButtons() {
        btnSend.setEnabled(false);
        imgMsg.setEnabled(false);
        txtMessage.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UserRefMe.update("onlinechating", true);
        ishere = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        UserRefMe.update("onlinechating", false);
        ishere = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserRefMe.update("onlinechating", false);
        ishere = false;
    }
}
