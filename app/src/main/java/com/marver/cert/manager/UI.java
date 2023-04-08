package com.marver.cert.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;

public class UI {
    public static final void showMessage(Context context, Throwable e) {
        if (e != null) {
            Log.e("UI", "unknown error", e);
            e.printStackTrace();
        }
        showMessage(context, e.getLocalizedMessage());
    }

    public static final void showMessage(Context _context, String message) {
        if (_context != null) {
            AlertDialog ad = new AlertDialog.Builder(_context).create();
            ad.setCancelable(false);
            ad.setMessage(message);
            ad.setButton(-1, (CharSequence) "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            try {
                ad.show();
            } catch (WindowManager.BadTokenException e) {
            }
        }
    }
}
