package com.example.smartlink;

import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;

/**
 * 配置腾讯的Bugly用到的全局application
 * @author Andy
 *
 */
public class SmartLinkApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		CrashReport.initCrashReport(getApplicationContext(), "900028866", false);
	}
	
}
