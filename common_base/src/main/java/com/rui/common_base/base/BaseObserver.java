package com.rui.common_base.base;

import com.rui.common_base.http.ApiException;
import com.rui.common_base.http.ExceptionHandler;
import com.rui.common_base.mvp.IView;

import io.reactivex.observers.DisposableObserver;

public abstract class BaseObserver<T> extends DisposableObserver<BaseResponse<T>> {
    private IView baseView;

    public BaseObserver() {

    }


    public BaseObserver(IView baseView) {
        this.baseView = baseView;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (baseView != null) {
            baseView.showLoading();
        }
    }

    @Override
    public void onNext(BaseResponse<T> baseResponse) {
        if (baseView != null) {
            baseView.hideLoading();
        }
        int errcode = baseResponse.getErrorCode();
        String errmsg = baseResponse.getErrorMsg();

        if (errcode == 0 || errcode == 200) {   // wanandroid api
            T data = baseResponse.getData();
            // 将服务端获取到的正常数据传递给上层调用方
            if (null == data) {
                onSuccess((T)baseResponse);
            } else {
                onSuccess(data);
            }
        } else {
            onError(new ApiException(errcode, errmsg));
        }
    }

    /**
     * 回调正常数据
     * @param data
     */
    protected abstract void onSuccess(T data);

    /**
     * 异常处理，包括两方面数据：
     * (1) 服务端没有没有返回数据，HttpException，如网络异常，连接超时;
     * (2) 服务端返回了数据，但 errcode!=0,即服务端返回的data为空，如 密码错误,App登陆超时,token失效
     */
    @Override
    public void onError(Throwable e) {
        if (baseView != null) {
            baseView.hideLoading();
        }
        ExceptionHandler.handleException(e);
    }

    @Override
    public void onComplete() {
        if (baseView != null) {
            baseView.hideLoading();
        }
    }
}
