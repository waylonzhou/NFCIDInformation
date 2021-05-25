package com.app.nfcidchange;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;

import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class NFCIdService extends Service {
    NFCReadWrite NFCID;
    private static final String TAG = "waylon_NormalService";

    private void refresh(String text) {
        Log.d(TAG, text);

    }
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public NFCIdService getService() {
            return NFCIdService.this;
        }
    }
    @Override
    public void onCreate() {
        refresh("onCreate");
        super.onCreate();

        NFCID = NFCReadWrite.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        refresh("onStartCommand. flags=" + flags);
        BluetoothAdapter mBluetooth;
        String BTMACAddr;

        if (intent != null) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                NFCID.force = bundle.getBoolean("force");
                NFCID.autoClose = bundle.getInt("autoClose");
                NFCID.silent = bundle.getBoolean("silent");
                Log.d(TAG, "getAppExtraData silent: " + NFCID.silent +" autoClose: " + NFCID.autoClose  + " force: " + NFCID.force);
                if(NFCID.silent){
                    NFCID.force = false;
                    NFCID.autoClose =0;
                    NFCID.silent =false;
                    mBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BTMACAddr =mBluetooth.getAddress().substring(6);
                    NFCID.ExceptData = BTMACAddr.split(":");
                    Log.d(TAG, "onStartCommand: write NFC ID before: " + BTMACAddr);
                    BTMACAddr =String.format("%d,%d,%d,%d",Integer.valueOf(NFCID.ExceptData[0],16),
                            Integer.valueOf(NFCID.ExceptData[1],16),
                            Integer.valueOf(NFCID.ExceptData[2],16),
                            Integer.valueOf(NFCID.ExceptData[3]),16);
                    Log.d(TAG, "onStartCommand: write NFC ID after: " + BTMACAddr);
                    NFCID.NfCIDWriteFile(BTMACAddr);
                }else if(NFCID.force || NFCID.autoClose!=0){
                    Intent intent1 =new Intent(this,MainActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }
            }
        }

        //getApplicationContext();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        refresh("onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        refresh("onBind");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        refresh("onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        refresh("onUnbind");
        return true;
    }

}


