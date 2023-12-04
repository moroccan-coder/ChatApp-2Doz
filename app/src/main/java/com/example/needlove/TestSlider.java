package com.example.needlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.needlove.Adapters.TestSlideAdapter;
import com.example.needlove.Model.TestSlideItem;

import java.util.ArrayList;
import java.util.List;

public class TestSlider extends AppCompatActivity implements TestSlideAdapter.imgClick {

    ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_slider);

        viewPager2 = findViewById(R.id.viewpagerImageSlider2);


        List<TestSlideItem> slideItems = new ArrayList<>();
        slideItems.add(new TestSlideItem(R.drawable.bg1));
        slideItems.add(new TestSlideItem(R.drawable.bg2));
        slideItems.add(new TestSlideItem(R.drawable.bg3));
        slideItems.add(new TestSlideItem(R.drawable.bg5));

        TestSlideAdapter mAdapter;
       // mAdapter = new TestSlideAdapter(slideItems, viewPager2);
       // viewPager2.setAdapter(mAdapter);

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



       // mAdapter.setonImageClickLisener(this);

    }

    @Override
    public void onImageClick(int position, ImageView img) {
        Toast.makeText(TestSlider.this, " "+position, Toast.LENGTH_SHORT).show();
    }
}
