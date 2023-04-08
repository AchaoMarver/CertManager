package com.marver.cert.manager;

import com.marver.cert.manager.util.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.security.auth.x500.X500Principal;

public final class CertHash {
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static final String hash(X500Principal name) {
        int hash = X509_NAME_hash(name);
        String intToHexString = intToHexString(hash, 8);
        String IntegertoHexString = Integer.toHexString(hash);
        if (!intToHexString.equals(IntegertoHexString)) {
            LogUtil.e("Not Equal " + intToHexString + " " + IntegertoHexString);
        }
        return intToHexString;
    }

    private static final String intToHexString(int i, int minWidth) {
        char[] buf = new char[8];
        int cursor = 8;
        char[] digits = DIGITS;
        while (true) {
            cursor--;
            buf[cursor] = digits[i & 15];
            i >>>= 4;
            if (i == 0 && 8 - cursor >= minWidth) {
                return new String(buf, cursor, 8 - cursor);
            }
        }
    }

    private static final int X509_NAME_hash(X500Principal principal) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(principal.getEncoded());
            ByteBuffer wrap = ByteBuffer.wrap(digest);
            wrap.order(ByteOrder.LITTLE_ENDIAN);
            return wrap.getInt();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
