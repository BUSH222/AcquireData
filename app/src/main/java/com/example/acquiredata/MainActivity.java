package com.example.acquiredata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.net.URL;
import java.net.HttpURLConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONObject;


public class MainActivity extends Activity implements View.OnClickListener{




    WifiManager wifiManager;
    WifiScanReceiver wifiReciever;
    ArrayList<String> wifis;
    WifiInfo wifiInfo;
    Map<String, String> BSSIDS = new HashMap<>();
    Boolean isScanON;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mClickButton1 = (Button) findViewById(R.id.button);
        mClickButton1.setOnClickListener(this);
        Button mClickButton2 = (Button) findViewById(R.id.button2);
        mClickButton2.setOnClickListener(this);

        Spinner spinnernum = findViewById(R.id.spinner_num);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.num, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnernum.setAdapter(adapter);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wifiInfo = wifiManager.getConnectionInfo();
        isScanON = Boolean.FALSE;

        wifis = new ArrayList<>();
        BSSIDS = new HashMap<>();

    }

    Thread DoRequest = new Thread() {
        @SuppressLint("SetTextI18n")
        public void run() {
            try {
                EditText medit = (EditText)findViewById(R.id.ipinput);
                URL url = new URL("http://" + medit.getText().toString() + "/testdata");
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Content-Type", "application/json");
                Spinner mySpinner = (Spinner) findViewById(R.id.spinner_num);
                BSSIDS.put("NUM", mySpinner.getSelectedItem().toString());
                JSONObject BSSIDS_TO_JSON = new JSONObject(BSSIDS);
                httpConn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
                writer.write(String.valueOf(BSSIDS_TO_JSON));
                writer.flush();
                writer.close();
                httpConn.getOutputStream().close();
                InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                        ? httpConn.getInputStream()
                        : httpConn.getErrorStream();
                Scanner s = new Scanner(responseStream).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                System.out.println(response);
                wifis.clear();
                BSSIDS.clear();
                TextView serverresponse = findViewById(R.id.ServerResponse);
                serverresponse.setText("Server response: Success!");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR", "REQUEST ERROR");
                TextView serverresponse = findViewById(R.id.ServerResponse);
                serverresponse.setText("Server response: Request Failure");
            }

        }

    };

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button: {
                TextView scanstartedtext = findViewById(R.id.isScanOn);
                scanstartedtext.setText("SCAN IS ON!!");
                TextView serverresponse = findViewById(R.id.ServerResponse);
                serverresponse.setText("Server response: -");
                isScanON = Boolean.TRUE;
                wifiManager.startScan();


                break;
            }

            case R.id.button2: {
                Log.i("HULLO", "scan stopped");
                TextView serverresponse = findViewById(R.id.ServerResponse);
                serverresponse.setText("Server response: Awaiting...");
                TextView scanstartedtext = findViewById(R.id.isScanOn);
                scanstartedtext.setText("SCAN IS OFF!!");
                isScanON = Boolean.FALSE;
                System.out.println(BSSIDS);
                new Thread(DoRequest).start();
                break;
            }

        }
    }




    protected void onPause() {
        unregisterReceiver(wifiReciever);

        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("SetTextI18n")
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            Log.d("THETAG", "bruh scan");
            for (int i = 0; i < wifiScanList.size(); i++) {
                String ssid = wifiScanList.get(i).SSID;
                String bssid =  wifiScanList.get(i).BSSID;
                int rssi =  wifiScanList.get(i).level;
                wifis.add("SSID: " + ssid + "\nBSSID: " + bssid + "\nRSSI: " + rssi);
                BSSIDS.put(bssid, String.valueOf(rssi));
            }
            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            TextView textView = findViewById(R.id.lastscantime);
            textView.setText("Last Update:\n" + currentDateTimeString);
            if (isScanON) {
                wifiManager.startScan();
            }


        }
    }
}