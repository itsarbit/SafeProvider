package com.arbit.safe.provider;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	List<ScanResult> wifiList; // 型態為ScanResult的List，專門儲存wifi的資訊
	WifiManager myWifiManager;
	WifiScanner myWifiScanner;
	String[] AccessPoint;
	String[] Coordinate;
	int temp = 0;
	TextView wifi_msg, output_msg;
	// EditText et_x,et_y,et_z;
	Button bt;
	FileWriter fw;
	BufferedWriter bw;
	int wifi_update_no, output_update_no, write_count;

	class WifiScanner extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(200);
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

				for (int i = 0; i < wifiList.size(); i++) {
					AccessPoint[i] = "SSID:, " + wifiList.get(i).SSID
							+ "  ,MAC:, " + wifiList.get(i).BSSID
							+ "  ,freqency:, " + wifiList.get(i).frequency
							+ "  ,Level:, " + wifiList.get(i).level + ", \r\n";
				}
				// Toast.makeText(WifiExample02.this, "Wifi success",
				// Toast.LENGTH_LONG).show();
				if (write_count > 0 && temp != AccessPoint.length) {
					write_count = write_count - 1;
//					write_199();
					temp = AccessPoint.length;
				}
			} catch (InterruptedException e) {

			}// 兩百筆之間隔 1000ms
			wifi_msg.setText("更新次數: " + String.valueOf(++wifi_update_no) + "次");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
