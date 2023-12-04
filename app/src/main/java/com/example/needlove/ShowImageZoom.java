package com.example.needlove;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class ShowImageZoom extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_zoom);



        PhotoView photo_view;
        photo_view = findViewById(R.id.photo_view);

        String pic= getIntent().getExtras().getString("pic");

        Picasso.get().load(pic).into(photo_view);
    }
}
