package com.astro.q8.handler;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.astro.q8.ui.OrderPlacedActivity;

public class JSInterface {
    Context context;
    Handler handler;
    public JSInterface(Context c) {
        this.context = c;
        handler = new Handler();
    }
    @JavascriptInterface
    public void orderPlaced() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                context.startActivity(new Intent(context, OrderPlacedActivity.class));
            }
        });
    }

}
