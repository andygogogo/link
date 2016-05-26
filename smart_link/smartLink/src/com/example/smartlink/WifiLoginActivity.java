package com.example.smartlink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.example.smartlink.ui.CustomDialog;
import com.example.smartlink.ui.CustomDialog.onDialogItemClickListener;
import com.hiflying.smartlink.OnSmartLinkListener;
import com.hiflying.smartlink.SmartLinkedModule;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 连接wifi页面
 * 
 * @author Andy
 *
 */
public class WifiLoginActivity extends Activity implements OnClickListener, OnSmartLinkListener {

	private static final String TAG = "WifiLoginActivity";

	/**
	 * 摄像机对wifi有什么要求？
	 */
	private TextView tvWifiRequired;

	/**
	 * 登陆wifi返回按钮
	 */
	private Button btnWifiBack;

	/**
	 * 下箭头，点击后显示wifi列表
	 */
	private ImageView ivWifis;

	/**
	 * ssid编辑框
	 */
	private EditText etSSID;

	/**
	 * 用户输入密码
	 */
	private EditText etPassword;

	/**
	 * 显示和隐藏密码
	 */
	private TextView tvDisplayPwd;

	/**
	 * 密码是否隐藏标识，默认隐藏
	 */
	private Boolean isHidden = true;

	/**
	 * 连接wifi按钮
	 */
	private Button connectWifi;

	private boolean mIsConncting = false;

	protected SnifferSmartLinker mSnifferSmartLinker;

	/**
	 * 手机当前的ip
	 */
	public static String ip = "";

	/**
	 * 给设备分配的ip 子网掩码和网关信息
	 */
	private String data;

	/**
	 * 未被占用的ip
	 */
	private String newIp;

	/**
	 * 组播锁
	 */
	private MulticastLock multicastLock;

	private BroadcastReceiver mWifiChangedReceiver;

	protected Handler mViewHandler = new Handler();

	private boolean gettingData = true;

	protected ProgressDialog mWaitingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSnifferSmartLinker = SnifferSmartLinker.getInstance();
		initWaitingDialog();
		setContentView(R.layout.wifi_login);
		initView();
		initData();
		etSSID.setText(getSSid());
		etPassword.setText("manniukejiphone");
		initBroadcastReceiver();
		allowMulticast();
	}

	// 初始化要发组播的数据
	private void initData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				data = getIPData();

			}
		}).start();

		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				while (gettingData) {
					if (data != null) {
						connectWifi.setEnabled(true);
						connectWifi.setBackgroundColor(Color.RED);
						gettingData = false;
					}
				}
			}
		});
	}

	// 初始化广播接收者，接收wifi变化的广播
	private void initBroadcastReceiver() {
		mWifiChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (networkInfo != null && networkInfo.isConnected()) {
					etSSID.setText(getSSid());
					etPassword.requestFocus();
					connectWifi.setEnabled(true);
				} else {
					etSSID.setText(getString(R.string.hiflying_smartlinker_no_wifi_connectivity));
					etSSID.requestFocus();
					connectWifi.setEnabled(false);
					 if (mWaitingDialog.isShowing()) {
					 mWaitingDialog.dismiss();
					 }
				}
			}
		};
		registerReceiver(mWifiChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	// 初始化等待条
	private void initWaitingDialog() {
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
	}

	//获取ssid，比如我连的wifi名是macrocheng,那么在4.0以后的系统版本中getSSID返回的是 "macrocheng",注意是有分号括起来的，
	//而在2.x的系统版本中返回的是 macrocheng
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

	//打开组播锁，默认组播锁是关闭的，开启后很耗电
	private void allowMulticast() {
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		multicastLock = wm.createMulticastLock("multicast.demo");
		multicastLock.acquire();
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		tvWifiRequired = (TextView) findViewById(R.id.tvWifiRequired);
		btnWifiBack = (Button) findViewById(R.id.btnWifiBack);
		ivWifis = (ImageView) findViewById(R.id.ivWifis);
		etSSID = (EditText) findViewById(R.id.etSSID);
		etPassword = (EditText) findViewById(R.id.etPassword);
		tvDisplayPwd = (TextView) findViewById(R.id.tvDisplayPwd);
		
		connectWifi = (Button) findViewById(R.id.btnConnWifi);
		connectWifi.setEnabled(false);
		connectWifi.setBackgroundColor(Color.BLUE);

		tvWifiRequired.setOnClickListener(this);
		btnWifiBack.setOnClickListener(this);
		ivWifis.setOnClickListener(this);
		tvDisplayPwd.setOnClickListener(this);
		connectWifi.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvWifiRequired:
			new CustomDialog(this, "摄像机对WiFi有什么要求？", R.drawable.camera_commend).show();
			break;
		case R.id.btnWifiBack:
			finish();
			break;
		case R.id.ivWifis:
			new CustomDialog(this, CustomDialog.DIALOG_WIFIS, new onDialogItemClickListener() {

				@Override
				public void getSSID(String ssid) {
					etSSID.setText(ssid);
				}

			}).show();
			break;
		case R.id.tvDisplayPwd:
			if (isHidden) {
				// 设置可见
				etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				tvDisplayPwd.setText("隐藏密码");
			} else {
				// 设置隐藏
				etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
				tvDisplayPwd.setText("显示密码");
			}
			isHidden = !isHidden;
			etPassword.postInvalidate();
			// 切换后将EditText光标置于末尾
			CharSequence charSequence = etPassword.getText();
			if (charSequence instanceof Spannable) {
				Spannable spanText = (Spannable) charSequence;
				Selection.setSelection(spanText, charSequence.length());
			}
			break;
		case R.id.btnConnWifi:
			// 点击连接wifi按钮,同时发送声波和组播
			if (etPassword.getText().toString().equals("")) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("提示");
				builder.setMessage("请检查ssid和密码是否为空！");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // 设置确定按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss(); // 关闭dialog
					}
				});
				builder.show();
			} else {
				// 将得到的未被占用的ssid和密码保存起来，以后发广播用
				ConstantValue.ssid = etSSID.getText().toString().trim();
				ConstantValue.password = etPassword.getText().toString().trim();
				if (!mIsConncting) {
					new Thread(new Runnable() {
						public void run() {
							try {
								// 开始发送组播
								mSnifferSmartLinker.setOnSmartLinkListener(WifiLoginActivity.this);
								mSnifferSmartLinker.start(getApplicationContext(),
										etPassword.getText().toString().trim(), etSSID.getText().toString().trim(),
										data);
								mIsConncting = true;
								mWaitingDialog.show();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}

				startActivity(new Intent(this, ConnectingActivity.class));
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onLinked(final SmartLinkedModule module) {
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		multicastLock.release();
		mSnifferSmartLinker.stop();
		mSnifferSmartLinker.setOnSmartLinkListener(null);
		try {
			unregisterReceiver(mWifiChangedReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取ip 网关 掩码信息
	 * 
	 * @return
	 */
	private String getIPData() {
		// 获取wifi服务
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
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
			// 将得到的未被占用的ip保存起来，以后发广播用
			ConstantValue.ip = newIP;
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
