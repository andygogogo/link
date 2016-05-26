package com.example.smartlink.ui;

import java.util.List;

import com.example.smartlink.CheckSoundActivity;
import com.example.smartlink.ConnectMyCameraActivity;
import com.example.smartlink.R;
import com.example.smartlink.SoundWaveActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Andy 显示操作提示的对话框
 */
@SuppressLint("InflateParams")
public class CustomDialog extends Dialog {

	/**
	 * 提示对话框
	 */
	public static final int DIALOG_PROMPT = 0;
	/**
	 * wifi列表对话框
	 */
	public static final int DIALOG_WIFIS = 1;
	public static final int DIALOG_PROBLEM = 2;
	public static final int DIALOG_EXIT_ADD = 3;

	private Context context;

	/**
	 * 标题
	 */
	private String title;
	/**
	 * 图片资源
	 */
	private int imageID;

	/**
	 * 对话框类型
	 */
	private int dialogType;

//	private WifiManager wifiManager;
	/**
	 * wifi列表数据
	 */
	private List<ScanResult> list;
	LayoutInflater inflater;
	/**
	 * wifi对话框右上角刷新的图片
	 */
	ImageView ivRefresh;

	/**
	 * wifi列表适配器
	 */
	private MyAdapter mAdapter;

	/**
	 * 显示wifi列表的listview
	 */
	ListView lvWifis;

	/**
	 * wifi列表被点击时回调接口
	 */
	private onDialogItemClickListener mListener;

	/**
	 * 倒计时页面，点击返回键，退出按钮
	 */
	private Button btnExit;

	/**
	 * 倒计时页面，点击返回键,继续等待按钮
	 */
	private Button btnContinueWait;

	/**
	 * 倒计时页面，计时为0时退出对话框,退出按钮
	 */
	private Button btnExitProblem;

	/**
	 * 倒计时页面，计时为0时退出对话框，重新添加按钮
	 */
	private Button btnAddAgain;

	/**
	 * 对话框按钮点击监听
	 */
//	private ClickListenerInterface listener;
	
	

	public CustomDialog(Context context, String title, int imageID) {
		super(context, R.style.Theme_dialog);
		this.context = context;
		this.title = title;
		this.imageID = imageID;
	}

