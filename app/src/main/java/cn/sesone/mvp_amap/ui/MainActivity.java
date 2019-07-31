package cn.sesone.mvp_amap.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.google.android.material.navigation.NavigationView;
import com.rui.common_base.base.BaseMVPActivity;
import com.rui.common_base.mvp.IPresenter;
import com.rui.common_base.util.LOG;
import com.rui.common_base.widget.loading.LoadDialog;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import cn.sesone.mvp_amap.R;
import cn.sesone.mvp_amap.lib.LocationTask;
import cn.sesone.mvp_amap.lib.OnLocationGetListener;
import cn.sesone.mvp_amap.lib.PositionEntity;
import cn.sesone.mvp_amap.lib.RegeocodeTask;
import cn.sesone.mvp_amap.lib.RouteTask;
import cn.sesone.mvp_amap.lib.Utils;
import cn.sesone.mvp_amap.utils.AMapUtil;
import cn.sesone.mvp_amap.utils.ToastUtil;
import cn.sesone.mvp_amap.utils.overlay.WalkRouteOverlay;

public class MainActivity extends BaseMVPActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AMap.OnMapTouchListener,
        AMap.OnMapLoadedListener,
        AMap.OnCameraChangeListener,
        AMap.OnMapClickListener,
        RouteSearch.OnRouteSearchListener,
        AMap.InfoWindowAdapter,
        OnLocationGetListener,
        RouteTask.OnRouteCalculateListener {


    @BindView(R.id.iv_user)
    ImageView      ivUser;
    @BindView(R.id.tv_title)
    TextView       tvTitle;
    @BindView(R.id.iv_action)
    ImageView      ivAction;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout   drawer;
    @BindView(R.id.recyclerView)
    RecyclerView   recyclerView;
    @BindView(R.id.map)
    MapView        mMapView;

    //初始化地图控制器对象
    AMap aMap;
    // 定位
    private LocationTask  mLocationTask;
    // 逆地理编码功能
    private RegeocodeTask mRegeocodeTask;
    //绘制点标记
    private Marker        mPositionMark, mInitialMark, tempMark;//可移动、圆点、点击
    //初始坐标、移动记录坐标
    private LatLng mStartPosition, mRecordPositon;
    //默认添加一次
    private boolean mIsFirst     = true;
    //就第一次显示位置
    private boolean mIsFirstShow = true;

    private LatLng           initLocation;
    private ValueAnimator    animator = null;//坐标动画
    private BitmapDescriptor initBitmap, moveBitmap, smallIdentificationBitmap, bigIdentificationBitmap;//定位圆点、可移动、所有标识（车）
    private RouteSearch mRouteSearch;

    private       WalkRouteResult mWalkRouteResult;
    private       LatLonPoint     mStartPoint           = null;//起点，116.335891,39.942295
    private       LatLonPoint     mEndPoint             = null;//终点，116.481288,39.995576
    private final int             ROUTE_TYPE_WALK       = 3;
    private       boolean         isClickIdentification = false;
    WalkRouteOverlay walkRouteOverlay;//路线
    private String[] time;
    private String   distance;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViewState(Bundle savedInstanceState) {
        super.initViewState(savedInstanceState);
        mMapView.onCreate(savedInstanceState);
        navigationView.setNavigationItemSelectedListener(this);
        ivUser.setOnClickListener(view -> {
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        initBitmap();
        initAMap();
        initLocation();
        RouteTask.getInstance(getApplicationContext())
                .addRouteCalculateListener(this);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected IPresenter createPresenter() {
        return null;
    }


    private void initBitmap() {
        initBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker);
        moveBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_loaction_start);
        smallIdentificationBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.stable_cluster_marker_one_normal);
        bigIdentificationBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.stable_cluster_marker_one_normal);
    }

    /**
     * 初始化地图控制器对象
     */
    private void initAMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            //            //搜索
            mRouteSearch = new RouteSearch(this);
            mRouteSearch.setRouteSearchListener(this);
            //UiSettings 主要是对地图上的控件的管理，比如指南针、logo位置（不能隐藏）.....
            //管理缩放控件
            aMap.getUiSettings().setZoomControlsEnabled(false);
            //开启以中心点进行手势操作的方法
            aMap.getUiSettings().setGestureScaleByMapCenter(true);
            //            设置是否允许缩放手势
            //aMap.getUiSettings().setScrollGesturesEnabled(false);
            //            设置触摸地图事件监听者
            aMap.setOnMapTouchListener(this);
            //            设置地图单击事件监听者
            aMap.setOnMapClickListener(this);
            //            设置amap加载成功事件监听
            aMap.setOnMapLoadedListener(this);
            //            高德地图监听地图状态改变的方法
            aMap.setOnCameraChangeListener(this);
            // 绑定 Marker 被点击事件
            aMap.setOnMarkerClickListener(markerClickListener);
            // 设置自定义InfoWindow样式
            aMap.setInfoWindowAdapter(this);
        }
    }

    // 定义 Marker 点击事件监听
    AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {

        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        @Override
        public boolean onMarkerClick(final Marker marker) {
            LOG.e("点击的Marker");
            LOG.e(marker.getPosition() + "");
            isClickIdentification = true;
            if (tempMark != null) {
                tempMark.setIcon(smallIdentificationBitmap);
                walkRouteOverlay.removeFromMap();
                tempMark = null;
            }
            startAnim(marker);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                        tempMark = marker;
                        LOG.e(mPositionMark.getPosition().latitude + "===" + mPositionMark.getPosition().longitude);
                        mStartPoint = new LatLonPoint(mRecordPositon.latitude, mRecordPositon.longitude);
                        mPositionMark.setPosition(mRecordPositon);
                        mEndPoint = new LatLonPoint(marker.getPosition().latitude, marker.getPosition().longitude);
                        marker.setIcon(bigIdentificationBitmap);
                        marker.setPosition(marker.getPosition());
                        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);//路径规划
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return true;
        }
    };

    private void startAnim(Marker marker) {
        ScaleAnimation anim = new ScaleAnimation(1.0f, 1.3f, 1.0f, 1.3f);
        anim.setDuration(300);
        marker.setAnimation(anim);
        marker.startAnimation();
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(this, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(this, "终点未设置");
        }
        showDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }

    private void showDialog() {
        LoadDialog loadDialog = LoadDialog.getInstance();
        loadDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.load_dialog);
        LoadDialog.getInstance().show(getSupportFragmentManager(), "");
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        mLocationTask = LocationTask.getInstance(getApplicationContext());
        mLocationTask.setOnLocationGetListener(this);
        mRegeocodeTask = new RegeocodeTask(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * 当用户触摸地图时回调函数
     */
    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (motionEvent.getPointerCount() >= 2) {
            aMap.getUiSettings().setScrollGesturesEnabled(false);
        } else {
            aMap.getUiSettings().setScrollGesturesEnabled(true);
        }
    }

    /**
     * 地图加载完成
     */
    @Override
    public void onMapLoaded() {
        mLocationTask.startLocate();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    /**
     * 这个方法为地图移动后触发的
     * 图层为枚举值为 3-20
     */
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LOG.e("onCameraChangeFinish" + cameraPosition.target);
        if (!isClickIdentification) {
            mRecordPositon = cameraPosition.target;
        }
        mStartPosition = cameraPosition.target;
        mRegeocodeTask.setOnLocationGetListener(this);
        mRegeocodeTask.search(mStartPosition.latitude, mStartPosition.longitude);
        Utils.removeMarkers();
        DisplayMetrics dm = getResources().getDisplayMetrics();

        LOG.e(mMapView.getMeasuredWidth() + ":"+ dm.widthPixels);
        if (mIsFirst) {
            Utils.addEmulateData(aMap, mStartPosition);
            createInitialPosition(cameraPosition.target.latitude, cameraPosition.target.longitude);
            createMovingPosition();
            mIsFirst = false;
        }
        if (mInitialMark != null) {
            mInitialMark.setToTop();
        }
        if (mPositionMark != null) {
            mPositionMark.setToTop();
            if (!isClickIdentification) {
                animMarker();
            }
        }
    }

    private void animMarker() {
        if (animator != null) {
            animator.start();
            return;
        }
        animator = ValueAnimator.ofFloat(mMapView.getHeight() / 2, mMapView.getHeight() / 2 - 30);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(150);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mPositionMark.setPositionByPixels(mMapView.getWidth() / 2, Math.round(value));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPositionMark.setIcon(moveBitmap);
            }
        });
        animator.start();
    }

    /**
     * 地图单击事件回调函数
     */
    @Override
    public void onMapClick(LatLng latLng) {
        clickMap();
    }

    private void clickRefresh() {
        clickInitInfo();
        if (initLocation != null) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    initLocation, 17f);
            aMap.animateCamera(cameraUpate);
        }
    }

    private void clickMap() {
        clickInitInfo();
        if (mRecordPositon != null) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    mRecordPositon, 17f);
            aMap.animateCamera(cameraUpate);
        }
    }

    private void clickInitInfo() {
        isClickIdentification = false;
        if (null != tempMark) {
            tempMark.setIcon(smallIdentificationBitmap);
            tempMark.hideInfoWindow();
            tempMark = null;
        }
        if (null != walkRouteOverlay) {
            walkRouteOverlay.removeFromMap();
        }
    }

    /**
     * 公交路径规划
     */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    /**
     * 驾车路径规划
     */
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    /**
     * 步行路径规划
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        LoadDialog.getInstance().dismiss();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    time = AMapUtil.getFriendlyTimeArray(dur);
                    distance = AMapUtil.getFriendlyLength(dis);
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    tempMark.setTitle(des);
                    tempMark.showInfoWindow();
                    Log.e("onWalkRouteSearched", des);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(this, R.string.no_result);
                }
            } else {
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    /**
     * 骑行路径规划
     */
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        LOG.e("getInfoWindow", "getInfoWindow");
        View infoWindow = getLayoutInflater().inflate(
                R.layout.info_window, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        TextView tv_time_info = (TextView) view.findViewById(R.id.tv_time_info);
        TextView tv_distance = (TextView) view.findViewById(R.id.tv_distance);
        tv_time.setText(time[0]);
        tv_time_info.setText(time[1]);
        tv_distance.setText(distance);
    }

    @Override
    public View getInfoContents(Marker marker) {
        LOG.e("getInfoContents");
        return null;
    }

    /**
     * 获取位置
     */
    @Override
    public void onLocationGet(PositionEntity entity) {
        // todo 这里在网络定位时可以减少一个逆地理编码
        LOG.e("onLocationGet" + entity.address);
        //        RouteTask.getInstance(getApplicationContext()).setStartPoint(entity);
        mStartPosition = new LatLng(entity.latitue, entity.longitude);
        if (mIsFirstShow) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    mStartPosition, 17);
            aMap.animateCamera(cameraUpate);
            mIsFirstShow = false;
        }
        mInitialMark.setPosition(mStartPosition);
        initLocation = mStartPosition;
        mLocationTask.stopLocate();
        LOG.e("onLocationGet" + mStartPosition);
    }

    /**
     * 地图移动后的位置
     */
    @Override
    public void onRegecodeGet(PositionEntity entity) {
        LOG.e("onRegecodeGet" + entity.address);
        entity.latitue = mStartPosition.latitude;
        entity.longitude = mStartPosition.longitude;
        RouteTask.getInstance(getApplicationContext()).setStartPoint(entity);
        RouteTask.getInstance(getApplicationContext()).search();
        LOG.e("onRegecodeGet" + mStartPosition);
    }

    /**
     * 定位失败
     */
    @Override
    public void onLocationError(RegeocodeResult regeocodeReult, int resultCode) {
        LatLng latLng = new LatLng(30.599108, 114.247264);//构造一个位置 此位置为武汉市的位置
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    /**
     * 创建初始位置图标
     */
    private void createInitialPosition(double lat, double lng) {
        MarkerOptions markerOptions = new MarkerOptions();
        //        markerOptions.setFlat(true);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(new LatLng(lat, lng));
        markerOptions.icon(initBitmap);
        mInitialMark = aMap.addMarker(markerOptions);
        mInitialMark.setClickable(false);
    }

    /**
     * 创建移动位置图标
     */
    private void createMovingPosition() {
        MarkerOptions markerOptions = new MarkerOptions();
        //        markerOptions.setFlat(true);
        //        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(new LatLng(0, 0));
        markerOptions.icon(moveBitmap);
        mPositionMark = aMap.addMarker(markerOptions);
        mPositionMark.setPositionByPixels(mMapView.getWidth() / 2,
                mMapView.getHeight() / 2);
        mPositionMark.setClickable(false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        Utils.removeMarkers();
        mMapView.onDestroy();
        mLocationTask.onDestroy();
        RouteTask.getInstance(getApplicationContext()).removeRouteCalculateListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        if (mInitialMark != null) {
            mInitialMark.setToTop();
        }
        if (mPositionMark != null) {
            mPositionMark.setToTop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRouteCalculate(float cost, float distance, int duration) {
        LOG.e("cost" + cost + "---" + "distance" + distance + "---" + "duration" + duration);
        PositionEntity endPoint = RouteTask.getInstance(getApplicationContext()).getEndPoint();
        mRecordPositon = new LatLng(endPoint.latitue, endPoint.longitude);
        clickMap();
        RouteTask.getInstance(getApplicationContext()).setEndPoint(null);
    }
}






