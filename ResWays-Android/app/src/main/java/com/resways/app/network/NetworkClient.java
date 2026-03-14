package com.resways.app.network;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkClient {
    private static NetworkClient instance;
    private RequestQueue requestQueue;
    private static Context ctx;
    
    // Change this to your Laptop's IP address when running on a physical phone. 
    // 10.0.2.2 is the localhost for the Android Emulator.
    public static final String BASE_URL = "http://192.168.100.27:8080/ResWays-Backend-1.0-SNAPSHOT/api";

    private NetworkClient(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized NetworkClient getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkClient(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
