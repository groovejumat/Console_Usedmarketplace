package org.techtown.consolenuri.adapter;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.techtown.consolenuri.R;
import org.techtown.consolenuri.model.Product;

import java.util.ArrayList;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<Product> ProductsArrayList;
    private static String today;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView productname, message, price, category,createdat;
        public ImageView thumbnail;

        public ViewHolder(View view) {
            super(view);
            productname = (TextView) view.findViewById(R.id.productnameView);
            thumbnail = (ImageView) view.findViewById(R.id.productimageView);
            category = (TextView) view.findViewById(R.id.categoryView);
            createdat = (TextView) view.findViewById(R.id.createdatView);
            price = (TextView) view.findViewById(R.id.productpriceView);
        }
    }


    public ProductsAdapter(Context mContext, ArrayList<Product> chatRoomArrayList) {
        this.mContext = mContext;
        this.ProductsArrayList = chatRoomArrayList;

        //Calendar calendar = Calendar.getInstance();
        //today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);

        return new ProductsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProductsAdapter.ViewHolder holder, int position) {
        //아이템 정보값을 가지고 장난치기.
        Product product = ProductsArrayList.get(position);

        //뷰홀더에 붙이기
        holder.productname.setText(product.getProductname());
        holder.price.setText(product.getPrice());
        holder.category.setText(product.getCategory());
        holder.createdat.setText(product.getCreated_at()); // 작성한 시점을 추가해 줍니다.

        //이부분에서 읽지 않고 읽은 부분에 대해서 리사이클러뷰 나타냄을 처리해준다.
//        if (chatRoom.getUnreadCount() > 0) {
//            holder.count.setText(String.valueOf(chatRoom.getUnreadCount()));
//            holder.count.setVisibility(View.VISIBLE);
//        } else {
//            holder.count.setVisibility(View.GONE);
//        }

        Glide.with(holder.itemView.getContext())
                .load(product.getThumbnail())// image url
                //.override(1440, 1440) // resizing
                .centerCrop()
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return ProductsArrayList.size();
    }


    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ChatRoomsAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ChatRoomsAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
