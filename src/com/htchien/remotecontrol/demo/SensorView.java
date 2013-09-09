package com.htchien.remotecontrol.demo;

import android.*;
import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by tedchien on 13/9/2.
 */
public class SensorView extends View {
	final String TAG = SensorView.class.getSimpleName();

	final int dotRadius = 10;

	Paint mPaint;
	int mX = 0, mY = 0;

	public SensorView(Context context) {
		super(context);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(getResources().getColor(R.color.holo_red_dark));
	}

	public void updateSensorPosition(int type, float x, float y, float z) {
		mX = (int)(x * 100f);
		mY = (int)(y * 100f);
		postInvalidate();
		Log.d(TAG, "updateSensorPosition - type: " + type + "\tx: " + mX + "\ty: " + mY + "\tz: " + z);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle((getWidth() / 2) + mX, (getHeight() / 2) + mY, dotRadius, mPaint);
	}
}
