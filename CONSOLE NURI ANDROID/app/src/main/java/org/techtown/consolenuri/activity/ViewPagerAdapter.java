package org.techtown.consolenuri.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import org.techtown.consolenuri.R;

import java.util.ArrayList;

//ProductActivity상에서 이미지를 띄워주기 위한 ViewPager기능 입니다.
public class ViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> imageList;

    //뷰페이저에 해당 값을 받는다.
    public ViewPagerAdapter(Context context, ArrayList<String> imageList)
    {
        this.mContext = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pagerview_layout, null);

        ImageView imageView = view.findViewById(R.id.imageView);

        Glide.with(container.getContext())
                .load(imageList.get(position))// image url
                //.override(1440, 1440) // resizing
                .centerCrop()
                .into(imageView);  // imageview object

        //imageView.setImageResource(imageList.get(position));

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == (View)o);
    }

}