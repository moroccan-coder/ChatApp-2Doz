package com.example.needlove.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.needlove.Model.Slide;
import com.example.needlove.Model.TestSlideItem;
import com.example.needlove.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TestSlideAdapter extends RecyclerView.Adapter<TestSlideAdapter.SliderViewHolder>{

    List<Slide> slideItems;
    ViewPager2 viewPager2;

    imgClick itemClickLisener;

    public TestSlideAdapter(List<Slide> slideItems, ViewPager2 viewPager2) {
        this.slideItems = slideItems;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.test_slide_item_container,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {


        Picasso.get().load(slideItems.get(position).getImageLink()).placeholder(R.drawable.avatar_prf).into(holder.imageSlide);
    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageSlide;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);

            imageSlide = itemView.findViewById(R.id.imageSlide);

            imageSlide.setOnClickListener(this);
        }




        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION)
            {
                itemClickLisener.onImageClick(position,imageSlide);
            }


        }
    }


    public interface imgClick{

        void onImageClick(int position,ImageView img);
    }

    public void setonImageClickLisener(imgClick imgClickk)
    {
        itemClickLisener = imgClickk;
    }

}
