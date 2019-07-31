package cn.sesone.mvp_amap.ui;

import android.Manifest;
import android.content.Intent;

import com.rui.common_base.base.BaseActivity;
import com.rui.common_base.util.LOG;
import com.tbruyelle.rxpermissions2.RxPermissions;

import cn.sesone.mvp_amap.R;

public class SplashActivity extends BaseActivity {


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        //        ARouter.getInstance().build(ARouterConstace.ACTIVITY_USER_LOGIN).navigation();//简单跳转
        new RxPermissions(this)
                .requestEach(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        // 用户已经同意该权限
                        startActivity(new Intent(this, MainActivity.class));
                        LOG.d(permission.name + " is granted.");
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时。还会提示请求权限的对话框
                        LOG.d(permission.name + " is denied. More info should be provided.");
                    } else {
                        // 用户拒绝了该权限，而且选中『不再询问』
                        LOG.d(permission.name + " is denied.");

                    }
                });

    }
}
