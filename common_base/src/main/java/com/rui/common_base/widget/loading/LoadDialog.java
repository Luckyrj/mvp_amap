package com.rui.common_base.widget.loading;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.rui.common_base.R;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


/**
 * Created by Administrator on 2017/4/24.
 */

public class LoadDialog extends DialogFragment {

    public static LoadDialog getInstance()
    {
        return FirstQuote.instance;
    }

    //在第一次被引用时被加载
    static class FirstQuote {
        private static LoadDialog instance = new LoadDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        View view = inflater.inflate(R.layout.load, container);
        return view;
    }
}
