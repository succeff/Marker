package com.bwie.appmaeker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private double mCurrentLat;
    private double mCurrentLon;
    private float mCurrentAccracy;
    private MyLocationData locData;
    boolean isFirstLoc = true; // 是否首次定位
    private LatLng ll;
    private InfoWindow mInfoWindow;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        iv = (ImageView) findViewById(R.id.miv);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapStatusUpdate update = MapStatusUpdateFactory
                        .newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
            }
        });
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
       // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        initOverlay();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                Toast.makeText(MainActivity.this,"onMarkerClick",Toast.LENGTH_SHORT).show();
                Button button = new Button(getApplicationContext());
                button.setBackgroundResource(R.drawable.popup);
                InfoWindow.OnInfoWindowClickListener listener = null;

                listener = new InfoWindow.OnInfoWindowClickListener() {
                    public void onInfoWindowClick() {
                        MakerBean makerBean = new Gson().fromJson(JsonStr.json, MakerBean.class);
                        for (int i = 0; i < makerBean.getList().size(); i++) {
                            MakerBean.ListBean listBean = makerBean.getList().get(i);
                            double lat = listBean.getLat();
                            double lng = listBean.getLng();
                            LatLng ll = new LatLng(lat, lng);
                            BitmapDescriptor bd = BitmapDescriptorFactory
                                    .fromResource(R.drawable.charge_no_net);
                            MarkerOptions mo = new MarkerOptions().position(ll).icon(bd)
                                    .zIndex(9).draggable(true);
                            mBaiduMap.addOverlay(mo);
                        }
                        Toast.makeText(MainActivity.this, "OnInfoWindowClickListener", Toast.LENGTH_SHORT).show();
                    }
                };
                LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
                mBaiduMap.showInfoWindow(mInfoWindow);

                return false;

            }
        });

    }
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).
                        zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
    public void initOverlay() {
        MakerBean makerBean = new Gson().fromJson(JsonStr.json, MakerBean.class);
        for (int i = 0; i < makerBean.getList().size(); i++) {
            MakerBean.ListBean listBean = makerBean.getList().get(i);
            double lat = listBean.getLat();
            double lng = listBean.getLng();
            LatLng ll = new LatLng(lat, lng);
            BitmapDescriptor bd = BitmapDescriptorFactory
                    .fromResource(R.drawable.charge_has_net);
            MarkerOptions mo = new MarkerOptions().position(ll).icon(bd)
                    .zIndex(9).draggable(true);
            mBaiduMap.addOverlay(mo);
        }
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
