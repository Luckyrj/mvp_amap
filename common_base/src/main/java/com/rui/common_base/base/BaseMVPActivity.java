package com.rui.common_base.base;

import com.rui.common_base.R;
import com.rui.common_base.mvp.IPresenter;
import com.rui.common_base.mvp.IView;
import com.rui.common_base.widget.loading.LoadDialog;

import androidx.fragment.app.DialogFragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseMVPActivity<P extends IPresenter> extends BaseActivity implements IView {
    protected P        presenter;
    private   Unbinder unbinder;

    @Override
    protected void initData() {
        super.initData();
        presenter = createPresenter();
        // presenter 绑定 view
        if (presenter != null) {
            presenter.attachView(this);
        }
        unbinder = ButterKnife.bind(this);

    }


    protected abstract int getLayoutResId();

    protected abstract P createPresenter();

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Activity 销毁时取消所有的订阅
        if (presenter != null) {
            presenter.detachView();
            presenter = null;
        }
        if (unbinder != null) {
            unbinder.unbind();
        }

    }

    @Override
    public void showLoading() {
        LoadDialog loadDialog = LoadDialog.getInstance();
        loadDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.load_dialog);
        LoadDialog.getInstance().show(getSupportFragmentManager(), "");

    }

    @Override
    public void hideLoading() {
        LoadDialog.getInstance().dismiss();
    }
}
