package com.xgn.network;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import org.json.JSONObject;

/**
 * Created by fanzhengchen on 10/27/17.
 */

public class JsonUtil {


    public static JSONObject readableMapToJson(ReadableMap readableMap) throws Exception {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, null);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, readableMap.getMap(key).toHashMap());
                    break;
                case Array:
                    object.put(key, readableMap.getArray(key).toArrayList());
                    break;
                default:
            }

        }
        return object;
    }

    public static String convertToString(ReadableMap map) {
        String jsonString = null;
        try {
            JSONObject jsonObject = readableMapToJson(map);
            jsonString = jsonObject.toString();
        } catch (Exception e) {

        }
        return jsonString;
    }
}
