package com.example.needlove.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.example.needlove.Model.Slide;
import com.example.needlove.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SliderPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<Slide> mList;
    private int WichSlid=0;

    public SliderPagerAdapter(Context mContext, List<Slide> mList,int slid) {
        this.mContext = mContext;
        this.mList = mList;
        this.WichSlid = slid;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View slideLayout = null;

        if(WichSlid == 1)
        {
            slideLayout = inflater.inflate(R.layout.slide_item,null);
            ImageView slideImg = slideLayout.findViewById(R.id.img_slide);

            TextView slideText = slideLayout.findViewById(R.id.slide_title);
            slideImg.setImageResource(mList.get(position).getImage());
            slideText.setText(mList.get(position).getTitle());
        }
        else if (WichSlid ==2)
        {
            slideLayout = inflater.inflate(R.layout.slide_item2,null);
            ImageView slideImg = slideLayout.findViewById(R.id.img_slide);
            String imglnk = mList.get(position).getImageLink();
            Picasso.get().load(imglnk).placeholder(R.drawable.avatar_prf).into(slideImg);
        }
        else if (WichSlid ==3)
        {
            slideLayout = inflater.inflate(R.layout.slide_item3,null);
            ImageView slideImg = slideLayout.findViewById(R.id.img_slide);
            String imglnk = mList.get(position).getImageLink();
            Picasso.get().load(imglnk).placeholder(R.drawable.avatar_prf).into(slideImg);
        }



        container.addView(slideLayout);

        return slideLayout;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view,Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
