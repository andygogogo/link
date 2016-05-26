package com.example.smartlink;

import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * wifi列表页面
 * 
 * @author Andy
 *
 */
public class WifiListActivity extends Activity {

	private WifiManager wifiManager;
	private List<ScanResult> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.wifi_dialog);
		init();
	}

	private void init() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		openWifi();
		list = wifiManager.getScanResults();
		ListView lvWifis = (ListView) findViewById(R.id.lvWifis);
		if (list == null) {
			Toast.makeText(this, "WIFI未打开", Toast.LENGTH_LONG).show();
		} else {
			lvWifis.setAdapter(new MyAdapter(this, list));
		}
	}

	/**
	 * 打开WIFI
	 */
	private void openWifi() {
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}

	}

	/**
	 * 显示wifi列表的适配器
	 * 
	 * @author Andy
	 *
	 */
	@SuppressLint({ "ViewHolder", "InflateParams" })
	public class MyAdapter extends BaseAdapter {

		LayoutInflater inflater;
		List<ScanResult> list;

		public MyAdapter(Context context, List<ScanResult> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view = null;
			view = inflater.inflate(R.layout.item_wifi_lv, null);
			ScanResult scanResult = list.get(position);
			
			TextView textView = (TextView) view.findViewById(R.id.tvSSID);
			textView.setText(scanResult.SSID);
			
			TextView signalStrenth = (TextView) view.findViewById(R.id.tvSignal);
			signalStrenth.setText(String.valueOf(Math.abs(scanResult.level)));
			
			ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
			// 判断信号强度，显示对应的指示图标
			if (Math.abs(scanResult.level) > 100) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_signal_evdo_0));
			} else if (Math.abs(scanResult.level) > 80) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_signal_evdo_1));
			} else if (Math.abs(scanResult.level) > 70) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_signal_evdo_1));
			} else if (Math.abs(scanResult.level) > 60) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_signal_evdo_2));
			} else if (Math.abs(scanResult.level) > 50) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_signal_evdo_3));
			} else {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_signal_evdo_4));
			}
			return view;
		}

	}
	
	class ViewHolder{
		TextView ssid;
		TextView signal;
		ImageView image;
	}

}
