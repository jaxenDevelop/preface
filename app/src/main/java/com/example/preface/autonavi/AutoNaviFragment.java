package com.example.preface.autonavi;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapRestrictionInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.example.preface.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoNaviFragment extends Fragment implements View.OnClickListener, AMapNaviViewListener, AMapNaviListener {
    private MapView auto_map;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private RelativeLayout naviBar, parentRelative;
    private long ClickMapTime;
    private ImageView location, favourite, search;
    private boolean ShowMaxZoomLocation;
    private RecyclerView poi_recycle, route_recycle;
    private PoiRecycleAdapter myRecycleAdapter;
    private RouteRecycleAdapter routeRecycleAdapter;
    private EditText search_edit;
    private List<Tip> searchList;
    private List<CalRouteResult> routeList;
    private PopupWindow searchPop, selectRoutePop;
    private Handler handler;
    //注册广播
    private ClickSearchResult clickBroadCaster;
    private AMapNaviView aMapNaviView;
    public static final  String AUTONAVIACTION = "ClickPosition";
    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();
    /**
     * 路线计算成功标志位
     */
    private boolean calculateSuccess = false;
    private boolean chooseRouteSuccess = false;
    /**
     * 路线的权值，重合路线情况下，权值高的路线会覆盖权值低的路线
     **/
    private int zindex = 1;
    /**
     * 当前用户选中的路线，在下个页面进行导航
     */
    private int routeIndex;

    //定位服务
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    private boolean isFirstLocation = true;
    private double lat, lng;
    private double endlat, endlng;
    private AMapNavi aMapNavi;
    private Marker mStartMarker;
    private Marker mEndMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.autonavi, container, false);
        ClickMapTime = 0;
        searchList = new ArrayList<>();
        routeList = new ArrayList<>();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        auto_map = view.findViewById(R.id.auto_map);
        aMapNaviView = view.findViewById(R.id.naviView);

        auto_map.onCreate(savedInstanceState);// 此方法必须重写
        aMapNaviView.onCreate(savedInstanceState);
        aMapNaviView.setAMapNaviViewListener(this);

        //获取AMapNavi实例
        try {
            aMapNavi = AMapNavi.getInstance(getActivity());
            //添加监听回调，用于处理算路成功
            aMapNavi.addAMapNaviListener(this);
            aMapNavi.setUseInnerVoice(true,false);
//            aMapNavi.addParallelRoadListener(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }

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

        initBroadCaster();
        initSearchPopWindow();
        SelectRoutePopWindow();

        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 0) {
                    myRecycleAdapter.notifyDataSetChanged();
                }
                super.handleMessage(msg);
            }
        };
        return view;
    }

    private void initBroadCaster() {
        clickBroadCaster = new ClickSearchResult();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AUTONAVIACTION);
        getActivity().registerReceiver(clickBroadCaster, intentFilter);
    }

    private void initSearchPopWindow() {
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.search_pop, null);
        search_edit = inflate.findViewById(R.id.search_edit);
        //初始化RecycleView
        myRecycleAdapter = new PoiRecycleAdapter(searchList, getActivity());

        poi_recycle = inflate.findViewById(R.id.poi_recycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        poi_recycle.setAdapter(myRecycleAdapter);
        //列表布局
        poi_recycle.setLayoutManager(linearLayoutManager);

        //输入框监听
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
                InputtipsQuery inputquery = new InputtipsQuery(editable.toString(), "");
                inputquery.setCityLimit(true);//限制在当前城市

                Inputtips inputTips = new Inputtips(getActivity(), inputquery);
                inputTips.setInputtipsListener(new Inputtips.InputtipsListener() {
                    @Override
                    public void onGetInputtips(List<Tip> list, int i) {
                        searchList.clear();
                        for (int j = 0; j < list.size(); j++) {
                            searchList.add(list.get(j));
                        }

                        Message message = Message.obtain();
                        message.what = 0;
                        handler.sendMessage(message);
                    }
                });
                inputTips.requestInputtipsAsyn();
            }
        });

        searchPop = new PopupWindow(inflate, getWindowWidth() / 3, getWindowHeight() * 4 / 5);
        searchPop.setOutsideTouchable(true);
        searchPop.setAnimationStyle(R.style.popAnimation);
    }

    private int getWindowHeight(){
        DisplayMetrics dm = new DisplayMetrics();
       getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
       return dm.heightPixels;
    }

    private int getWindowWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }


    private void SelectRoutePopWindow() {
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.select_route, null);
        //初始化RecycleView
        routeRecycleAdapter = new RouteRecycleAdapter(routeList, getActivity());

        route_recycle = inflate.findViewById(R.id.route_recycle);
        ImageView dismiss = inflate.findViewById(R.id.dismiss);
        Button confirm = inflate.findViewById(R.id.confirm);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        route_recycle.setAdapter(routeRecycleAdapter);
        //列表布局
        route_recycle.setLayoutManager(linearLayoutManager);

        selectRoutePop = new PopupWindow(inflate, getWindowWidth() / 3, getWindowHeight() * 4 / 5);
        selectRoutePop.setOutsideTouchable(false);
        selectRoutePop.setFocusable(false);
