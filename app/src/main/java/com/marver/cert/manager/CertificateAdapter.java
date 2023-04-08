package com.marver.cert.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertificateAdapter extends BaseAdapter {
    private final Reference<Activity> _activity;
    private final List<Pair<X509Certificate, String>> _certs;
    private final CertificateComparator _comparator;

    public CertificateAdapter(Activity activity, List<Pair<X509Certificate, String>> certs, CertificateComparator comparator) {
        this._certs = certs;
        this._activity = new WeakReference(activity);
        this._comparator = comparator;
    }

    @Override
    public int getCount() {
        return this._certs.size();
    }

    @Override
    public Object getItem(int position) {
        return this._certs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((X509Certificate) this._certs.get(position).first).getIssuerX500Principal().hashCode();
    }

    @SuppressLint("ResourceType")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            View v5 = convertView == null ? ((LayoutInflater)((Activity)this._activity.get()).getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(0x1090004, parent, false) : convertView;
            TextView largeTextView = (TextView)v5.findViewById(0x1020014);
            TextView smallTextView = (TextView)v5.findViewById(0x1020015);
            X500Name x500name0 = new JcaX509CertificateHolder(((X509Certificate)((Pair)this._certs.get(position)).first)).getSubject();
            largeTextView.setText(this._comparator.getPrimarySort(x500name0));
            smallTextView.setText(this._comparator.getSecondarySort(x500name0));
            return v5;
        } catch(Throwable e) {
            UI.showMessage(((Context)this._activity.get()), e);
            return null;
        }
    }
}