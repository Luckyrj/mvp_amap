package com.rui.common_base.base;

/**
 * 网络请求返回的数据，按格式统一包装成 BaseResponse 类
 * Created by Administrator on 2018/9/15.
 */

public class BaseResponse<T> {

    private int code = -1;
    private String msg;
    private T data;

    private T results;
    private boolean error = true;

    public int getErrorCode() {
        return code;
    }

    public void setErrorCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return msg;
    }

    public void setErrorMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isError() {
        return error;
    }

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "errorCode=" + code +
                ", errorMsg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
