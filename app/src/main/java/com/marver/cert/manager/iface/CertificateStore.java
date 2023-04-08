package com.marver.cert.manager.iface;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

public interface CertificateStore {
    boolean checkId(String paramString);

    X509Certificate getCertificate(String paramString) throws IOException, CertificateException;

    Set<String> getStoredIds();

    boolean putCertificate(X509Certificate paramX509Certificate) throws CertificateEncodingException, IOException;

    abstract boolean removeCertificate(String paramString) throws IOException;
}
