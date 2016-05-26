package com.example.smartlink;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.smartlink.ui.DotProgressBar;
import com.hiflying.smartlink.v3.SnifferSmartLinkerActivity;
import com.tencent.bugly.crashreport.CrashReport;

public class MainActivity extends Activity {
	
	private DotProgressBar dot_progress_bar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((TextView) findViewById(R.id.textView_version)).setText("Version: " + getVersionName());
//		dot_progress_bar = (DotProgressBar) findViewById(R.id.dot_progress_bar);
		
//		dot_progress_bar.setStartColor(startColor);
//		dot_progress_bar.setEndColor(endColor);
//		dot_progress_bar.setDotAmount(amount);
//		dot_progress_bar.setAnimationTime(100);
	}

	public void startSnifferSmartLinkerActivity(View view) {
		startActivity(new Intent(this, SnifferSmartLinkerActivity.class));
	}

	public void startSnifferSmartLinkerFragment(View view) {
		startActivity(new Intent(this, SnifferSmartLinkerFragmentActivity.class));
	}

	public void startCustomizedActivity(View view) {
//		startActivity(new Intent(this, CustomizedActivity.class));
		startActivity(new Intent(this, CheckGreenLightActivity.class));
	}

	private String getVersionName() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "unknown";
	}
}