//        selectRoutePop.setAnimationStyle(R.style.RoutePopAnimation);

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectRoutePop.dismiss();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aMapNavi.startNavi(NaviType.GPS);
            }
        });
    }

    private void forbidClick() {
        location.setClickable(false);
        location.setFocusable(false);
        favourite.setClickable(false);
        favourite.setFocusable(false);
        search.setClickable(false);
        search.setFocusable(false);
    }

    private void allowClick() {
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

        aMap.setMinZoomLevel(3);
        aMap.setMaxZoomLevel(20);
        aMap.getUiSettings().setZoomControlsEnabled(false);

        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(10000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style


        aMap.addOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if ((System.currentTimeMillis() - ClickMapTime) > 6000) {
                    naviBar.setVisibility(View.VISIBLE);
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                    alphaAnimation.setDuration(200);
                    alphaAnimation.setRepeatCount(0);
                    alphaAnimation.setFillAfter(true);
                    naviBar.startAnimation(alphaAnimation);
                    allowClick();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                            alphaAnimation.setDuration(200);
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
                System.out.println("打印坐标：进入locationchange");
                if (location == null) {
                    return;
                }
                Bundle bundle = location.getExtras();
                System.out.println("打印坐标：准备进入bundle："  + " ");
                if (bundle != null && (bundle.getInt("errorCode", -1)) == 0) {//定位成功
                    //获取定位数据
                    //经度
                    lat = location.getLatitude();
                    //纬度
                    lng = location.getLongitude();

                    System.out.println("打印坐标："+lat);
//                    //实现第一次定位成功,将地图中心移动到定位点
//                    if (isFirstLocation){
//                        isFirstLocation = false;
//                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new
//                                LatLng(lat,lng),17);
//                        aMap.moveCamera(cameraUpdate);//将地图移动到定位坐标点
//                    }

                } else {
                    //错误信息
                    String error = bundle.getString("errorInfo");
                    Log.e("Map", error);
                }
            }
        });

        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        // 初始化Marker添加到地图
        mStartMarker = aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.start))));
        mEndMarker = aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.end))));

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
        switch (view.getId()) {
            case R.id.location:
                if (!ShowMaxZoomLocation) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat, lng), 19, 45, 0));
                    aMap.animateCamera(cameraUpdate);
                    ShowMaxZoomLocation = true;
                } else {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat, lng), 15, 0, 0));
                    aMap.animateCamera(cameraUpdate);
                    ShowMaxZoomLocation = false;
                }

                break;
            case R.id.favourite:

                break;
            case R.id.search:
                int FULL_SCREEN_FLAG =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN;
                searchPop.setFocusable(false);
                searchPop.showAsDropDown(naviBar, 0, 10, Gravity.RIGHT);
                searchPop.getContentView().setSystemUiVisibility(FULL_SCREEN_FLAG);
                searchPop.setFocusable(true);
                searchPop.update();
                break;
        }
    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {
        System.out.println("初始化成功");



    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        System.out.println("算路成功2");
//        if (ints.length == 1) {
//            onCalculateRouteSuccessOld();
//        } else {
//            onCalculateMultipleRoutesSuccessOld(ints);
//        }
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        //清空上次计算的路径列表。
        routeOverlays.clear();
        routeList.clear();

        aMap.animateCamera(CameraUpdateFactory.zoomTo(3));
        int[] routeIds = aMapCalcRouteResult.getRouteid();
        HashMap<Integer, AMapNaviPath> paths = aMapNavi.getNaviPaths();
        for (int i = 0; i < routeIds.length; i++) {
            AMapNaviPath path = paths.get(routeIds[i]);
            if (path != null) {


//                routeList.add(new CalRouteResult(path.getAllLength(), path.getAllTime(), path.getLightList().size(), path.getLabels(), path.getTollCost()));

                drawRoutes(routeIds[i], path);
            }
        }
    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }

    class ClickSearchResult extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("flag", 0)){
                case 0:
                    endlat = intent.getDoubleExtra("lat", 0);
                    endlng = intent.getDoubleExtra("lng", 0);
                    String name = intent.getStringExtra("name");

                    mEndMarker.setPosition(new LatLng(endlat, endlng));

                    searchPop.dismiss();
//            aMapNaviView.setVisibility(View.VISIBLE);

                    clearRoute();

                    // 起点信息
                    List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
                    startList.add(new NaviLatLng(lat, lng));
                    // 终点信息
                    List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
                    endList.add(new NaviLatLng(endlat, endlng));

                    int strategy = 0;
                    try {
                        //再次强调，最后一个参数为true时代表多路径，否则代表单路径
                        strategy = aMapNavi.strategyConvert(true, false, false, false, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (strategy >= 0) {
//                String carNumber = editText.getText().toString();
//                AMapCarInfo carInfo = new AMapCarInfo();
                        //设置车牌
//                carInfo.setCarNumber(carNumber);
                        //设置车牌是否参与限行算路
//                carInfo.setRestriction(true);
                        if (aMapNavi!=null){
                            aMapNavi.calculateDriveRoute(startList, endList, null, strategy);
                        }
                    }
                    break;

                case 1:
                    int routeId = intent.getIntExtra("routeId",0);
                    changeRoute(routeId);
                    break;
            }

        }
    }


    /**
     * 绘制路线
     */
    private void drawRoutes(int routeId, AMapNaviPath path) {
        //弹出路线选择窗口


        calculateSuccess = true;
        aMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        //路径绘制图层
        RouteOverLay routeOverLay = new RouteOverLay(aMap, path, getActivity());
        routeOverLay.setTrafficLine(false);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
        System.out.println("存入ID："+routeId);
        routeList.add(new CalRouteResult(routeId, path.getAllLength(), path.getAllTime(), path.getLightList().size(), path.getLabels(), path.getTollCost()));
        routeRecycleAdapter.notifyDataSetChanged();
        selectRoutePop.showAsDropDown(naviBar, 0, 10, Gravity.LEFT);

    }

    public void changeRoute(int routeId) {
        if (!calculateSuccess) {
            Toast.makeText(getActivity(), "请先算路", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 计算出来的路径只有一条
         */
        if (routeOverlays.size() == 1) {
            chooseRouteSuccess = true;
            //必须告诉AMapNavi 你最后选择的哪条路
            aMapNavi.selectRouteId(routeOverlays.keyAt(0));
            Toast.makeText(getActivity(), "导航距离:" + (aMapNavi.getNaviPath()).getAllLength() + "m" + "\n" + "导航时间:" + (aMapNavi.getNaviPath()).getAllTime() + "s", Toast.LENGTH_SHORT).show();
            return;
        }

        if (routeIndex >= routeOverlays.size()) {
            routeIndex = 0;
        }


        //突出选择的那条路
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            routeOverlays.get(key).setTransparency(0.4f);
        }
        RouteOverLay routeOverlay = routeOverlays.get(routeId);
        if(routeOverlay != null){
            routeOverlay.setTransparency(1);
            /**把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明**/
            routeOverlay.setZindex(zindex++);
        }
        //必须告诉AMapNavi 你最后选择的哪条路
        aMapNavi.selectRouteId(routeId);
        chooseRouteSuccess = true;

        /**选完路径后判断路线是否是限行路线**/
        AMapRestrictionInfo info = aMapNavi.getNaviPath().getRestrictionInfo();
        System.out.println("是否为空："+info);
        if (info != null){if (!TextUtils.isEmpty(info.getRestrictionTitle())) {
            Toast.makeText(getActivity(), info.getRestrictionTitle(), Toast.LENGTH_SHORT).show();
        }}

    }

    /**
     * 清除当前地图上算好的路线
     */
    private void clearRoute() {
        for (int i = 0; i < routeOverlays.size(); i++) {
            RouteOverLay routeOverlay = routeOverlays.valueAt(i);
            routeOverlay.removeFromMap();
        }
        routeOverlays.clear();
    }
}



