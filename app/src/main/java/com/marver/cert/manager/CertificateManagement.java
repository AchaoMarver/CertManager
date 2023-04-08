package com.marver.cert.manager;

import android.app.Activity;

import com.marver.cert.manager.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class CertificateManagement {
    public static final File CACERTS_SYSTEM = new File("/system/etc/security/cacerts");

    private CertificateManagement() {
    }

    public static final void deleteCertificateFromSystem(String id) throws IOException {
        File file = new File(CACERTS_SYSTEM, id);
        if (!file.exists()) {
            throw new FileNotFoundException(String.valueOf(file.getCanonicalPath()) + " could not be found");
        }
        StringBuilder builder = new StringBuilder();

        try {
            Shell.Pool.SU.run("mount -o rw,remount /system");
            LogUtil.i("Running \"rm " + file.getCanonicalPath() + "\"");
            List<String> run = Shell.run("su", new String[]{"rm " + file.getCanonicalPath()}, (String[]) null, true);
            LogUtil.i("Results: " + run.size());
            if (run.size() > 0) {
                for (String s : run) {
                    LogUtil.i(s);
                    builder.append(s).append(": ");
                }
                throw new IOException(builder.toString());
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            try {
                Shell.Pool.SU.run("mount -o ro,remount /system");
            } catch (Shell.ShellDiedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final void moveCertificateToSystem(Activity activity, byte[] source, String dest) throws IOException {
        FileOutputStream outputStream = null;
        LogUtil.i("copying " + source.length + " bytes to " + dest);
        File outputFile = new File(CACERTS_SYSTEM, dest);
        if (outputFile.exists()) {
            throw new IOException(dest + " already exists in " + CACERTS_SYSTEM.getCanonicalPath());
        }
        File tempFile = new File(activity.getCacheDir(), dest);
        try {
            outputStream = new FileOutputStream(tempFile);
            outputStream.write(source);
            outputStream.close();
            Shell.Pool.SU.run("mount -o rw,remount /system");
            try {
                Shell.Pool.SU.run("cp " + tempFile.getCanonicalPath() + " " + CACERTS_SYSTEM.getCanonicalPath() + "/" + tempFile.getName());
                Shell.Pool.SU.run("chmod 644 " + outputFile.getCanonicalPath());
                Shell.Pool.SU.run("mount -o ro,remount /system");
                tempFile.delete();
            } catch (Exception e) {
                Shell.Pool.SU.run("mount -o ro,remount /system");
                throw e;
            }
        } catch (Exception e) {
            if (outputStream != null) {
                outputStream.close();
            }
            throw new RuntimeException(e);
        }
    }
}