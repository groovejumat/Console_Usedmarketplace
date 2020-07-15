package org.techtown.consolenuri.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import org.techtown.consolenuri.model.TradingProduct;

import java.util.ArrayList;

import static org.techtown.consolenuri.app.MyApplication.TAG;

public class TradingProductsAdapter extends RecyclerView.Adapter<TradingProductsAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<TradingProduct> TradingProductsArrayList;
    private static String today;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView trproductname, message, trprice, produtprogress,createdat;
        public ImageView trthumbnail;

        public ViewHolder(View view) {
            super(view);
            trproductname = (TextView) view.findViewById(R.id.trprnameView); //거래중제품이름
            trthumbnail = (ImageView) view.findViewById(R.id.trprThumbnailView); //거래중제품썸네일
            produtprogress = (TextView) view.findViewById(R.id.prprogressView); //제품거래상태확인
            createdat = (TextView) view.findViewById(R.id.createdatView); //제품날짜
            trprice = (TextView) view.findViewById(R.id.trprpriceView); //거래중인제품가격

        }

        //컨텍스트 메뉴 인데 일단 살려는 놓자.
//        @Override
//        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            MenuItem Edit = menu.add(Menu.NONE, 1001, 1, "배송지 확인");
//            MenuItem Delete = menu.add(Menu.NONE, 1002, 2, "배송처리");
//            MenuItem Delete = menu.add(Menu.NONE, 1003, 2, "배송처리");
//            Edit.setOnMenuItemClickListener(onMenuItemClickListener);
//            Delete.setOnMenuItemClickListener(onMenuItemClickListener);
//        }
//
//        private final MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.add_menu:
//                        return true;
//
//                    case R.id.delete_menu:
//                        return true;
//                }
//                return false;
//            }
//        };
    }


    public TradingProductsAdapter(Context mContext, ArrayList<TradingProduct> TradingProductArrayList) {
        this.mContext = mContext;
        this.TradingProductsArrayList = TradingProductArrayList;

        //Calendar calendar = Calendar.getInstance();
        //today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public TradingProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trading_product, parent, false);

        return new TradingProductsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TradingProductsAdapter.ViewHolder holder, int position) {
        //아이템 정보값을 가지고 장난치기.
        TradingProduct trproduct = TradingProductsArrayList.get(position);
        Log.e(TAG, trproduct.getProductname());

        //뷰홀더에 붙이기
        holder.trproductname.setText(trproduct.getProductname());
        holder.trprice.setText("결제금액 : " + trproduct.getPrice() + " 원");
        holder.createdat.setText(trproduct.getCreated_at()); // 작성한 시점을 추가해 줍니다.

        //productprogress의 경우는 받아온 값에 따라 다른 색상의 텍스트를 처리.
        String productprogress = trproduct.getProductprgress();
        switch (productprogress) {
            case "0": holder.produtprogress.setText("[결제 완료]");
                      holder.produtprogress.setTextColor(Color.GRAY);
                break;
            case "1": holder.produtprogress.setText("[결제 확인]");
                      holder.produtprogress.setTextColor(Color.BLACK);
                break;
            case "2": holder.produtprogress.setText("[배송 준비중]");
                      holder.produtprogress.setTextColor(Color.GREEN);
                break;
            case "3": holder.produtprogress.setText("[배송 중]");
                      holder.produtprogress.setTextColor(Color.BLUE);
                break;
            case "4": holder.produtprogress.setText("[구매 확정]");
                      holder.produtprogress.setTextColor(Color.RED);
                break;
        }
        //holder.produtprogress.setText(trproduct.getProductprgress());


        Glide.with(holder.itemView.getContext())
                .load(trproduct.getThumbnail())// image url
                //.override(1440, 1440) // resizing
                .centerCrop()
                .into(holder.trthumbnail);
    }

    @Override
    public int getItemCount() {
        return TradingProductsArrayList.size();
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
