package com.example.smartlink;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * 调大手机音量
 * 
 * @author Andy
 *
 */
public class RaiseVolumeActivity extends Activity  implements OnClickListener{
	/**
	 * 下一步按钮
	 */
	private Button btnNext;
	
	/**
	 * 返回按钮
	 */
	private Button btnRaiseVolumeBack;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState){
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raise_volume);
		btnRaiseVolumeBack = (Button) findViewById(R.id.btnRaiseVolumeBack);
		btnNext = (Button) findViewById(R.id.btnNext);
		
		btnRaiseVolumeBack.setOnClickListener(this);
		btnNext.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btnRaiseVolumeBack:
			finish();
			break;
		case R.id.btnNext:
			//跳转到连接wifi登陆界面
			startActivity(new Intent(this,WifiLoginActivity.class));
			break;
		default:
			break;
		}
		
	}

}
