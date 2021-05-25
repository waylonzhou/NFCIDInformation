package com.app.nfcidchange;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.regex.Pattern;

import android.view.View;

import net.vidageek.mirror.dsl.Mirror;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String NFC_ID_READ_WRITE = "/sys/devices/platform/config_data/nfc_id_value";
    private static final String TAG ="waylon_NFC_ID_WR";
   //Button btn_nfcid_read;
    private Button btn_nfcid_write;
    private TextView tv_nfc_read;
    private TextView tv_expect;
    private EditText et_nfc_write1;
    private EditText et_nfc_write2;
    private EditText et_nfc_write3;
    private EditText et_nfc_write4;
    private BluetoothAdapter mBluetooth;
    private static String BTMACAddr;
    private static int IntData[];
    private NfcAdapter mNfcAdapter;
    NFCReadWrite NFCID = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NFCID = NFCReadWrite.getInstance();
        boolean isEnable =false;
        String NFCIDData;
        IntData = new int[4];
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        isEnable = mBluetooth.isEnabled();
        if(!isEnable){
            mBluetooth.enable();
        }
        BTMACAddr =mBluetooth.getAddress();
        Log.d(TAG, "onCreate: " + BTMACAddr + " :" + isEnable);
        NFCIDData = NFCID.NfCIDReadFile();
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
            View decorView = getWindow().getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            systemUiVisibility |= flags;
            getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
        }
        setContentView(R.layout.activity_main);

        tv_nfc_read = findViewById(R.id.tv_nfc_read);
        tv_expect = findViewById(R.id.tv_expect);
        et_nfc_write1 = findViewById(R.id.et_nfc_write1);
        et_nfc_write2 = findViewById(R.id.et_nfc_write2);
        et_nfc_write3 = findViewById(R.id.et_nfc_write3);
        et_nfc_write4 = findViewById(R.id.et_nfc_write4);
        btn_nfcid_write = findViewById(R.id.btn_nfcid_write);
        btn_nfcid_write.setOnClickListener(this);
        findViewById(R.id.iv_cart).setOnClickListener(this);
        tv_nfc_read.setText(NFCIDData);
        handleDataID();
        if(!NFCID.force && (NFCIDData.compareTo("1:2:3:4") == 0)){
            NFCIDInformationShow();
        }
        if(NFCID.force)
            NFCIDWrite();
        if(NFCID.autoClose != 0) {
            Log.d(TAG, "onCreate: autoClose mode");
            CountDownTimer cdt = new CountDownTimer(1000 * NFCID.autoClose, 1000 ) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String value = String.valueOf((int)(millisUntilFinished/1000));
                    Log.d(TAG, "onTick: "+ value +" seconds");

                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "onCreate: autoClose onfinish");
                    //System.exit(0);
                    finish();
                   // cancel();
                }
            };
            cdt.start();
        }
        NFCID.force = false;
        NFCID.autoClose= 0;
        NFCID.silent = false;

    }
    private void NFCIDInformationShow()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("NFC ID Information");

        builder.setMessage("Do you want change the default NFC ID？");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                NFCIDWrite();
                Log.d(TAG, "setPositiveButton...");
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "setNegativeButton...");
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String GetVersion(Context context) {
        try {
            PackageInfo manager = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return manager.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    private void NFCIDVersionShow()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("APP Version Information");

        builder.setMessage("Name : NFC ID Information\nAuthor : Datalogic Waylon" +"\nVersion :"+GetVersion(this));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // NFCIDWrite();
                Log.d(TAG, "APP Version Information...");
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private void NFCIDWrite()
    {
        String WriteValue;
        Log.d(TAG, "onClick: btn_nfcid_write ");
        if (!isValidData()) {
            Toast.makeText(MainActivity.this, "Invalid, The NFC ID should be 0x00~0xFF", Toast.LENGTH_SHORT).show();
            return;
        }
        WriteValue = DealDataToString();
        NFCID.NfCIDWriteFile(WriteValue);

        tv_nfc_read.setText(NFCID.NfCIDReadFile().toString());
        if (mNfcAdapter != null) {
            NfcDisable();
            NfcEnable();
        }
        Log.d(TAG, "onClick: write end");
    }
    @Override
    public void onClick(View v)  {
        Log.d(TAG, "onClick: start writing");
        synchronized (this){
                if (v.getId() == R.id.btn_nfcid_write) {
                    NFCIDWrite();
                    Toast.makeText(MainActivity.this, "Write new NFC ID to chip success", Toast.LENGTH_SHORT).show();
                }
                if (v.getId() == R.id.iv_cart) {
                    NFCIDVersionShow();
                }
        }
    }
    private void NfcEnable(){
        try {
            Log.d(TAG, "NfcEnable ");
            Thread.sleep(700,0);
            Method method =mNfcAdapter.getClass().getDeclaredMethod("enable");
            method.invoke(mNfcAdapter);
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "NfcEnable: "+ e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.d(TAG, "NfcEnable: "+ e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.d(TAG, "NfcEnable: "+ e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.d(TAG, "NfcEnable: "+ e.getMessage());
            e.printStackTrace();
        }
    }
    private void NfcDisable(){
        try {
            Log.d(TAG, "NfcDisable");
            Method method = mNfcAdapter.getClass().getDeclaredMethod("disable");
            method.invoke(mNfcAdapter);
            //Thread.sleep(300,0);
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "NfcEnable: "+ e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.d(TAG, "NfcEnable: "+ e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.d(TAG, "NfcEnable: "+ e.getMessage());
            e.printStackTrace();
        }
    }
    void handleDataID(){
        if (BTMACAddr.length() < 16)
            return;
        BTMACAddr = BTMACAddr.substring(6);
        NFCID.ExceptData = BTMACAddr.split(":");
        Log.d(TAG, "handleDataID: " + NFCID.ExceptData[0] +" "+ NFCID.ExceptData[1] +" "+ NFCID.ExceptData[2] +" "+ NFCID.ExceptData[3]);
        tv_expect.setText("Expected  ID: "+ NFCID.ExceptData[0].concat(":")+NFCID.ExceptData[1].concat(":")+NFCID.ExceptData[2].concat(":")+NFCID.ExceptData[3]);
        et_nfc_write1.setText(NFCID.ExceptData[0]);
        et_nfc_write2.setText(NFCID.ExceptData[1]);
        et_nfc_write3.setText(NFCID.ExceptData[2]);
        et_nfc_write4.setText(NFCID.ExceptData[3]);
        return;
    }
     boolean isNumber(String str){
         Pattern pattern = Pattern.compile("[0-9a-fA-F]*");
        return pattern.matcher(str).matches();
    }
    boolean isValidData(){
        boolean isAvaiable = true;
        //String.isNumber(et_nfc_write1.getText().toString());

        isAvaiable &= isNumber(et_nfc_write1.getText().toString());
        isAvaiable &= isNumber(et_nfc_write2.getText().toString());
        isAvaiable &= isNumber(et_nfc_write3.getText().toString());
        isAvaiable &= isNumber(et_nfc_write4.getText().toString());
        if(!isAvaiable){
            Log.d(TAG, "isValidData: the String is invalid");
            return false;
        }
        IntData[0] = Integer.parseInt(et_nfc_write1.getText().toString(),16);
        if (IntData[0] < 0 || IntData[0] > 255)
            return false;
        IntData[1] = Integer.parseInt(et_nfc_write2.getText().toString(),16);
        if (IntData[1] < 0 || IntData[1] > 255)
            return false;
        IntData[2] = Integer.parseInt(et_nfc_write3.getText().toString(),16);
        if (IntData[2] < 0 || IntData[2] > 255)
            return false;
        IntData[3] = Integer.parseInt(et_nfc_write4.getText().toString(),16);
        if (IntData[3] < 0 || IntData[3] > 255)
            return false;
        return isAvaiable;
    }
    String DealDataToString(){

        String prop = String.format("%d,%d,%d,%d",IntData[0],IntData[1],IntData[2],IntData[3]);
        Log.d(TAG, "DealDataToString: " +prop);
        return prop;

    }

    private String getBtAddressViaReflection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
        if (bluetoothManagerService == null) {
            Log.w(TAG, "couldn't find bluetoothManagerService");
            return null;
        }
        Object address = new Mirror().on(bluetoothManagerService).invoke().method("getAddress").withoutArgs();
        if (address != null && address instanceof String) {
            Log.w(TAG, "using reflection to get the BT MAC address: " + address);
            return (String) address;
        } else {
            return null;
        }
    }

//    private String getBluetoothMacAddress() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        String bluetoothMacAddress = "";
//        try {
//            Field mServiceField = bluetoothAdapter.getClass().getDeclaredField("mService");
//            mServiceField.setAccessible(true);
//
//            Object btManagerService = mServiceField.get(bluetoothAdapter);
//
//            if (btManagerService != null) {
//                bluetoothMacAddress = (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);
//            }
//        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {
//
//        }
//        return bluetoothMacAddress;
//    }

}