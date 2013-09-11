package com.htchien.remotecontrol.demo;

import android.*;
import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
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

	public void updateSensorPosition(float[] values) {
		mX = (int)(values[0] * 100f);
		mY = (int)(values[1] * 100f);
		postInvalidate();
		Log.d(TAG, "updateSensorPosition - values: " + mX + ", " + mY);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(TAG, "onDraw");
		canvas.drawCircle((getWidth() / 2) + mX, (getHeight() / 2) + mY, dotRadius, mPaint);
	}
}
