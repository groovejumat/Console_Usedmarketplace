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

import org.techtown.consolenuri.R;
import org.techtown.consolenuri.model.Address;

import java.util.ArrayList;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder>{

    //RecyclerView.Adapter<ProductsAdapter.ViewHolder>

    private Context mContext;
    private ArrayList<Address> AddressArrayList;
    private static String today;


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView addressname, postaladdrsse, addressdetail, addressrequest, addresscontact;
        public ImageView deleteaddress;
        //public ImageView thumbnail;

        //뷰홀더에 정보 세팅하기.
        public ViewHolder(View view) {
            super(view);
            addressname = (TextView) view.findViewById(R.id.nameView);
            postaladdrsse = (TextView) view.findViewById(R.id.addressView);
            addressdetail = (TextView) view.findViewById(R.id.detailView);
            addressrequest = (TextView) view.findViewById(R.id.requestView);
            addresscontact = (TextView) view.findViewById(R.id.contactView);
            //deleteaddress = (ImageView) view.findViewById(R.id.deleteAdressView);

            //캔슬버튼에 클릭 리스너를 달아서 리사이클러뷰 내에 있는 리스트를 삭제할 수 있도록 하자.
//            deleteaddress.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View v) {
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                    builder.setTitle("AlertDialog Title");
//                    builder.setMessage("AlertDialog Content");
//                    builder.setPositiveButton("예",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
//                                    // 이 버튼 클릭시 삭제 진행
//                                    Log.e("엑스버튼의 위치 값은 : ", getAdapterPosition()+"");
//                                    //해당 위치 값에 있는 데이터를 삭제 처리 하도록 한다.
//                                    AddressArrayList.remove(getAdapterPosition());
//                                    notifyItemRemoved(getAdapterPosition()); //삭제 처리하기
//                                    dialog.dismiss();
//                                }
//                            });
//                    builder.setNegativeButton("아니오",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    //Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
//                                    dialog.dismiss();
//                                }
//                            });
//                    builder.show();
//
//                }
//            });
        }
    }


    public DeliveryAdapter(Context mContext, ArrayList<Address> chatRoomArrayList) {
        this.mContext = mContext;
        this.AddressArrayList = chatRoomArrayList;

        //Calendar calendar = Calendar.getInstance();
        //today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public DeliveryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);

        return new DeliveryAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeliveryAdapter.ViewHolder holder, int position) {
        //아이템 정보값을 가지고 장난치기.
        Address address = AddressArrayList.get(position);

        //뷰홀더에 붙이기
        holder.addressname.setText(address.getName());
        holder.postaladdrsse.setText(address.getAddress());
        holder.addressdetail.setText(address.getAddressdetail());
        holder.addresscontact.setText(address.getContact());
        holder.addressrequest.setText(address.getRequset());

    }

    @Override
    public int getItemCount() {
        return AddressArrayList.size();
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