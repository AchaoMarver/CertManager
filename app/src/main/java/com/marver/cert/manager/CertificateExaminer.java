package com.marver.cert.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x500.AttributeTypeAndValue;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CertificateExaminer {
    public static final void examine(Context context, X509Certificate cert, String closeTitle, String[] titles, DialogInterface.OnClickListener... clickListeners) {
        if (titles.length != clickListeners.length) {
            throw new IllegalArgumentException("Invalid parameter length");
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Examine Certificate");
        TextView textView = new TextView(context);
        StringBuilder builder = new StringBuilder();
        try {
            JcaX509CertificateHolder certHolder = new JcaX509CertificateHolder(cert);
            builder.append("Serial Number: ").append(certHolder.getSerialNumber()).append("\n");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            builder.append("Valid ").append(formatter.format(certHolder.getNotBefore())).append(" Through ").append(formatter.format(certHolder.getNotAfter())).append("\n");
            builder.append("SigAlgName: ").append(cert.getSigAlgName()).append("\n");
            builder.append("SigAlgOID: ").append(cert.getSigAlgOID()).append("\n");
            X500Name x500Subject = certHolder.getSubject();
            builder.append("---Subject Information: ---\n");
            appendInfo(builder, BCStyle.CN, "Common Name", x500Subject);
            appendInfo(builder, BCStyle.DN_QUALIFIER, "DN_QUALIFIER", x500Subject);
            appendInfo(builder, BCStyle.NAME, "NAME", x500Subject);
            appendInfo(builder, BCStyle.O, "Organization", x500Subject);
            appendInfo(builder, BCStyle.OU, "Orgainization Unit", x500Subject);
            appendInfo(builder, BCStyle.DC, "DC", x500Subject);
            appendInfo(builder, BCStyle.C, "Country", x500Subject);
            appendInfo(builder, BCStyle.SN, "SN", x500Subject);
            appendInfo(builder, BCStyle.UID, "UID", x500Subject);
            appendInfo(builder, BCStyle.E, "E", x500Subject);
            appendInfo(builder, BCStyle.ST, "ST", x500Subject);
            appendInfo(builder, BCStyle.T, "T", x500Subject);
            X500Name x500Issuer = certHolder.getSubject();
            builder.append("---Issuer Information: ---\n");
            appendInfo(builder, BCStyle.CN, "Common Name", x500Issuer);
            appendInfo(builder, BCStyle.DN_QUALIFIER, "DN_QUALIFIER", x500Issuer);
            appendInfo(builder, BCStyle.NAME, "NAME", x500Issuer);
            appendInfo(builder, BCStyle.O, "Organization", x500Issuer);
            appendInfo(builder, BCStyle.OU, "Orgainization Unit", x500Issuer);
            appendInfo(builder, BCStyle.DC, "DC", x500Issuer);
            appendInfo(builder, BCStyle.C, "Country", x500Issuer);
            appendInfo(builder, BCStyle.SN, "SN", x500Issuer);
            appendInfo(builder, BCStyle.UID, "UID", x500Issuer);
            appendInfo(builder, BCStyle.E, "Email", x500Issuer);
            appendInfo(builder, BCStyle.ST, "State", x500Issuer);
            appendInfo(builder, BCStyle.T, "T", x500Issuer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(textView);
        textView.setText(builder.toString());
        dialogBuilder.setView(scrollView);
        dialogBuilder.setNegativeButton(closeTitle, (dialogInterface, i) -> dialogInterface.dismiss());
        if (titles.length > 0) {
            dialogBuilder.setNeutralButton(titles[0], clickListeners[0]);
        }
        if (titles.length > 1) {
            dialogBuilder.setPositiveButton(titles[1], clickListeners[1]);
        }
        dialogBuilder.create().show();
    }

    private static final void appendInfo(StringBuilder builder, ASN1ObjectIdentifier style, String prefix, X500Name x500Name) {
        StringBuilder tempBuilder = new StringBuilder();
        RDN[] rdNs = x500Name.getRDNs(style);
        if (rdNs != null && rdNs.length > 0) {
            for (RDN rdn : rdNs) {
                AttributeTypeAndValue[] values = rdn.getTypesAndValues();
                for (AttributeTypeAndValue value : values) {
                    tempBuilder.append(IETFUtils.valueToString(value.getValue())).append(", ");
                }
            }
        }
        if (tempBuilder.toString().trim().length() > 0) {
            builder.append(prefix).append(": ");
            String t = tempBuilder.toString();
            int index = t.lastIndexOf(", ");
            builder.append(tempBuilder.substring(0, index)).append("\n");
        }
    }
}
