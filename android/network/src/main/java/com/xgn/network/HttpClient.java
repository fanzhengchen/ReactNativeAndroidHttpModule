package com.xgn.network;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by fanzhengchen on 10/23/17.
 */

public class HttpClient extends ReactContextBaseJavaModule {
    private static final String TIME_OUT = "timeout";
    private static final String METHOD = "method";
    private static final String HEADERS = "headers";
    private static final String BODY = "body";

    private static OkHttpClient sOkHttpClient = new OkHttpClient.Builder()
            .build();


    public HttpClient(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "HttpClient";
    }

    @ReactMethod
    public void fetch(String url, ReadableMap options, Promise promise) {
        try {
            handleFetch(url, options, promise);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFetch(String url, final ReadableMap options, final Promise promise) throws Exception {
        /**
         * time out
         */
        OkHttpClient.Builder builder = sOkHttpClient.newBuilder();
        if (options.hasKey(TIME_OUT)) {
            int timeout = options.getInt(TIME_OUT);
            if (timeout != sOkHttpClient.connectTimeoutMillis()) {
                builder.connectTimeout(timeout, TimeUnit.SECONDS);
                sOkHttpClient = builder.build();
            }
        }


        ReadableMap mapHeaders = options.getMap(HEADERS);
        Headers headers = extractHeaders(mapHeaders);


        String method = "POST";
        if (options.hasKey(METHOD)) {
            method = options.getString(METHOD);
        }

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/json");
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                ReadableMap map = options.getMap(BODY);
                bufferedSink.write(map.toString().getBytes());
            }
        };

        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.headers(headers)
                .method(method, requestBody)
                .build();

        sOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.body().string());
            }
        });


    }

    private Headers extractHeaders(ReadableMap readableMap) {
        Headers.Builder builder = new Headers.Builder();
        if (readableMap != null) {
            ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                builder.add(key, readableMap.getString(key));
            }
        }
        return builder.build();
    }
}