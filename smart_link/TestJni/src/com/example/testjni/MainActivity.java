package com.example.testjni;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.smartlink.SDK;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		byte[] b = new byte[7];
		b[0] = 1;
		b[1] = 8;
		b[2] = 0;
		b[3] = 109;
		b[4] = 0;
		b[5] = 112;
		b[6] = 0;
		
		byte[] out = new byte[2];
		int r = SDK.SlaveCrc8(b, b.length, out);
		Log.d("out[]:", out[0]+"|"+out[1]);
		Toast.makeText(this, "crc8:" + r+"|"+ out[0]+"|"+out[1], Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
