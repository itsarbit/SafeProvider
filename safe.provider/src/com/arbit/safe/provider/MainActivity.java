package com.arbit.safe.provider;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	final String[] hackMAC = new String[] { "", "" };

	List<ScanResult> wifiList;
	WifiManager myWifiManager;
	WifiScanner myWifiScanner;
	LocationManager locationManager;
	LocationListener mlocListener;
	Button startBtn;

	ArrayList<ArrayList<String>> wifiDataArray;
	ArrayList<String> wifiArray;

	// SSID MAC FREQ LEVEL
	ArrayList<String> locationArray;
	// LONGITUDE LATITUDE
	double getLong = 0;
	double getLat = 0;
	int timeCounter = 0;
	int clickFlag = 0;

	String[] AccessPoint;
	private Handler handler;

	class WifiScanner extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(1000);
				myWifiManager.startScan();
				wifiList = myWifiManager.getScanResults(); // 掃描可用的wifi資訊及數量
				while (wifiList.size() <= 2) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {

					}// 兩百筆之間隔 1000ms
					wifiList = myWifiManager.getScanResults();
				}
				AccessPoint = new String[wifiList.size()]; // 分析剛剛掃描的wifi資料，方便print
															// out
				String str = "";
				wifiDataArray = new ArrayList<ArrayList<String>>();
				for (int i = 0; i < wifiList.size(); i++) {
					AccessPoint[i] = "SSID:, " + wifiList.get(i).SSID
							+ "  ,MAC:, " + wifiList.get(i).BSSID
							+ "  ,freqency:, " + wifiList.get(i).frequency
							+ "  ,Level:, " + wifiList.get(i).level + ", \r\n";
					str += AccessPoint[i];
					wifiArray.set(0, wifiList.get(i).SSID);
					wifiArray.set(1, wifiList.get(i).BSSID);
					wifiArray.set(2, String.valueOf(wifiList.get(i).frequency));
					wifiArray.set(3, String.valueOf(wifiList.get(i).level));
					ArrayList<String> list = new ArrayList<String>(wifiArray);
					wifiDataArray.add(list);

				}

			} catch (InterruptedException e) {

			}// 兩百筆之間隔 1000ms

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startBtn = (Button) findViewById(R.id.button1);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		locationArray = new ArrayList<String>();
		wifiArray = new ArrayList<String>();

		locationArray.add(String.valueOf(0));
		locationArray.add(String.valueOf(0));
		wifiArray.add(String.valueOf(0));
		wifiArray.add(String.valueOf(0));
		wifiArray.add(String.valueOf(0));
		wifiArray.add(String.valueOf(0));

		myWifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE); // 取得wifi
		myWifiScanner = new WifiScanner();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlocListener = new MyLocationListener();

		if (!myWifiManager.isWifiEnabled()) {
			myWifiManager.setWifiEnabled(true);

		} else {

			if (!myWifiManager.startScan())
				Toast.makeText(getApplicationContext(), "Scan unsuccessful",
						Toast.LENGTH_LONG).show();
		}

		IntentFilter myFilter = new IntentFilter();
		myFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(myWifiScanner, myFilter);

		// locationManager.requestLocationUpdates(
		// LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);

		handler = new Handler();

		startBtn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (clickFlag == 0) {
					handler.removeCallbacks(updateLocation);
					handler.postDelayed(updateLocation, 0);
					clickFlag = 1;
				} else {
					handler.removeCallbacks(updateLocation);
				}

			}

		});

	}

	Runnable updateLocation = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			timeCounter += 1000; // 1 sec.

			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);

			if (timeCounter % 5000 == 0) {
				if (!locationArray.get(0).equals("0")) {
					String str = "";
					for (int i = 0; i < wifiDataArray.size(); i++) {
						for (int j = 0; j < wifiDataArray.get(i).size(); j++) {
							str += wifiDataArray.get(i).get(j);
							str += " ";
						}
						str += "\n";
					}
					str += "\n";
					for (int i = 0; i < locationArray.size(); i++) {
						str += locationArray.get(i);
					}

					Toast.makeText(getApplicationContext(), str,
							Toast.LENGTH_LONG).show();
				}
			}
			handler.removeCallbacks(updateLocation);
			handler.postDelayed(updateLocation, 1000);
		}

	};

	public class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			getLong = loc.getLongitude();
			getLat = loc.getLatitude();

			locationArray.set(0, String.valueOf(getLong));
			locationArray.set(1, String.valueOf(getLat));

			locationManager.removeUpdates(mlocListener);
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Disabled",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Enabled",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Toast.makeText(getApplicationContext(), "Status: " + status,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
