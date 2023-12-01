package com.example.StressOverflow.Scan;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class BarcodeLookup {

    /**
     * Listener interface for when barcode lookup has finished
     */
    public interface OnBarcodeLookupResponseListener {
        void OnBarcodeLookupResponse(Map<String, String> map);
    }

    public static Map<String, String> parseResponse(String jsonResponse) throws JSONException {
        Map<String, String> barcodeLookupResult = new HashMap<>();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONObject productObject = jsonObject.getJSONObject("product");
        JSONObject attributesObject = productObject.getJSONObject("attributes");

        barcodeLookupResult.put("brand", productObject.optString("brand"));
        barcodeLookupResult.put("description", productObject.optString("description"));
        barcodeLookupResult.put("title", productObject.optString("title"));
        barcodeLookupResult.put("model", attributesObject.optString("model"));
        barcodeLookupResult.put("pic_URL", productObject.getJSONArray("images").optString(0));

        return barcodeLookupResult;
    }

    public static void get(String barcode, BarcodeLookup.OnBarcodeLookupResponseListener listener) throws IOException {
        String apiKey = "9ff9cbbf9cmshb225ceea962f5ccp14963bjsnd27e61c28621";
        String url = "https://barcodes-lookup.p.rapidapi.com/?query=" + barcode;

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("X-RapidAPI-Key", apiKey);
        client.addHeader("X-RapidAPI-Host", "barcodes-lookup.p.rapidapi.com");
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                try {
                    listener.OnBarcodeLookupResponse(parseResponse(response));
                } catch (JSONException e) {
                    // TODO don't crash, log error message
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // TODO: Handle failure
                Log.e("AsyncHttpClient", "Failure: " + statusCode, error);
            }
        });
    }

}
