package com.marver.cert.manager.iface.impl;

import android.app.Activity;

import com.marver.cert.manager.CertHash;
import com.marver.cert.manager.CertificateManagement;
import com.marver.cert.manager.iface.CertificateStore;
import com.marver.cert.manager.util.IOUtils;
import com.marver.cert.manager.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

public class IceCreamSandwichSystemCertficateStore implements CertificateStore {
    private final Reference<Activity> _activity;

    public IceCreamSandwichSystemCertficateStore(Activity activity) {
        this._activity = new WeakReference<Activity>(activity);
    }

    @Override
    public boolean checkId(String object) {
        return new File(CertificateManagement.CACERTS_SYSTEM, object).exists();
    }

    @Override
    public X509Certificate getCertificate(String str) throws IOException, CertificateException {
        File file = new File(CertificateManagement.CACERTS_SYSTEM, str);
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, byteArrayOutputStream);
            fileInputStream.close();
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        } catch (Throwable th) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Set<String> getStoredIds() {
        File[] fileArray = CertificateManagement.CACERTS_SYSTEM.listFiles();
        HashSet<String> hashSet = new HashSet<String>();
        if (fileArray == null) return hashSet;
        int n = fileArray.length;
        int n2 = 0;
        while (n2 < n) {
            File file = fileArray[n2];
            hashSet.add(file.getName());
            ++n2;
        }
        return hashSet;
    }

    public boolean putCertificate(X509Certificate x509Certificate) throws CertificateEncodingException, IOException {
        String hash = CertHash.hash(x509Certificate.getIssuerX500Principal());
        String hash2 = CertHash.hash(x509Certificate.getSubjectX500Principal());
        if (!hash.equals(hash2)) {
            LogUtil.e("Hashes are not equal: " + hash + " " + hash2);
        }
        File file = null;
        int i = 0;
        while (file == null && i < 10) {
            File file2 = new File(CertificateManagement.CACERTS_SYSTEM, String.valueOf(hash) + "." + i);
            file = file2;
            if (file2.exists()) {
                file = null;
                i++;
            }
        }
        if (file == null) {
            throw new IOException("Too many File Collisions for " + hash);
        }
        CertificateManagement.moveCertificateToSystem(this._activity.get(), x509Certificate.getEncoded(), hash + "." + i);
        return true;

    }

    public boolean removeCertificate(String string) throws IOException {
        CertificateManagement.deleteCertificateFromSystem((String) string);
        return true;
    }
}