package com.example.preface.autonavi;

import static com.example.preface.autonavi.AutoNaviFragment.AUTONAVIACTION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Tip;
import com.example.preface.R;

import java.util.List;

public class RouteRecycleAdapter extends RecyclerView.Adapter<RouteRecycleAdapter.myHolder> {
    public List<CalRouteResult> list;
    public Context context;
    private int ClickPosition = 0;

    public RouteRecycleAdapter(List list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.autonavi_route_recycleitem, parent, false);
        final myHolder myHolder = new myHolder(view);
        return myHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull myHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.label.setText(list.get(position).getLabel());

        if (list.get(position).getTime() / 60 < 60)
            holder.time.setText(list.get(position).getTime() / 60 + "分钟");
        else if (60 < list.get(position).getTime() / 60 && list.get(position).getTime() / 60  < 1440 )
            holder.time.setText(list.get(position).getTime() / 60 / 60 +  "小时" + list.get(position).getTime() / 60 % 60 + "分钟");
        else if (list.get(position).getTime() / 60 > 1440 )
            holder.time.setText(list.get(position).getTime() / 60 / 60 / 24  +  "天" + list.get(position).getTime() / 60 / 60 % 24 + "小时");

        holder.distance.setText(list.get(position).getDistance() / 1000 + "公里" );
        holder.lights.setText(list.get(position).getLights() + "个");

        if (list.get(position).getCost() != 0)
        {
            holder.img_money.setVisibility(View.VISIBLE);
            holder.cost.setVisibility(View.VISIBLE);
            holder.cost.setText(list.get(position).getCost() + "");
        }
        else {
            holder.img_money.setVisibility(View.GONE);
            holder.cost.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickPosition = position;
                notifyDataSetChanged();

                Intent intent = new Intent();
                intent.setAction(AUTONAVIACTION);
                intent.putExtra("flag", 1);
                intent.putExtra("routeId", list.get(position).getRouteId());
                System.out.println("打印ID："+list.get(position).getRouteId());
                context.sendBroadcast(intent);
            }
        });

        if (position == ClickPosition)
        {
            holder.label.setTextColor(Color.parseColor("#3069F6"));
            holder.time.setTextColor(Color.parseColor("#3069F6"));
            holder.distance.setTextColor(Color.parseColor("#3069F6"));
            holder.lights.setTextColor(Color.parseColor("#3069F6"));
            holder.cost.setTextColor(Color.parseColor("#3069F6"));
            holder.divider_point.setTextColor(Color.parseColor("#3069F6"));
            holder.img_money.setImageResource(R.drawable.money_blue);
            holder.light.setImageResource(R.drawable.light_blue);
        }
        else {
            holder.label.setTextColor(Color.parseColor("#808080"));
            holder.time.setTextColor(Color.parseColor("#808080"));
            holder.distance.setTextColor(Color.parseColor("#808080"));
            holder.lights.setTextColor(Color.parseColor("#808080"));
            holder.cost.setTextColor(Color.parseColor("#808080"));
            holder.divider_point.setTextColor(Color.parseColor("#808080"));
            holder.img_money.setImageResource(R.drawable.money);
            holder.light.setImageResource(R.drawable.light);

        }


    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    class myHolder extends RecyclerView.ViewHolder {
        TextView label, time, distance,lights,cost, divider_point;
        ImageView img_money, light;
        View ItemView;

        private myHolder(View itemView) {
            super(itemView);

            label = itemView.findViewById(R.id.label);
            time = itemView.findViewById(R.id.time);
            distance = itemView.findViewById(R.id.distance);
            lights = itemView.findViewById(R.id.lights);

            img_money = itemView.findViewById(R.id.img_money);
            cost = itemView.findViewById(R.id.cost);
            light = itemView.findViewById(R.id.light);
            divider_point = itemView.findViewById(R.id.divider_point);
            ItemView = itemView;
        }
    }

}
