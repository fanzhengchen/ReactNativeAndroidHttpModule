package com.xgn.network;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

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
    private static final String HTTP_STATUS = "httpStatus";
    private static final String ERROR = "error";
    private static final String HEADER = "responseHeader";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";

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
        Log.d("fetch", url + " " + options.toString());
        OkHttpClient.Builder builder = sOkHttpClient.newBuilder();
        if (options.hasKey(TIME_OUT)) {
            int timeout = options.getInt(TIME_OUT);
            if (timeout != sOkHttpClient.connectTimeoutMillis()) {
                builder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
                sOkHttpClient = builder.build();
            }
        }


        ReadableMap mapHeaders = options.getMap(HEADERS);
        Headers headers = extractHeaders(mapHeaders);


        String method = "POST";
        if (options.hasKey(METHOD)) {

            method = options.getString(METHOD);
            Log.d("has method", method);

        }

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/json");
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                try {
                    ReadableMap map = options.getMap(BODY);
                    JSONObject jsonObject = MapUtil.toJSONObject(map);
                    String string = jsonObject.toString();
                    bufferedSink.write(string.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.headers(headers)
                .url(url)
                .method(method, requestBody)
                .build();

        Log.e("http", "wwwwwwww");

        sOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println(response.body().string());
                try {
                    promise.resolve(buildWritableMap(call, response));
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

    private WritableMap buildWritableMap(Call call, Response response) throws Exception {

        WritableMap bodyMap = null;
        WritableMap responseMap = Arguments.createMap();
        if (response != null && response.isSuccessful()) {

            JSONObject jsonObject = new JSONObject(response.body().string());
            WritableMap headerMap = createHeaderMap(response.headers());

            bodyMap = MapUtil.jsonToReact(jsonObject);
            responseMap.putBoolean(TIME_OUT, false);
            responseMap.putMap(HEADER, headerMap);
            responseMap.putInt(HTTP_STATUS, response.code());
            responseMap.putMap(BODY, bodyMap);
            responseMap.putMap(ERROR, null);
        } else if (response == null) {
            responseMap.putBoolean(TIME_OUT, true);
            responseMap.putMap(BODY, null);

            WritableMap errorMap = Arguments.createMap();
            errorMap.putString(CODE, "404");
            errorMap.putString(MESSAGE, "response is null");
        } else {
            JSONObject jsonObject = new JSONObject(response.body().string());
            WritableMap headerMap = createHeaderMap(response.headers());
            WritableMap errorMap = Arguments.createMap();
            bodyMap = MapUtil.jsonToReact(jsonObject);

            errorMap.putInt(CODE, response.code());
            errorMap.putString(MESSAGE, response.message());
            responseMap.putBoolean(TIME_OUT, false);
            responseMap.putMap(BODY, null);
            responseMap.putMap(ERROR, errorMap);
        }


        return responseMap;
    }

    private WritableMap createHeaderMap(Headers headers) {
        WritableMap writableMap = Arguments.createMap();
        int size = headers.size();
        for (int i = 0; i < size; ++i) {
            String name = headers.name(i);
            String value = headers.value(i);
            writableMap.putString(name, value);
        }
        return writableMap;
    }
}