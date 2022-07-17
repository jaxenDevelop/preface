package com.example.preface.autonavi;

import static com.example.preface.autonavi.AutoNaviFragment.AUTONAVIACTION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Tip;
import com.example.preface.R;

import java.util.List;

public class PoiRecycleAdapter extends RecyclerView.Adapter<PoiRecycleAdapter.myHolder> {
    public List<Tip> list;
    public Context context;

    public PoiRecycleAdapter(List list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.autonavi_search_recycleitem, parent, false);
        final myHolder myHolder = new myHolder(view);
        return myHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull myHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.name.setText(list.get(position).getName());
        holder.address.setText(list.get(position).getDistrict() + list.get(position).getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list.get(position).getPoint() != null)
                {
                    Intent intent = new Intent();
                    intent.setAction(AUTONAVIACTION);
                    intent.putExtra("flag", 0);
                    intent.putExtra("name", list.get(position).getName());
                    intent.putExtra("lat", list.get(position).getPoint().getLatitude());
                    intent.putExtra("lng", list.get(position).getPoint().getLongitude());
                    context.sendBroadcast(intent);
                }
                else {
                    Toast.makeText(context, "该地址无效，请重新选择！", Toast.LENGTH_LONG).show();
                }


//                //获取导航Manager
//                try {
//                    AMapNavi mAMapNavi = AMapNavi.getInstance(context);
//                    // 起点信息
//                    List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
//                    startList.add(new NaviLatLng(lat,lng));
//
//                    System.out.println("测试数据："+ lat + " "+ lng);
//                    // 终点信息
//                    List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
//                    endList.add(new NaviLatLng(list.get(position).getPoint().getLatitude(), list.get(position).getPoint().getLongitude()));
//                    System.out.println("测试数据："+ list.get(position).getPoint().getLatitude() + " "+ list.get(position).getPoint().getLongitude());
//                    // 经纬度算路
//                    mAMapNavi.calculateDriveRoute(startList, endList, null, PathPlanningStrategy.DRIVING_MULTIPLE_ROUTES_DEFAULT);
//
//                } catch (AMapException e) {
//                    e.printStackTrace();
//                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class myHolder extends RecyclerView.ViewHolder {
        TextView name, address;
        View ItemView;

        private myHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            ItemView = itemView;
        }
    }

}
