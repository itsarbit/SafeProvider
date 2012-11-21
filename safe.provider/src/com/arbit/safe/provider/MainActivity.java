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
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	List<ScanResult> wifiList;
	WifiManager myWifiManager;
	WifiScanner myWifiScanner;
	LocationManager locationManager;
	LocationListener mlocListener;
	ArrayList<ArrayList<String>> dataArray;
	ArrayList<String> wifiArray;
	// SSID MAC FREQ LEVEL
	ArrayList<String> locationArray;
	// LONGITUDE LATITUDE
	double getLong = 0;
	double getLat = 0;

	String[] AccessPoint;
	String[] Coordinate;
	int temp = 0;

	Button bt;
	FileWriter fw;
	BufferedWriter bw;
	int wifi_update_no, output_update_no, write_count;

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
				for (int i = 0; i < wifiList.size(); i++) {
					AccessPoint[i] = "SSID:, " + wifiList.get(i).SSID
							+ "  ,MAC:, " + wifiList.get(i).BSSID
							+ "  ,freqency:, " + wifiList.get(i).frequency
							+ "  ,Level:, " + wifiList.get(i).level + ", \r\n";
					str += AccessPoint[i];
					wifiArray.add(wifiList.get(i).SSID);
					wifiArray.add(wifiList.get(i).BSSID);
					wifiArray.add(String.valueOf(wifiList.get(i).frequency));
					wifiArray.add(String.valueOf(wifiList.get(i).level));

				}

				if (write_count > 0 && temp != AccessPoint.length) {
					write_count = write_count - 1;
					// write_199();
					temp = AccessPoint.length;
				}
			} catch (InterruptedException e) {

			}// 兩百筆之間隔 1000ms

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
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

	}

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
