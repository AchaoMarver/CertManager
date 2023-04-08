package com.marver.cert.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class WeakBroadcastReceiver<T extends Context> extends BroadcastReceiver {
    private final Reference<T> _context;

    public WeakBroadcastReceiver(T context) {
        this._context = new WeakReference(context);
    }

    public final T getContext() {
        return this._context.get();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}