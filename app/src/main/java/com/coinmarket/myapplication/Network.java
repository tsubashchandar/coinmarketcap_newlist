package com.coinmarket.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Network {

    String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=3&convert=USD&sort=date_added&sort_dir=asc";
    RequestQueue queue;
    static ArrayList<DataModel> dataSet = null;
    NetworkResponse networkResponse;
    private Context ctx;
    static String INTENT_ACTION_KEY = "com.networkResponse";
    static String LAST_COIN_KEY = "lastCoin";
    static String LAST_COIN_PLATFORM_KEY = "lastCoinPlatform";
    static String API_KEY_SHARED_PREFERENCE = "apiKeySP";
    static String Key = "";
    static HashSet<String> keySet;
    public interface NetworkResponse {
        public void onResponse();
    }
    {
        keySet = new HashSet<String>();
        keySet.add("b7a9c0f9-1476-472c-8a3e-a575b7204820");
        keySet.add("ad4e531e-9102-42ea-b757-a67e27c26a2a");
        keySet.add("fd4badee-4867-4729-8bdf-b414a2830c18");
        keySet.add("6903341b-8cd4-4f42-9e4d-d6f07180269b");
        keySet.add("a9c205fa-2af8-456d-b77b-e62047f75da5");

    }
    static {
        if (dataSet == null) {
            dataSet = new ArrayList<DataModel>();
        }
    }

    public Network(NetworkResponse networkResponse) {
        this.networkResponse = networkResponse;
    }

    public Network() {
        this.networkResponse = null;
    }

    public ArrayList<DataModel> getDataSet() {
        return dataSet;
    }

    public void PostRequest(Context context) {
        queue = Volley.newRequestQueue(context);
        ctx = context;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    parseData(response);
                    sendResponse();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("response", response.toString());
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dataSet.clear();
                DataModel temp = new DataModel("error", "error", "error", "error", "error");
                dataSet.add(temp);
                sendResponse();
            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("start", "1");
                params.put("limit", "1");
                params.put("convert", "USD");
                params.put("sort", "date_added");
                params.put("sort_dir", "asc");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-CMC_PRO_API_KEY", Network.Key);
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        queue.add(request);

    }

    public void parseData(JSONObject data) throws JSONException {
        if (!dataSet.isEmpty())
            dataSet.clear();
        JSONArray rootcrypto = data.getJSONArray("data");
        for (int i = 0; i < rootcrypto.length(); i++) {
            JSONObject arr = rootcrypto.getJSONObject(i);
            JSONObject plat = arr.getJSONObject("platform");
            JSONObject quote = arr.getJSONObject("quote");
            JSONObject usd = quote.getJSONObject("USD");
            String name = arr.getString("name");
            String symbol = arr.getString("symbol");
            String platform = plat.getString("symbol");
            String address = plat.getString("token_address");
            String price = usd.getString("price");
            DataModel temp = new DataModel(name, symbol, platform, address, price);
            dataSet.add(temp);
        }

    }

    private void sendResponse() {
        if (networkResponse != null)
            networkResponse.onResponse();
        else
            sendBroadcastToService();
    }

    private void sendBroadcastToService() {
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_KEY);
        intent.putExtra(LAST_COIN_KEY, dataSet.get(0).name);
        intent.putExtra(LAST_COIN_PLATFORM_KEY, dataSet.get(0).platform);
        ctx.sendBroadcast(intent);
    }
}
