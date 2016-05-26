package com.example.smartlink.ui;

import com.example.smartlink.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class TransformingView extends LinearLayout {

	private Context context;
	
	private RelativeLayout rl_parent;
	
	private int indicatorWidth;
    private int indicatorHeight;
    private int indicatorGap;
    private int cornerRadius;
    private Drawable selectDrawable;
    private Drawable unSelectDrawable;
    private int strokeWidth;
    private int strokeColor;
    private boolean isSnap;
	
	public TransformingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
        setClipChildren(false);
        setClipToPadding(false);

        rl_parent = new RelativeLayout(context);
        rl_parent.setClipChildren(false);
        rl_parent.setClipToPadding(false);
        addView(rl_parent);
        
        setGravity(Gravity.CENTER);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlycoPageIndicaor);
        indicatorWidth = ta.getDimensionPixelSize(R.styleable.FlycoPageIndicaor_fpi_width, dp2px(6));
        indicatorHeight = ta.getDimensionPixelSize(R.styleable.FlycoPageIndicaor_fpi_height, dp2px(6));
        indicatorGap = ta.getDimensionPixelSize(R.styleable.FlycoPageIndicaor_fpi_gap, dp2px(8));
        cornerRadius = ta.getDimensionPixelSize(R.styleable.FlycoPageIndicaor_fpi_cornerRadius, dp2px(3));
        strokeWidth = ta.getDimensionPixelSize(R.styleable.FlycoPageIndicaor_fpi_strokeWidth, dp2px(0));
        strokeColor = ta.getColor(R.styleable.FlycoPageIndicaor_fpi_strokeColor, Color.parseColor("#ffffff"));
        isSnap = ta.getBoolean(R.styleable.FlycoPageIndicaor_fpi_isSnap, false);

        int selectColor = ta.getColor(R.styleable.FlycoPageIndicaor_fpi_selectColor, Color.parseColor("#ffffff"));
        int unselectColor = ta.getColor(R.styleable.FlycoPageIndicaor_fpi_unselectColor, Color.parseColor("#88ffffff"));
        int selectRes = ta.getResourceId(R.styleable.FlycoPageIndicaor_fpi_selectRes, 0);
        int unselectRes = ta.getResourceId(R.styleable.FlycoPageIndicaor_fpi_unselectRes, 0);
        ta.recycle();
        
        if (selectRes != 0) {
            this.selectDrawable = getResources().getDrawable(selectRes);
        } else {
            this.selectDrawable = getDrawable(selectColor, cornerRadius);
        }

        if (unselectRes != 0) {
            this.unSelectDrawable = getResources().getDrawable(unselectRes);
        } else {
            this.unSelectDrawable = getDrawable(unselectColor, cornerRadius);
        }
	}
	
	
	 private GradientDrawable getDrawable(int color, float raduis) {
	        GradientDrawable drawable = new GradientDrawable();
	        drawable.setCornerRadius(raduis);
	        drawable.setStroke(strokeWidth, strokeColor);
	        drawable.setColor(color);

	        return drawable;
	    }
	
	private int dp2px(float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
