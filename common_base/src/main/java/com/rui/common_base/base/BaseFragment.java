package com.rui.common_base.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rui.common_base.annotation.BindEventBus;
import com.rui.common_base.util.EventBusHelper;

import org.greenrobot.eventbus.EventBus;

import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {
    protected Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
            EventBusHelper.register(this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResId(), container, false);
        initView(rootView);
        initData();
        return rootView;
    }

    protected abstract int getLayoutResId();

    protected abstract void initView(View rootView);

    protected abstract void initData();

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
