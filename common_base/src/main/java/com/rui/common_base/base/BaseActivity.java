package com.rui.common_base.base;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.gyf.immersionbar.ImmersionBar;
import com.rui.common_base.R;
import com.rui.common_base.annotation.BindEventBus;
import com.rui.common_base.receiver.NetworkChangeReceiver;
import com.rui.common_base.util.EventBusHelper;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    protected Context               mContext;
    private   NetworkChangeReceiver receiver;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(getLayoutResId());
        setStatusBarColor();
        ButterKnife.bind(this);
        // 基类中注册 eventbus
        if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
            EventBusHelper.register(this);
        }
        initViewState(savedInstanceState);
        registerNetworkChangeReceiver();
        initView();
        initData();

    }


    protected abstract int getLayoutResId();

    protected abstract void initView();

    protected void initData() {}

    protected void initViewState(Bundle savedInstanceState) {}

    /**
     * 注册网络监听广播
     */
    private void registerNetworkChangeReceiver() {
        receiver = new NetworkChangeReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver.onDestroy();
            receiver = null;
        }
        // 取消注册
        if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
            EventBusHelper.unregister(this);
        }
    }

    public void setStatusBarColor() {
        ImmersionBar.with(this)
                .keyboardEnable(true)
                .transparentStatusBar()
                .navigationBarColor(R.color.white) //导航栏颜色，不写默认黑色
                .statusBarDarkFont(true, 0.2f) //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
                .navigationBarDarkIcon(true)
                .autoDarkModeEnable(true)
                .flymeOSStatusBarFontColor(com.rui.common_base.R.color.black)  //修改flyme OS状态栏字体颜色
                .init();  //必须调用方可沉浸
    }

}
