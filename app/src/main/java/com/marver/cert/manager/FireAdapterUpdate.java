package com.marver.cert.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.marver.cert.manager.util.LogUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class FireAdapterUpdate extends WeakBroadcastReceiver<Activity> {
    private final AtomicBoolean _hasRoot;

    public FireAdapterUpdate(Activity activity, AtomicBoolean hasRoot) {
        super(activity);
        this._hasRoot = hasRoot;
    }

    public void onReceive(Context arg0, Intent arg1) {
        if (getContext() != null) {
            new GetRootCerts((Activity) getContext(), this._hasRoot.get()).execute(new File[]{CertificateManagement.CACERTS_SYSTEM});
            return;
        }
        LogUtil.e("Null Context");
        Thread.dumpStack();
    }
}