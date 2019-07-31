package com.rui.common_base.http;


import android.util.Log;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.rui.common_base.BuildConfig;
import com.rui.common_base.base.BaseApplication;
import com.rui.common_base.util.NetworkUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String         API_HOST = BuildConfig.BASE_SERVER_URL;
    private static       RetrofitClient instance;
    private static       OkHttpClient   okHttpClient;
    private static       Retrofit       retrofit;

    private RetrofitClient() {

        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(BaseApplication.getApplication()));

        okHttpClient = new OkHttpClient.Builder()

                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .cache(createMCache())
                .retryOnConnectionFailure(true)//错误重连
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .addInterceptor(InterceptorUtil.logInterceptor())
                .addInterceptor(InterceptorUtil.headerInterceptor())
                .addInterceptor(cacheInterceptor())
                .addNetworkInterceptor(cacheInterceptor())
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(API_HOST)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())   // 使用 gson 解析器解析 json
                .client(okHttpClient)
                .build();

    }


    public static RetrofitClient getInstance() {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    instance = new RetrofitClient();
                }
            }
        }
        return instance;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    private static Cache createMCache() {
        File cacheFile = new File(String.valueOf(BaseApplication.getApplication().getCacheDir()));
        return new Cache(cacheFile, 1024 * 1024 * 60);
    }

    private Interceptor cacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                if (!NetworkUtil.isNetworkAvailable(BaseApplication.getApplication())) {
                    request = request.newBuilder()
                            //强制使用缓存
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }

                Response response = chain.proceed(request);

                if (NetworkUtil.isNetworkAvailable(BaseApplication.getApplication())) {
                    //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                    String cacheControl = request.cacheControl().toString();
                    Log.i("has network", "cacheControl=" + cacheControl);
                    return response.newBuilder()
                            .header("Cache-Control", cacheControl)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                    Log.i("network error ", "maxStale=" + maxStale);
                    return response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }

            }
        };
    }
}