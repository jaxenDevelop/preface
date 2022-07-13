package com.example.preface.autonavi;


import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.preface.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AutoNaviFragment extends Fragment implements View.OnClickListener {
    private MapView auto_map;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private RelativeLayout naviBar, parentRelative;
    private long ClickMapTime;
    private ImageView location, favourite, search;
    private boolean ShowMaxZoomLocation;

    //定位服务
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    private boolean isFirstLocation = true;
    private  double lat, lng;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.autonavi, container, false);
        ClickMapTime = 0;
//        setTerrainEnable(true);
//       updatePrivacyShow(getActivity(), true, true);
//        updatePrivacyAgree(getActivity(), true);

        auto_map = view.findViewById(R.id.auto_map);
        auto_map.onCreate(savedInstanceState);// 此方法必须重写

        if (aMap == null)
            aMap = auto_map.getMap();

        //NaviBar
        naviBar = view.findViewById(R.id.navi_bar);
        parentRelative = view.findViewById(R.id.parentRelative);
        location = view.findViewById(R.id.location);
        favourite = view.findViewById(R.id.favourite);
        search = view.findViewById(R.id.search);

        forbidClick();
        setMapAttribute();

        location.setOnClickListener(this);
        favourite.setOnClickListener(this);
        search.setOnClickListener(this);

        ShowMaxZoomLocation = false;

//        initSearchPopWindow();
        return view;
    }

    private void initSearchPopWindow() {
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.search_pop, null);
        PopupWindow window = new PopupWindow(inflate, 1000,1200);
        window.setOutsideTouchable(true);
        window.setAnimationStyle(R.style.popAnimation);
        window.showAsDropDown(naviBar,0,0, Gravity.RIGHT);
    }

    private void forbidClick(){
        location.setClickable(false);
        location.setFocusable(false);
        favourite.setClickable(false);
        favourite.setFocusable(false);
        search.setClickable(false);
        search.setFocusable(false);
    }

    private void allowClick(){
        location.setClickable(true);
        location.setFocusable(true);
        favourite.setClickable(true);
        favourite.setFocusable(true);
        search.setClickable(true);
        search.setFocusable(true);
    }

    private void setMapAttribute() {
        aMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        aMap.setMapLanguage(AMap.CHINESE);
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 卫星地图模式
        aMap.setTrafficEnabled(true);// 显示实时交通状况

        aMap.setMinZoomLevel(15);
        aMap.setMaxZoomLevel(20);
        aMap.getUiSettings().setZoomControlsEnabled(false);

        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(5000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style


        aMap.addOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if ((System.currentTimeMillis() - ClickMapTime) > 7000) {
                    naviBar.setVisibility(View.VISIBLE);
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                    alphaAnimation.setDuration(1000);
                    alphaAnimation.setRepeatCount(0);
                    alphaAnimation.setFillAfter(true);
                    naviBar.startAnimation(alphaAnimation);
                    allowClick();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                            alphaAnimation.setDuration(500);
                            alphaAnimation.setRepeatCount(0);
                            alphaAnimation.setFillAfter(true);
                            naviBar.startAnimation(alphaAnimation);

                            naviBar.setVisibility(View.GONE);
                            forbidClick();
                        }
                    }, 5000);
                    ClickMapTime = System.currentTimeMillis();
                }
            }
        });

        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (location == null){
                    return;
                }
                Bundle bundle = location.getExtras();
                if (bundle != null && (bundle.getInt("errorCode",-1)) == 0){//定位成功
                    //获取定位数据
                    //经度
                    lat = location.getLatitude();
                    //纬度
                    lng = location.getLongitude();
//                    //实现第一次定位成功,将地图中心移动到定位点
//                    if (isFirstLocation){
//                        isFirstLocation = false;
//                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new
//                                LatLng(lat,lng),17);
//                        aMap.moveCamera(cameraUpdate);//将地图移动到定位坐标点
//                    }

                }else{
                    //错误信息
                    String error = bundle.getString("errorInfo");
                    Log.e("Map",error);
                }
            }
        });

        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        auto_map.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        auto_map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        auto_map.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        auto_map.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.location:
                if (!ShowMaxZoomLocation)
                {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng( lat,lng),19,45,0));
                    aMap.animateCamera(cameraUpdate);
                    ShowMaxZoomLocation = true;
                }
                else {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat,lng),15,0,0));
                    aMap.animateCamera(cameraUpdate);
                    ShowMaxZoomLocation = false;
                }

                break;
            case R.id.favourite:

                break;
            case R.id.search:
//                PopupWindow window = new PopupWindow()
                initSearchPopWindow();
                break;
        }
    }
}



