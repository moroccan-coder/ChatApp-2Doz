package com.example.needlove;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchActivity extends AppCompatActivity {

    boolean onresum = false;

    String userId;
    String userpic;
    String username;
    TextView txt_username_other;
    CircleImageView profile_Other,profile_me;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        profile_Other = findViewById(R.id.profile_Other);
        profile_me = findViewById(R.id.profile_me);
        txt_username_other = findViewById(R.id.txt_username_other);

        userId = getIntent().getExtras().getString("userId");
        username = getIntent().getExtras().getString("username");
        userpic = getIntent().getExtras().getString("pic");


        Picasso.get().load(userpic)
                .placeholder(R.drawable.avatar_prf)
                .into(profile_Other);
        txt_username_other.setText(username);

        SharedPreferences pref = getSharedPreferences("userInf", Context.MODE_PRIVATE);

        String picMe;

        if(pref.contains("pic"))
        {
            picMe = pref.getString("pic", "http://");
        }
        else {

            picMe = "http://";
        }

        Picasso.get().load(picMe)
                .placeholder(R.drawable.avatar_prf)
                .into(profile_me);





    }

    public void cllose(View view) {
        finish();
        overridePendingTransition(R.anim.nothing,R.anim.bottom_down);
    }

    public void MessageActiv(View view) {


        Intent intent = new Intent(MatchActivity.this, ChatActivity.class);
        intent.putExtra("key", userId);
        intent.putExtra("username", username);
        intent.putExtra("pic", userpic);
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions.makeSceneTransitionAnimation(MatchActivity.this, profile_Other, profile_Other.getTransitionName()).toBundle();
        }
        startActivity(intent, bundle);
     onresum = true;


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (onresum)
        {
            finish();
        }
    }
}
