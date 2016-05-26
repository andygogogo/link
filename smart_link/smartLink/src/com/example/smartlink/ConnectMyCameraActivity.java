/**
 * 
 */
package com.example.smartlink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * @author Andy
 *  添加摄像机
 */
public class ConnectMyCameraActivity extends Activity {

	/**
	 * 添加摄像机按钮
	 */
	private ImageButton connectMyCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_my_camera);
		connectMyCamera = (ImageButton) findViewById(R.id.connect);
		
	}
	public void startGreenLightActivity(View view) {
//		startActivity(new Intent(this, CustomizedActivity.class));
		startActivity(new Intent(this, CheckGreenLightActivity.class));
	}

}
