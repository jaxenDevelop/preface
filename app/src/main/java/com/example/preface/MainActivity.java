package com.example.preface;


import static com.amap.api.services.core.ServiceSettings.updatePrivacyAgree;
import static com.amap.api.services.core.ServiceSettings.updatePrivacyShow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.preface.autonavi.AutoNaviFragment;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity {
    private TextView showTime;
    private Handler handler;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String perms[] = {Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms,1);
        }

        updatePrivacyShow(getApplicationContext(), true, true);
        updatePrivacyAgree(getApplicationContext(), true);

        setTransparent();
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/")
                                .setFontAttrId(io.github.inflationx.calligraphy3.R.attr.fontPath)
                                .build()))
                .build());

        showTime = findViewById(R.id.show_time);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                switch (message.what) {
                    case 0:
                        showTime.setText(message.obj.toString());
                        break;
                }
                return false;
            }
        });
        initTime();

        viewPager = findViewById(R.id.view_pager);

        /**初始化FragmentList**/
        List<Fragment> list = new ArrayList<>();
        list.add(new AutoNaviFragment());
//        list.add(new TurntableFragment());
//        list.add(new AboutFragment());

        myFragAdapter myFragAdapter = new myFragAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(myFragAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(1000);
                        Message message = Message.obtain();
                        message.what = 0;
                        Calendar calendar = Calendar.getInstance();
                        message.obj = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
                                + calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR_OF_DAY))
                                + ":" + (calendar.get(Calendar.MINUTE) < 10 ? "0"
                                + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE));
                        handler.sendMessage(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (true);
            }
        }).start();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    /**
     * 1.使状态栏透明
     * @param
     */
    private void setTransparent() {
        // SYSTEM_UI_FLAG_FULLSCREEN只适合Android4.1以上版本,正常情况下隐藏导航栏，也会把状态栏给隐藏掉
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setTransparent();
    }
}