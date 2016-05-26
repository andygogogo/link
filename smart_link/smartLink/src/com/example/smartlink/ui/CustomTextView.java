package com.example.smartlink.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {
	
	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		this.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		this.getPaint().setAntiAlias(true);
		super.onDraw(canvas);
	}

}
