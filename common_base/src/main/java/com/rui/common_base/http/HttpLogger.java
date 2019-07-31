package com.rui.common_base.http;

import com.rui.common_base.util.LOG;

import okhttp3.logging.HttpLoggingInterceptor;

public class HttpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String message) {
            LOG.i( message);
        }
    }
