package com.example.StressOverflow.Scan;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class BarcodeLookup {

    public static void parseResponse(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONObject productObject = jsonObject.getJSONObject("product");
        JSONObject attributesObject = productObject.getJSONObject("attributes");

        String brand = productObject.optString("brand");
        String description = productObject.optString("description");
        String title = productObject.optString("title");
        String model = attributesObject.optString("model");
        String pic_URL = productObject.getJSONArray("images").optString(0);
    }

    public static void showLookupResults(String response) {
        try {
            parseResponse(response);
        } catch (JSONException e) {
            // TODO don't crash, log error message
            throw new RuntimeException(e);
        }

        // display

        // ask to overwrite

            // fill in by passing to fragment
    }

    public static void get(String barcode) throws IOException {
        String apiKey = "9ff9cbbf9cmshb225ceea962f5ccp14963bjsnd27e61c28621";
        String url = "https://barcodes-lookup.p.rapidapi.com/?query=" + barcode;

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("X-RapidAPI-Key", apiKey);
        client.addHeader("X-RapidAPI-Host", "barcodes-lookup.p.rapidapi.com");
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                showLookupResults(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // TODO: Handle failure
                Log.e("AsyncHttpClient", "Failure: " + statusCode, error);
            }
        });
    }

}
