package com.app.nfcidchange;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NFCReadWrite {
    protected static Boolean force;
    protected static int autoClose;
    protected static Boolean silent;
    protected static String ExceptData[];
    private static String TAG ="waylon_NFCReadWrite";
    public static final String NFC_ID_READ_WRITE = "/sys/devices/platform/config_data/nfc_id_value";
    private static NFCReadWrite instance=null;
    protected static String BTMACAddr;

    public NFCReadWrite(){
        force = false;
        autoClose= 0;
        silent = false;
        ExceptData = new String[4];
    }
    public static NFCReadWrite getInstance(){
        if(instance == null){
            Log.d(TAG, "NFCReadWrite new getInstance: ");
            instance = new NFCReadWrite();
        }
        return instance;
    }
    public String NfCIDReadFile() {
        String prop = "Waiting...";
        StringBuffer buf=new StringBuffer("");
        BufferedReader reader = null;
        String TmpIDData[]= new String[4];;
        try {
            reader = new BufferedReader(new FileReader(NFC_ID_READ_WRITE));
            prop = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, " ***ERROR*** Here is what I know: " + e.getMessage());
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        TmpIDData=prop.split(",");
        for(int i=0;i<4;i++) {
            TmpIDData[i] =Integer.toHexString(Integer.parseInt(TmpIDData[i]));
            buf.append(TmpIDData[i]);
            if( i != 3 )
                buf.append(":");
        }
        prop =buf.toString();
        Log.d(TAG, "readFile cmd from"+NFC_ID_READ_WRITE + "data"+" -> prop = "+prop);

        return prop;
    }

    public void NfCIDWriteFile(String data){
        BufferedWriter bufWriter = null;
        try {
            bufWriter = new BufferedWriter(new FileWriter(NFC_ID_READ_WRITE));
            bufWriter.write(data);
            //bufWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, " can't write: " + data+e.getMessage());
        } finally {
            if(bufWriter != null){
                try {
                    bufWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
