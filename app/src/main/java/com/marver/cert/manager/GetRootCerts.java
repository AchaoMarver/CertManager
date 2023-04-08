package com.marver.cert.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.marver.cert.manager.iface.impl.IceCreamSandwichSystemCertficateStore;
import com.marver.cert.manager.util.IOUtils;
import com.marver.cert.manager.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class GetRootCerts extends BaseAsyncTask<File, Double, SortedSet<Pair<X509Certificate, String>>> {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final boolean _hasRoot;
    private final ProgressBar _progress;

    public GetRootCerts(Activity activity, boolean hasRoot) {
        super(activity);
        this._progress = (ProgressBar) activity.findViewById(R.id.progressBar1);
        this._hasRoot = hasRoot;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this._progress.setVisibility(View.VISIBLE);
    }

    public void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
        this._progress.setProgress((int) (values[0].doubleValue() * 100.0d));
    }

    public SortedSet<Pair<X509Certificate, String>> doInBackground(File... arg0) {
        FileInputStream inStream = null;
        try {
            File[] files = arg0[0].listFiles();
            SortedSet<Pair<X509Certificate, String>> certs = new TreeSet<>((Comparator<? super Pair<X509Certificate, String>>) new CertificateComparator());
            Sorter sorter = new Sorter(certs);
            Thread thread = new Thread((Runnable) sorter);
            thread.setDaemon(true);
            thread.start();
            if (files != null) {
                LogUtil.i("Found " + files.length + " files");
                for (File file : files) {
                    try {
                        inStream = new FileInputStream(file);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        IOUtils.copy(inStream, outputStream);
                        sorter.add(outputStream.toByteArray(), file.getName());
                        inStream.close();
                        publishProgress(new Double[]{Double.valueOf(certs.size() / files.length)});
                    } catch (Throwable th) {
                        if (inStream != null) {
                            inStream.close();
                        }
                        throw th;
                    }
                }
                sorter.terminate();
                while (thread.isAlive()) {
                    Thread.sleep(25L);
                    publishProgress(new Double[]{Double.valueOf(certs.size() / files.length)});
                }
                thread.join();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Loaded " + certs.size() + " certificates", Toast.LENGTH_SHORT).show();
                }
            });
            return certs;
        } catch (Throwable e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    UI.showMessage(getActivity(), e);
                }
            });
            return null;
        }
    }

    public void onPostExecute(SortedSet<Pair<X509Certificate, String>> result) {
        super.onPostExecute(result);
        this._progress.setVisibility(View.GONE);
        if (result != null) {
            try {
                if (result.size() >= 1) {
                    ListView listView = (ListView) getActivity().findViewById(R.id.rootCertList);
                    ArrayList<Pair<X509Certificate, String>> certList = new ArrayList<>(result);
                    listView.setAdapter((ListAdapter) new CertificateAdapter(getActivity(), certList, (CertificateComparator) result.comparator()));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            X509Certificate cert = (X509Certificate) ((Pair) certList.get(i)).first;
                            String certKey = (String) ((Pair) certList.get(i)).second;
                            String[] titles = _hasRoot ? new String[]{"Delete", "Export to SD Card"} : new String[]{"Export to SD Card"};
                            DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
                                    deleteBuilder.setTitle("Delete this certificate (" + certKey + ")?").setPositiveButton((CharSequence) "Delete", new DialogInterface.OnClickListener() {
                                        @SuppressLint("StaticFieldLeak")
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            new BaseAsyncTask<Void, Void, Exception>(getActivity()) {
                                                @Override
                                                protected Exception doInBackground(Void... voids) {
                                                    try {
                                                        new IceCreamSandwichSystemCertficateStore(getActivity()).removeCertificate(certKey);
                                                        return null;
                                                    } catch (IOException e) {
                                                        return e;
                                                    }
                                                }

                                                public void onPostExecute(Exception result) {
                                                    super.onPostExecute(result);
                                                    if (result == null) {
                                                        UI.showMessage(getActivity(), "Success!");
                                                    } else {
                                                        UI.showMessage(getActivity(), result);
                                                    }
                                                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(MarverActivity.ADAPTER_UPDATE_KEY));
                                                }
                                            };
                                        }
                                    });
                                    deleteBuilder.setNegativeButton((CharSequence) "Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                                    deleteBuilder.create().show();
                                }
                            };
                            DialogInterface.OnClickListener exportListener = (dialogInterface, i1) -> dialogInterface.dismiss();
                            DialogInterface.OnClickListener[] listeners = _hasRoot ? new DialogInterface.OnClickListener[]{deleteListener, exportListener} : new DialogInterface.OnClickListener[]{exportListener};
                            CertificateExaminer.examine(getActivity(), cert, "Close", titles, listeners);
                        }
                    });
                }
            } catch (Throwable e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UI.showMessage(getActivity(), e);
                    }
                });
                return;
            }
        }
        if(result == null) {
            UI.showMessage(getActivity(), "未发现系统证书");
        }
    }

    class Sorter implements Runnable {
        private final SortedSet<Pair<X509Certificate, String>> _map;
        private final BlockingQueue<Pair<byte[], String>> _input = new LinkedBlockingQueue();
        private boolean _running = true;

        public Sorter(SortedSet<Pair<X509Certificate, String>> map) {
            this._map = map;
        }

        public final void add(byte[] data, String name) {
            this._input.add(new Pair<>(data, name));
        }

        public final void terminate() {
            this._running = false;
        }

        @Override
        public void run() {
            Pair<byte[], String> pair;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                while (true) {
                    try {
                        pair = this._input.poll(100L, TimeUnit.MILLISECONDS);
                        if (pair != null || this._running) {
                            if (pair != null) {
                                ByteArrayInputStream inStream = new ByteArrayInputStream((byte[]) pair.first);
                                X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
                                this._map.add(new Pair<>(cert, (String) pair.second));
                            }
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}