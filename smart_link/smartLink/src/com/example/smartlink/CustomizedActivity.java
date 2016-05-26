package com.example.smartlink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import com.hiflying.smartlink.OnSmartLinkListener;
import com.hiflying.smartlink.SmartLinkedModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CustomizedActivity extends Activity implements OnSmartLinkListener {

	private static final String TAG = "CustomizedActivity";

	protected EditText mSsidEditText;
	protected EditText mPasswordEditText;
	protected Button mStartButton, mStopButton;
	protected SnifferSmartLinker mSnifferSmartLinker;

	private boolean mIsConncting = false;

	protected ProgressDialog mWaitingDialog;

	private BroadcastReceiver mWifiChangedReceiver;

	private MulticastLock multicastLock;

	private String newIp;

	private String data;

	protected Handler mViewHandler = new Handler();

	private boolean haveData = true;

	WifiManager wifiManager;

	long netPrefix;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSnifferSmartLinker = SnifferSmartLinker.getInstance();

		mWaitingDialog = new ProgressDialog(this);
		mWaitingDialog.setMessage(getString(R.string.hiflying_smartlinker_waiting));
		mWaitingDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		mWaitingDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {

				mSnifferSmartLinker.setOnSmartLinkListener(null);
				mSnifferSmartLinker.stop();
				mIsConncting = false;
			}
		});

		setContentView(R.layout.activity_customized);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mSsidEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_ssid);
		mPasswordEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_password);
		mStartButton = (Button) findViewById(R.id.button_hiflying_smartlinker_start);
		mStartButton.setEnabled(false);
		mStartButton.setBackgroundColor(Color.BLUE);
		new Thread(new Runnable() {

			@Override
			public void run() {
				data = getIPData();

			}
		}).start();

		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				while (haveData) {
					if (data != null) {
						mStartButton.setEnabled(true);
						mStartButton.setBackgroundColor(Color.RED);
						haveData = false;
					}
				}
			}
		});

		mStopButton = (Button) findViewById(R.id.button_hiflying_smartlinker_stop);
		mSsidEditText.setText(getSSid());
		mPasswordEditText.setText("manniukejiphone");

		mStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mSnifferSmartLinker.stop();
			}
		});

		Toast.makeText(this, "mask===" + wifiManager.getDhcpInfo().netmask, Toast.LENGTH_LONG).show();
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mIsConncting) {
					new Thread(new Runnable() {
						public void run() {
							try {
								mSnifferSmartLinker.setOnSmartLinkListener(CustomizedActivity.this);
								mSnifferSmartLinker.start(getApplicationContext(),
										mPasswordEditText.getText().toString().trim(),
										mSsidEditText.getText().toString().trim(), data);
								mIsConncting = true;
								mWaitingDialog.show();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			}
		});

		mWifiChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
						Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (networkInfo != null && networkInfo.isConnected()) {
					mSsidEditText.setText(getSSid());
					mPasswordEditText.requestFocus();
					mStartButton.setEnabled(true);
				} else {
					mSsidEditText.setText(getString(R.string.hiflying_smartlinker_no_wifi_connectivity));
					mSsidEditText.requestFocus();
					mStartButton.setEnabled(false);
					if (mWaitingDialog.isShowing()) {
						mWaitingDialog.dismiss();
					}
				}
			}
		};
		registerReceiver(mWifiChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		allowMulticast();

	}

	public byte[] cutOutByte(byte[] b, int start, int len) {
		if (b.length == 0 || len == 0) {
			return null;
		}
		byte[] retb = new byte[len];
		for (int i = 0; i < len; i++) {
			retb[i] = b[start + i];
		}
		return retb;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		multicastLock.release();
		mSnifferSmartLinker.setOnSmartLinkListener(null);
		try {
			unregisterReceiver(mWifiChangedReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLinked(final SmartLinkedModule module) {
		// TODO Auto-generated method stub

		Log.w(TAG, "onLinked");
		mViewHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), getString(R.string.hiflying_smartlinker_new_module_found,
						module.getMac(), module.getModuleIP()), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onCompleted() {

		Log.w(TAG, "onCompleted");
		mViewHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), getString(R.string.hiflying_smartlinker_completed),
						Toast.LENGTH_SHORT).show();
				mWaitingDialog.dismiss();
				mIsConncting = false;
			}
		});
	}

	@Override
	public void onTimeOut() {

		Log.w(TAG, "onTimeOut");
		mViewHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), getString(R.string.hiflying_smartlinker_timeout),
						Toast.LENGTH_SHORT).show();
				mWaitingDialog.dismiss();
				mIsConncting = false;
			}
		});
	}

	private String getSSid() {
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		if (wm != null) {
			WifiInfo wi = wm.getConnectionInfo();
			if (wi != null) {
				String ssid = wi.getSSID();
				if (ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
					return ssid.substring(1, ssid.length() - 1);
				} else {
					return ssid;
				}
			}
		}
		return "";
	}

	private void allowMulticast() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("multicast.demo");
		multicastLock.acquire();
	}

	/**
	 * 获取ip 网关 掩码信息
	 * 
	 * @return
	 */
	private String getIPData() {
		// 获取wifi服务

		// 判断wifi是否开启
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		/** ip地址 子网掩码 默认网关 */
		final String ip = intToIp(wifiInfo.getIpAddress());
		final String mask = intToIp(wifiManager.getDhcpInfo().netmask);
		final String gateway = intToIp(wifiManager.getDhcpInfo().gateway);
		Log.i("andy", "原始ip+mask+gateway" + ip + mask + gateway);
		String newIP = getNewIP(ip, 10);

		if (newIP != null) {
			data = newIP + "." + mask + "." + gateway;
			Log.i("andy", "分配新ip+mask+gateway" + data);
			return data;
		} else {
			return null;
		}

	}

	/**
	 * 通过本机ip获取新的ip，并检查是否被占用
	 * 
	 * @param ip
	 * @param rang
	 * @return
	 */
	private String getNewIP(final String ip, int rang) {
		if (ip != null) {
			String[] ips = ip.split("\\.");
			String i = String.valueOf((Integer.parseInt(ips[3]) + rang));
			if (Integer.parseInt(i) < 256) {

				newIp = ips[0] + "." + ips[1] + "." + ips[2] + "." + i;

			} else {
				i = String.valueOf((Integer.parseInt(ips[3]) - rang));
				newIp = ips[0] + "." + ips[1] + "." + ips[2] + "." + i;
			}
			if (!ping(newIp)) {
				return newIp;
			}
			return null;
		} else {
			return null;
		}

	}

	/**
	 * 将获取到的ip 网关 掩码信息转化为string
	 * 
	 * @param i
	 * @return
	 */
	private String intToIp(int i) {
		Log.i("andy",
				"intToIp" + (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF));
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
	}


	/**
	 * 检查ip是否被占用
	 * 
	 * @param ip
	 * @return
	 */
	private static final boolean ping(String ip) {

		String result = null;

		try {

			// String ip = "192.168.0.129";// 除非百度挂了，否则用这个应该没问题~

			Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping3次

			// 读取ping的内容，可不加。

			InputStream input = p.getInputStream();

			BufferedReader in = new BufferedReader(new InputStreamReader(input));

			StringBuffer stringBuffer = new StringBuffer();

			String content = "";

			while ((content = in.readLine()) != null) {

				stringBuffer.append(content);

			}

			Log.i("andy", "result content : " + stringBuffer.toString());

			// PING的状态

			int status = p.waitFor();

			if (status == 0) {

				result = "successful~";

				return true;

			} else {

				result = "failed~ cannot reach the IP address";

			}

		} catch (IOException e) {

			result = "failed~ IOException";

		} catch (InterruptedException e) {

			result = "failed~ InterruptedException";

		} finally {

			Log.i("andy", "result = " + result);

		}

		return false;

	}

}
