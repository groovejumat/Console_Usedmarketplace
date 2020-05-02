package org.techtown.consolenuri.FileUpload;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.techtown.consolenuri.R;

import java.util.ArrayList;

//이미지 등록 리사이클러뷰에 대한 어댑터를 장착.
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<Uri> items = new ArrayList<>();
    private Context context;
    private View.OnClickListener onClickItem;

    public MyAdapter(Context context, ArrayList<Uri> itemList, View.OnClickListener onClickItem) {
        this.context = context;
        this.items = itemList;
        this.onClickItem = onClickItem;
    }

    //어레이 리스트 데이터셋 변경. 초기화 하는건 아님.
    public void setItems(ArrayList<Uri> itemList){
        this.items=itemList;
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder viewHolder, int position) {

        Uri item = items.get(position);

        //글라이드 이미지 장착 부분..... 정사각형으로 맞추려면 어떻게 해야 좋을까??
        Glide.with(viewHolder.itemView.getContext())
                .load(item)
                .into(viewHolder.ivMovie);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivMovie;
        ImageView cancel;

        ViewHolder(View itemView) {
            super(itemView);

            ivMovie = itemView.findViewById(R.id.item_imageview);
            cancel = itemView.findViewById(R.id.CanceliconView); // 진짜 변수이름 정하는거 개 같네...

            //캔슬버튼에 클릭 리스너를 달아서 리사이클러뷰 내에 있는 리스트를 삭제할 수 있도록 하자.
            cancel.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Log.e("엑스버튼의 위치 값은 : ", getAdapterPosition()+"");
                    //해당 위치 값에 있는 데이터를 삭제 처리 하도록 한다.
                    items.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition()); //삭제 처리하기
                }
            });

        }
    }
}