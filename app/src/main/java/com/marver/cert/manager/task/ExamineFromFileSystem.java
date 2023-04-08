package com.marver.cert.manager.task;

import android.content.Context;
import android.content.DialogInterface;

import com.marver.cert.manager.BaseAsyncTask;
import com.marver.cert.manager.CertificateExaminer;
import com.marver.cert.manager.CopyCertificate;
import com.marver.cert.manager.iface.CertificateStore;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ExamineFromFileSystem extends BaseAsyncTask<String, Void, X509Certificate> {
    private final InputStream _input;
    private final CertificateStore _systemStore;

    public ExamineFromFileSystem(Context activity, InputStream input, CertificateStore systemStore) {
        super(activity);
        this._input = input;
        this._systemStore = systemStore;
    }

    @Override
    public X509Certificate doInBackground(String... params) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(this._input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onPostExecute(X509Certificate x509Certificate) {
        super.onPostExecute(x509Certificate);
        if (x509Certificate != null) {
            CertificateExaminer.examine(getActivity(), x509Certificate, "Cancel", new String[]{"Import"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new CopyCertificate(ExamineFromFileSystem.this.getActivity(), x509Certificate).execute(_systemStore);
                }
            });
        }
    }
}