package com.arbit.safe.provider;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static String MSG_TAG = "SafeProvider";
	private final static String WIFI_CACHE_PATH = "/data/data/com.google.android.location/files/cache.wifi";
	private final static String BUF_WIFI_CACHE_PATH = "/data/data/com.arbit.safe.provider/files/cache.wifi";
	private String mWifiCacheStr = "Cache doesn't exist";

	final String[] hackMAC = new String[] { "aa:bb:cc:dd:ee:ff",
			"gg:hh:ii:jj:kk:ll" };

	List<ScanResult> wifiList;
	WifiManager myWifiManager;
	WifiScanner myWifiScanner;
	LocationManager locationManager;
	LocationListener mlocListener;
	Button startBtn;

	ArrayList<ArrayList<String>> mCacheDataArray;
	ArrayList<String> mCacheMACArray;
	ArrayList<String> mCacheLongArray;
	ArrayList<String> mCacheLatArray;
	ArrayList<String> mCacheAccuArray;
	ArrayList<String> mCacheConfArray;

	ArrayList<ArrayList<String>> wifiDataArray;
	ArrayList<String> wifiArray;
	// SSID MAC FREQ LEVEL
	ArrayList<String> locationArray;
	// LONGITUDE LATITUDE
	ArrayList<String> reFineLocationArray;

	double getLong = 0;
	double getLat = 0;
	int timeCounter = 0;
	int clickFlag = 0;

	String[] AccessPoint;
	private Handler handler;

	private File mWifiCache = null;
	private boolean mWifiCacheExist = false;
	private LocationCacheDatabase mWifiCacheDatabase = null;
	protected ArrayList<String> mCommands = null;

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

			}

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startBtn = (Button) findViewById(R.id.button1);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		locationArray = new ArrayList<String>();
		reFineLocationArray = new ArrayList<String>();
		wifiArray = new ArrayList<String>();

		locationArray.add(String.valueOf(0));
		locationArray.add(String.valueOf(0));
		reFineLocationArray.add(String.valueOf(0));
		reFineLocationArray.add(String.valueOf(0));
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
		createFile();
		handler = new Handler();

		startBtn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (clickFlag == 0) {
					handler.removeCallbacks(updateLocation);
					handler.postDelayed(updateLocation, 0);
					clickFlag = 1;
					startBtn.setText("stop request");
				} else {
					startBtn.setText("start request");
					handler.removeCallbacks(updateLocation);
				}

			}

		});

	}

	Runnable updateLocation = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			timeCounter += 1000;

			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);

			if (timeCounter % 10000 == 0) {

				if (checkCache()) {
					ddCommand();
					readCache(1);
					int numberOfLocWifi = wifiDataArray.size();
					double totalLong = 0;
					double totalLat = 0;
					if (numberOfLocWifi >= 10)
						numberOfLocWifi = 10;

					totalLong = Double.parseDouble(locationArray.get(0))
							* numberOfLocWifi;
					totalLat = Double.parseDouble(locationArray.get(1))
							* numberOfLocWifi;

					for (int index = 1; index < mCacheDataArray.get(0).size(); index++) {

						for (int hackIndex = 1; hackIndex < hackMAC.length; hackIndex++)
							if (mCacheDataArray.get(0).get(index)
									.equals(hackMAC[hackIndex])) {
								totalLong = totalLong
										- Double.parseDouble(mCacheDataArray
												.get(1).get(index));
								totalLat = totalLat
										- Double.parseDouble(mCacheDataArray
												.get(2).get(index));
							}
					}
					reFineLocationArray.set(0, String.valueOf(totalLong));
					reFineLocationArray.set(0, String.valueOf(totalLat));

				} else {
					Log.w(MSG_TAG, "no cache exist!");

				}
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
						str += " ";
					}
					str += "\n";
					for (int i = 0; i < reFineLocationArray.size(); i++) {
						str += reFineLocationArray.get(i);
						str += " ";
					}

					Toast.makeText(getApplicationContext(), str,
							Toast.LENGTH_LONG).show();
				}
			}
			handler.removeCallbacks(updateLocation);
			handler.postDelayed(updateLocation, 1000);
		}

	};

	void createFile() {
		mWifiCache = new File(WIFI_CACHE_PATH);
	}

	private void ddCommand() {
		mCommands = new ArrayList<String>();
		mCommands
				.add("dd if=/data/data/com.google.android.location/files/cache.wifi of=/data/data/com.arbit.safe.provider/files/cache.wifi");

	}

	private final boolean execute() {
		boolean retval = false;
		try {
			ArrayList<String> commands = mCommands;
			if (null != commands && commands.size() > 0) {
				Process suProcess = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(
						suProcess.getOutputStream());

				// Execute commands that require root access
				for (String currCommand : commands) {
					os.writeBytes(currCommand + "\n");
					os.flush();
				}

				os.writeBytes("exit\n");
				os.flush();

				try {
					int suProcessRetval = suProcess.waitFor();
					if (255 != suProcessRetval) {
						// Root access granted
						retval = true;
					} else {
						// Root access denied
						retval = false;
					}
				} catch (Exception ex) {
					Log.w(MSG_TAG, ex);
				}
			}
		} catch (IOException ex) {
			Log.w(MSG_TAG, "Can't get root access", ex);
		} catch (SecurityException ex) {
			Log.w(MSG_TAG, "Can't get root access", ex);
		} catch (Exception ex) {
			Log.w(MSG_TAG, "Error executing internal operation", ex);
		}
		return retval;
	}

	private boolean checkCache() {
		mWifiCacheExist = mWifiCache.exists();

		if (!mWifiCacheExist || mWifiCache.length() < 4) {
			Log.w(MSG_TAG, "Cache Doesn't Exist");
			return false;
		} else {
			Log.w(MSG_TAG, "Cache Exists");

			return true;
		}
	}

	private void readCache(int f) {
		if (execute()) {
			try {

				if (mWifiCacheExist) {
					mWifiCacheDatabase = new LocationCacheDatabase("wifi",
							BUF_WIFI_CACHE_PATH);
				}
				Log.w(MSG_TAG, "Cache Copied");
			} catch (Exception e) {
				e.printStackTrace();
			}
			setStatus();
		} else {
			Log.w(MSG_TAG, "Failed to Copy");
		}
	}

	private void setStatus() {

		if (mWifiCacheExist) {

			mCacheMACArray = new ArrayList<String>();
			mCacheLatArray = new ArrayList<String>();
			mCacheLongArray = new ArrayList<String>();
			mCacheConfArray = new ArrayList<String>();
			mCacheAccuArray = new ArrayList<String>();
			for (LocationCacheEntrie entrie : mWifiCacheDatabase.getEntries()) {

				mCacheMACArray.add(entrie.getKey());
				mCacheLongArray.add(String.valueOf(entrie.getLongitude()));
				mCacheLatArray.add(String.valueOf(entrie.getLatitude()));
				mCacheConfArray.add(String.valueOf(entrie.getConfidence()));
				mCacheAccuArray.add(String.valueOf(entrie.getAccuracy()));
			}
			ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
			list.add(mCacheMACArray);
			list.add(mCacheLongArray);
			list.add(mCacheLatArray);
			list.add(mCacheAccuArray);
			list.add(mCacheConfArray);

			mCacheDataArray = new ArrayList<ArrayList<String>>(list);
		}

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

	double getDistance(double latitude, double longitude) {
		return Math.sqrt(Math.pow(latitude, 2) + Math.pow(longitude, 2));
	}
}