	public CustomDialog(Context context, int type, onDialogItemClickListener mListener) {
		super(context, R.style.Theme_dialog);
		this.context = context;
		this.dialogType = type;
		this.mListener = mListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	@SuppressLint("InflateParams")
	public void init() {

		LayoutInflater inflater = LayoutInflater.from(context);

		if (this.dialogType == DIALOG_WIFIS) {
			View view = inflater.inflate(R.layout.wifi_dialog, null);
			setContentView(view);

			Button cancel = (Button) view.findViewById(R.id.btnCancel);
			cancel.setOnClickListener(new clickListener());

			ivRefresh = (ImageView) view.findViewById(R.id.ivRefresh);
			ivRefresh.setOnClickListener(new clickListener());

			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			openWifi();
			list = wifiManager.getScanResults();
			mAdapter = new MyAdapter(context, list);

			lvWifis = (ListView) view.findViewById(R.id.lvWifis);

			if (list == null) {
				Toast.makeText(context, "WIFI未打开", Toast.LENGTH_LONG).show();
			} else {
				lvWifis.setAdapter(mAdapter);
			}
		} else if (this.dialogType == DIALOG_PROBLEM) {

			View view = inflater.inflate(R.layout.add_problem_dialog, null);
			setContentView(view);
			btnExit = (Button) view.findViewById(R.id.btnExit);
			btnContinueWait = (Button) view.findViewById(R.id.btnContinueWait);
			return;
		} else if (this.dialogType == DIALOG_EXIT_ADD) {

			View view = inflater.inflate(R.layout.exit_add_dialog, null);
			setContentView(view);
			btnExitProblem = (Button) view.findViewById(R.id.btnExitProblem);
			btnAddAgain = (Button) view.findViewById(R.id.btnAddAgain);
			return;

		} else {
			View view = inflater.inflate(R.layout.custom_dialog, null);
			setContentView(view);

			TextView tvTitle = (TextView) view.findViewById(R.id.title);
			tvTitle.setText(title);

			ImageView ivNoSplash = (ImageView) view.findViewById(R.id.green_light_no_splash);
			ivNoSplash.setImageResource(imageID);

			ImageView ivCancel = (ImageView) view.findViewById(R.id.ivCancle);
			ivCancel.setOnClickListener(new clickListener());

		}

		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
		lp.width = (int) (d.widthPixels * 0.9); // 高度设置为屏幕的0.9
		lp.height = (int) (d.heightPixels * 0.9);
		dialogWindow.setAttributes(lp);
	}
	
	
	

	/**
	 * 打开WIFI
	 */
	private void openWifi() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}

	}

	/**
	 * 刷新wifi列表数据
	 */
	private void refreshWifis() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// 刷新wifi列表数据
		List<ScanResult> refreshList = wifiManager.getScanResults();
		for (int i = 0; i < refreshList.size(); i++) {
			if (list.contains(refreshList.get(i))) {
				list.add(refreshList.get(i));
				mAdapter.notifyDataSetChanged();
			}
		}

	}

	public class dialogClickListener implements DialogInterface.OnClickListener, android.view.View.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			// 出现问题，点击退出
			case R.id.btnExit:
				context.startActivity(new Intent(context, ConnectMyCameraActivity.class));
				break;
			// 倒计时页面，点击返回键，点击退出
			case R.id.btnExitProblem:
				context.startActivity(new Intent(context, SoundWaveActivity.class));
				break;
			// 出现问题，点击重新添加
			case R.id.btnAddAgain:
				context.startActivity(new Intent(context, CheckSoundActivity.class));
				break;
			// 倒计时页面，点击返回键，点击继续等待
			case R.id.btnContinueWait:
				CustomDialog.this.dismiss();
				break;
			default:
				break;
			}
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}

	}

	/**
	 * 对话框点击事件
	 * 
	 * @author Andy
	 *
	 */
	public class clickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {

			int id = v.getId();

			switch (id) {

			// 点击右上角图片关闭对话框
			case R.id.ivCancle:
				// 点击取消按钮关闭对话框
			case R.id.btnCancel:
				CustomDialog.this.dismiss();
				break;
			case R.id.ivRefresh:
				// 加载动画
				Animation rotate = AnimationUtils.loadAnimation(context, R.anim.wifi_refresh);
				// 动画执行完后停留在执行完的状态
				rotate.setFillAfter(true);
				// 避免动画重复执行时停顿
				LinearInterpolator lir = new LinearInterpolator();
				rotate.setInterpolator(lir);
				ivRefresh.startAnimation(rotate);
				refreshWifis();
				break;
			// 出现问题，点击退出
			case R.id.btnExit:
				context.startActivity(new Intent(context, ConnectMyCameraActivity.class));
				break;
			// 倒计时页面，点击返回键，点击退出
			case R.id.btnExitProblem:
				context.startActivity(new Intent(context, SoundWaveActivity.class));
				break;
			// 出现问题，点击重新添加
			case R.id.btnAddAgain:
				context.startActivity(new Intent(context, CheckSoundActivity.class));
				break;
			// 倒计时页面，点击返回键，点击继续等待
			case R.id.btnContinueWait:
				CustomDialog.this.dismiss();
				break;
			default:
				break;
			}

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

		List<ScanResult> list;
		LayoutInflater inflater;

		public MyAdapter(Context context, List<ScanResult> list) {
			this.list = list;
			this.inflater = LayoutInflater.from(context);
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

			View itemView = convertView;
			final ViewHolder holder;

			if (itemView == null) {
				itemView = inflater.inflate(R.layout.item_wifi_lv, null);
				holder = new ViewHolder();
				holder.image = (ImageView) itemView.findViewById(R.id.imageView);
				holder.ssid = (TextView) itemView.findViewById(R.id.tvSSID);
				holder.signal = (TextView) itemView.findViewById(R.id.tvSignal);
				holder.rlWifi = (RelativeLayout) itemView.findViewById(R.id.rlWifi);
				itemView.setTag(holder);
			} else {
				holder = (ViewHolder) itemView.getTag();
			}

			ScanResult scanResult = list.get(position);

			holder.ssid.setText(scanResult.SSID);

			holder.signal.setText(String.valueOf(Math.abs(scanResult.level)));

			// 判断信号强度，显示对应的指示图标
			if (Math.abs(scanResult.level) > 100) {
				holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.stat_sys_signal_evdo_0));
			} else if (Math.abs(scanResult.level) > 80) {
				holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.stat_sys_signal_evdo_1));
			} else if (Math.abs(scanResult.level) > 70) {
				holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.stat_sys_signal_evdo_1));
			} else if (Math.abs(scanResult.level) > 60) {
				holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.stat_sys_signal_evdo_2));
			} else if (Math.abs(scanResult.level) > 50) {
				holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.stat_sys_signal_evdo_3));
			} else {
				holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.stat_sys_signal_evdo_4));
			}

			holder.rlWifi.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.getSSID(holder.ssid.getText().toString());
					CustomDialog.this.dismiss();
				}
			});
			return itemView;
		}

	}

	class ViewHolder {
		TextView ssid;
		TextView signal;
		ImageView image;
		RelativeLayout rlWifi;
	}

	/**
	 * 点击wifi列表时回调的接口
	 * 
	 * @author Andy
	 *
	 */
	public interface onDialogItemClickListener {
		public void getSSID(String ssid);
	}

}
