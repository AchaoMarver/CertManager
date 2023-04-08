package com.marver.cert.manager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import eu.chainfire.libsuperuser.Shell;

import com.marver.cert.manager.iface.CertificateStore;
import com.marver.cert.manager.iface.impl.IceCreamSandwichSystemCertficateStore;
import com.marver.cert.manager.task.ExamineFromFileSystem;
import com.marver.cert.manager.task.ExamineWebsiteTask;
import com.marver.cert.manager.task.ImportFromWebsite;
import com.marver.cert.manager.util.LogUtil;
import com.marver.cert.manager.util.PermissionUtils;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class MarverActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private PermissionUtils mPermissionUtils;
    private static final int PERMISSION_ASK_CODE = 0;

    private final CertificateStore _systemStore = new IceCreamSandwichSystemCertficateStore(this);
    private final Set<WeakBroadcastReceiver<?>> receiveres = new HashSet();
    private AtomicBoolean _hasRoot = new AtomicBoolean(false);
    public static final String ADAPTER_UPDATE_KEY = "com.marver.cert.manager.MarverActivity.ADAPTER_UPDATE_KEY";

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(5);
        setContentView(R.layout.activity_importer);

        mPermissionUtils = new PermissionUtils(this);
        mPermissionUtils.requestPermission(new PermissionUtils.RequestPermission() {
            @Override
            public void onRequestPermissionSuccess() {
                Toast.makeText(MarverActivity.this, "Success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRequestPermissionFailure(List<String> permissions) {
                Toast.makeText(MarverActivity.this, "Failure:" + permissions.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                Toast.makeText(MarverActivity.this, "AskNeverAgain:" + permissions.toString(), Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(MarverActivity.this)
                        .setMessage("请打开已禁止的权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", MarverActivity.this.getPackageName(), null);
                                intent.setData(uri);
                                try {
                                    MarverActivity.this.startActivityForResult(intent, PERMISSION_ASK_CODE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MarverActivity.this.finish();
                            }
                        }).show();

            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        LocalBroadcastManager localLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        FireAdapterUpdate fireAdapterUpdate = new FireAdapterUpdate(this, this._hasRoot);
        this.receiveres.add(fireAdapterUpdate);
        localLocalBroadcastManager.registerReceiver(fireAdapterUpdate, new IntentFilter(ADAPTER_UPDATE_KEY));
        new BaseAsyncTask<Void, Void, Boolean>(this) {
            public void onPostExecute(@SuppressLint("StaticFieldLeak") Boolean bool) {
                super.onPostExecute(bool);
                if (!bool) {
                    UI.showMessage(getActivity(), "未发现root环境");
                } else {
                   _hasRoot.set(bool);
                }
                localLocalBroadcastManager.sendBroadcast(new Intent(MarverActivity.ADAPTER_UPDATE_KEY));
                getActivity().invalidateOptionsMenu();
            }
            @Override
            public Boolean doInBackground(Void... voidArr) {
                return Shell.SU.available();
            }
        }.execute();
    }

    protected void onResume() {
        super.onResume();
        LogUtil.i("onResume");
    }

    protected void onPause() {
        super.onPause();
        LogUtil.i("onPause");
    }

    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i("onDestroy");
        Iterator localIterator = this.receiveres.iterator();
        for (;;) {
            if (!localIterator.hasNext()) {
                this.receiveres.clear();
                return;
            }
            WeakBroadcastReceiver localWeakBroadcastReceiver = (WeakBroadcastReceiver)localIterator.next();
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(localWeakBroadcastReceiver);
        }
    }

    protected void onActivityResult(int paramInt1, int paramInt2, Intent intent) {
        super.onActivityResult(paramInt1, paramInt2, intent);
        if (paramInt1 == 0 && paramInt2 == -1) {
            try {
                new ExamineFromFileSystem(this, getContentResolver().openInputStream(Objects.requireNonNull(intent.getData())), this._systemStore).execute();
            } catch (FileNotFoundException e) {
                UI.showMessage(this, e);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater localMenuInflater = getMenuInflater();
        localMenuInflater.inflate(R.menu.main_commands, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_install_from_sd).setVisible(this._hasRoot.get());
        menu.findItem(R.id.action_install_from_web).setVisible(this._hasRoot.get());
        menu.findItem(R.id.action_search).setVisible(this._hasRoot.get());
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id){
            case R.id.action_settings:
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ADAPTER_UPDATE_KEY));
                return true;
            case R.id.action_install_from_sd:
                this.showFileChooser();
                return true;
            case R.id.action_install_from_web:
                this.showWebsiteChooser(new ImportFromWebsite(this), "输入证书来源网站url");
                return true;
            case R.id.action_search:
                this.showWebsiteChooser(new ExamineWebsiteTask(this), "Enter website to examine");
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void showWebsiteChooser(final BaseAsyncTask<String, ?, ?> baseAsyncTask, final String paramStr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(paramStr);
        EditText editText = new EditText(this);
        editText.setInputType(1);
        builder.setView(editText);
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            String editable = editText.getText().toString();
            String str2 = editable;
            if (!editable.startsWith("https://")) {
                str2 = "https://" + editable;
            }
            baseAsyncTask.execute(str2);
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }

    private void showFileChooser() {
        Intent localIntent = new Intent("android.intent.action.GET_CONTENT");
        localIntent.setType("*/*");
        try {
            startActivityForResult(Intent.createChooser(localIntent, "Select a File to Upload"), 0); return;
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            Toast.makeText(this, "Please install a File Manager. We recommend ES File Explorer", Toast.LENGTH_SHORT).show();
        }
    }
}