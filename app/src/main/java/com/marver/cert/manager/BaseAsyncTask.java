package com.marver.cert.manager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public abstract class BaseAsyncTask <Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {
    private final Reference<Context> _activity;

    public BaseAsyncTask(Context context) {
        this._activity = new WeakReference<Context>(context);
    }


    public final Activity getActivity() {
        return (Activity)this._activity.get();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        Context context = this._activity.get();
        if ((context instanceof Activity)) {
            ((Activity) context).setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = this._activity.get();
        boolean bl = context != null;
        if (bl & context instanceof Activity) {
            ((Activity)context).setProgressBarIndeterminateVisibility(true);
        }
    }
}
