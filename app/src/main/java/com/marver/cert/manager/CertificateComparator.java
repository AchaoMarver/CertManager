package com.marver.cert.manager;

import android.util.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x500.AttributeTypeAndValue;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.X509Certificate;
import java.util.Comparator;

public class CertificateComparator implements Comparator<Pair<X509Certificate, String>> {
    private final LoadingCache<X500Name, LoadingCache<ASN1ObjectIdentifier, String>> _cachedValues = CacheBuilder.newBuilder().build(new CacheLoader<X500Name, LoadingCache<ASN1ObjectIdentifier, String>>() {
        @Override
        public LoadingCache<ASN1ObjectIdentifier, String> load(X500Name x500Name) throws Exception {
            return CacheBuilder.newBuilder().build(new CacheLoader<ASN1ObjectIdentifier, String>() {
                public String load(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws Exception {
                    return CertificateComparator.getCertificateProperty(x500Name, aSN1ObjectIdentifier);
                }
            });

        }
    });

    @Override
    public int compare(Pair<X509Certificate, String> pair, Pair<X509Certificate, String> pair2) {
        int i;
        if (pair == pair2) {
            i = 0;
        } else {
            X509Certificate x509Certificate = (X509Certificate) pair.first;
            X509Certificate x509Certificate2 = (X509Certificate) pair2.first;
            int i2 = 0;
            try {
                X500Name subject = new JcaX509CertificateHolder(x509Certificate).getSubject();
                X500Name subject2 = new JcaX509CertificateHolder(x509Certificate2).getSubject();
                int compareToIgnoreCase = getPrimarySort(subject).compareToIgnoreCase(getPrimarySort(subject2));
                int i3 = compareToIgnoreCase;
                if (compareToIgnoreCase == 0) {
                    i3 = this._cachedValues.get(subject).get(BCStyle.OU).compareToIgnoreCase(this._cachedValues.get(subject2).get(BCStyle.OU));
                }
                i2 = i3;
                if (i3 == 0) {
                    i2 = this._cachedValues.get(subject).get(BCStyle.DC).compareToIgnoreCase(this._cachedValues.get(subject2).get(BCStyle.DC));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int i4 = i2;
            if (i2 == 0) {
                i4 = x509Certificate.getSerialNumber().compareTo(x509Certificate2.getSerialNumber());
            }
            int i5 = i4;
            if (i4 == 0) {
                i5 = Integer.compare(x509Certificate.getVersion(), x509Certificate2.getVersion());
            }
            int i6 = i5;
            if (i5 == 0) {
                i6 = x509Certificate.getNotAfter().compareTo(x509Certificate2.getNotAfter());
            }
            int i7 = i6;
            if (i6 == 0) {
                i7 = x509Certificate.getNotBefore().compareTo(x509Certificate2.getNotBefore());
            }
            i = i7;
            if (i7 == 0) {
                i = ((String) pair.second).compareTo((String) pair2.second);
            }
        }
        return i;
    }

    public final String getCachedCertificateProperty(X500Name name, ASN1ObjectIdentifier id) {
        return (String) ((LoadingCache) this._cachedValues.getUnchecked(name)).getUnchecked(id);
    }

    public static final String getCertificateProperty(X500Name name, ASN1ObjectIdentifier id) {
        StringBuilder builder = new StringBuilder();
        RDN[] rdNs = name.getRDNs(id);
        if (rdNs.length > 0) {
            for (int i = 0; i < rdNs.length; i++) {
                RDN cn = rdNs[i];
                IETFUtils.valueToString(cn.getFirst().getValue());
                AttributeTypeAndValue[] typesAndValues = cn.getTypesAndValues();
                if (typesAndValues != null) {
                    for (AttributeTypeAndValue value : typesAndValues) {
                        builder.append(IETFUtils.valueToString(value.getValue()));
                    }
                }
                if (i > rdNs.length - 1) {
                    builder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    private final String getFirstValue(X500Name name, ASN1ObjectIdentifier... ids) {
        String ret = "";
        if (ids == null) {
            return "";
        }
        try {
            LoadingCache<ASN1ObjectIdentifier, String> loadingCache = (LoadingCache) this._cachedValues.get(name);
            for (int count = 0; ret.trim().isEmpty() && count < ids.length; count++) {
                ret = (String) loadingCache.get(ids[count]);
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public final String getPrimarySort(X500Name name) {
        return getFirstValue(name, BCStyle.O, BCStyle.CN);
    }

    public final String getSecondarySort(X500Name name) {
        return getFirstValue(name, BCStyle.OU, BCStyle.CN);
    }
}