package com.marver.cert.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.marver.cert.manager.iface.CertificateStore;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CopyCertificate extends BaseAsyncTask<CertificateStore, Void, Exception> {
    private final X509Certificate _cert;

    public CopyCertificate(Activity activity, X509Certificate cert) {
        super(activity);
        this._cert = cert;
    }

    public Exception doInBackground(CertificateStore... arg0) {
        try {
            CertificateStore store = arg0[0];
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(this._cert.getEncoded()));
            store.putCertificate(cert);
            new Handler(Looper.getMainLooper()).post(() -> {
                UI.showMessage(getActivity(), "Sucessfully copied");
                LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(new Intent(MarverActivity.ADAPTER_UPDATE_KEY));
            });
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    public void onPostExecute(Exception result) {
        super.onPostExecute(result);
        if (result != null) {
            UI.showMessage(getActivity(), result);
        }
    }
}