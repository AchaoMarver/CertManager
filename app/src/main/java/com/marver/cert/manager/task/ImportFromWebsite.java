package com.marver.cert.manager.task;

import android.app.Activity;
import android.content.DialogInterface;

import com.marver.cert.manager.BaseAsyncTask;
import com.marver.cert.manager.CertificateComparator;
import com.marver.cert.manager.CertificateExaminer;
import com.marver.cert.manager.CopyCertificate;
import com.marver.cert.manager.UI;
import com.marver.cert.manager.iface.CertificateStore;
import com.marver.cert.manager.iface.impl.IceCreamSandwichSystemCertficateStore;
import com.marver.cert.manager.util.LogUtil;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ImportFromWebsite extends BaseAsyncTask<String, Void, X509Certificate> {
    public ImportFromWebsite(Activity activity) {
        super(activity);
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    public X509Certificate doInBackground(String... arg0) {
        try {
            LogUtil.i("Trying " + arg0[0]);
            URL url = new URL(arg0[0]);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager[] tm = {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};

            context.init(null, tm, new SecureRandom());
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.connect();
            Certificate[] serverCertificates = urlConnection.getServerCertificates();
            for (Certificate cert : serverCertificates) {
                X500Name x500name = new JcaX509CertificateHolder((X509Certificate) cert).getIssuer();
                LogUtil.i("CN=" + CertificateComparator.getCertificateProperty(x500name, BCStyle.CN));
                LogUtil.i("OU=" + CertificateComparator.getCertificateProperty(x500name, BCStyle.OU));
                LogUtil.i("O=" + CertificateComparator.getCertificateProperty(x500name, BCStyle.O));
            }
            return (X509Certificate) serverCertificates[serverCertificates.length - 1];
        } catch (Exception e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UI.showMessage(ImportFromWebsite.this.getActivity(), e);
                }
            });
            return null;
        }
    }

    public void onPostExecute(X509Certificate result) {
        super.onPostExecute(result);
        if (result != null) {
            CertificateExaminer.examine(getActivity(), result, "Cancel", new String[]{"Import"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new CopyCertificate(getActivity(), result).execute(new CertificateStore[]{new IceCreamSandwichSystemCertficateStore(ImportFromWebsite.this.getActivity())});
                }
            });
        }
    }
}