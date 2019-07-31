package com.rui.common_base.http;

import android.text.TextUtils;

import com.rui.common_base.constants.Constants;
import com.rui.common_base.util.SharedPreferenceUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class InterceptorUtil {
    public static Interceptor headerInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder();
                builder.addHeader("token", SharedPreferenceUtil.read(Constants.ACCESS_TOKEN, ""));
                builder.addHeader("Content-Type", "application/json;charset=UTF-8");
                String token = "";
                if (!TextUtils.isEmpty(token)) {
                    builder.addHeader("token", token);
                }
                Request request = builder.build();
                return chain.proceed(request);
            }
        };
    }

    public static Interceptor logInterceptor() {
        return new HttpLoggingInterceptor(new HttpLogger()).setLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
