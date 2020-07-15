package org.techtown.consolenuri.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;

import org.techtown.consolenuri.R;

public class ChatImageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.chatimagefragment, container, false);

        Button closebutton = (Button)v.findViewById(R.id.closeFragment);
        ImageView chatimage = (ImageView)v.findViewById(R.id.ChatImageView);

        Bundle extra = this.getArguments();
        String imageurl = extra.getString("url");
        Log.e("받아온 url 값",imageurl);

        //글라이드로 사진 붙이기.
        Glide.with(this )
                .load(imageurl)
                .centerCrop()
                .override(1400,1400)
                .into(chatimage);

        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //프래그먼트 종료 시키기.
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().remove(ChatImageFragment.this).commit();
            }
        });


        return v;
    }
}
