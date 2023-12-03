package com.example.StressOverflow.Scan;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import com.example.StressOverflow.Util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;


/**
 * Handles finding associated product information from a product barcode UPC.
 */
public class BarcodeLookup {

    /**
     * Listener interface for when barcode lookup has finished
     */
    public interface OnBarcodeLookupResponseListener {
        void OnBarcodeLookupResponse(Map<String, String> map);
    }

    /**
     * UPC should be 12 digits, usually starts with 0
     * @param barcode string UPC
     * @return true or false
     */
    public static boolean isUPCValid(String barcode) {
        if (barcode != null && barcode.length() == 12) {
//            && barcode.charAt(0) == '0'
            return barcode.matches("\\d+");
        }
        return false;
    }

    /**
     * Access UPC database with API. Passes result from database to listener
     * in the form of a hashmap. Uses: https://rapidapi.com/UnlimitedAPI/api/barcodes-lookup/
     * @param barcode string UPC
     * @param listener for OnBarcodeLookupResponse
     * @param context context of current fragment for Toast
     */
    public static void get(String barcode, BarcodeLookup.OnBarcodeLookupResponseListener listener, android.content.Context context) {
        Util.showShortToast(context, "Searching for product information");

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
                    Map<String, String> empty = new HashMap<>();
                    empty.put("","");
                    listener.OnBarcodeLookupResponse(empty);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Map<String, String> empty = new HashMap<>();
                empty.put("","");
                listener.OnBarcodeLookupResponse(empty);
            }
        });
    }

    /**
     * Parses JSON into hashmap of category and its result
     * @param jsonResponse JSON from UPC database
     * @return hashmap of category and its result
     * @throws JSONException if can't convert response into JSONObject
     */
    public static Map<String, String> parseResponse(String jsonResponse) throws JSONException {
        Map<String, String> barcodeLookupResult = new HashMap<>();
        JSONObject jsonObject = new JSONObject(jsonResponse);

        try {
            JSONObject productObject = jsonObject.getJSONObject("product");
            barcodeLookupResult.put("Title", productObject.optString("title"));
            barcodeLookupResult.put("Make", productObject.optString("manufacturer"));
            barcodeLookupResult.put("Description", productObject.optString("description"));
//            barcodeLookupResult.put("Picture", productObject.getJSONArray("images").optString(0));
        } catch (JSONException e) {
            barcodeLookupResult.put("Title", "");
            barcodeLookupResult.put("Make", "");
            barcodeLookupResult.put("Description", "");
        }
        try {
            JSONObject productObject = jsonObject.getJSONObject("product");
            JSONObject attributesObject = productObject.getJSONObject("attributes");
            barcodeLookupResult.put("Model", attributesObject.optString("model"));
        } catch (JSONException e) {
            barcodeLookupResult.put("Model", "");
        }

        return barcodeLookupResult;
    }

}
