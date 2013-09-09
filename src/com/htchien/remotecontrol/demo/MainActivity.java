package com.htchien.remotecontrol.demo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class MainActivity extends Activity implements SensorEventListener, ISensorViewUpdater {
	final String TAG = MainActivity.class.getSimpleName();

	CheckBox mCheckBoxServer;
	EditText mEditTextServerIP;
	Spinner mSpinnerSensorType;
	int mSensorType = Sensor.TYPE_ACCELEROMETER;
	SensorView mSensorView;

	UDPServer mUDPServer = null;
	ServerTask mServerTask = null;

	UDPClient mUDPClient = null;

	SensorManager mSensorManager;
	Sensor mAccelerometerSensor;
	Sensor mGravitySensor;
	Sensor mGyroscopeSensor;
	Sensor mLinearAccelerometerSensor;
	Sensor mRotationVectorSensor;
	boolean sensorRegistered = false;

	@Override
	public void onSensorChanged(final SensorEvent event) {
		String sensorText = "";
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			sensorText += "Accelerometer:\n";
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[0], event.values[0] > 1.0f ? "Left" : event.values[0] < -1.0f ? "Right" : "XCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[1], event.values[1] > 1.0f ? "Up" : event.values[1] < -1.0f ? "Down" : "YCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[2], event.values[2] > 1.0f ? "Plus" : event.values[2] < -1.0f ? "Minus" : "ZCenter" );
		}

		if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			sensorText += "Gravity:\n";
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[0], event.values[0] > 1.0f ? "Left" : event.values[0] < -1.0f ? "Right" : "XCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[1], event.values[1] > 1.0f ? "Up" : event.values[1] < -1.0f ? "Down" : "YCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[2], event.values[2] > 1.0f ? "Plus" : event.values[2] < -1.0f ? "Minus" : "ZCenter" );
		}

		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			sensorText += "Gyroscope:\n";
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[0], event.values[0] > 1.0f ? "Left" : event.values[0] < -1.0f ? "Right" : "XCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[1], event.values[1] > 1.0f ? "Up" : event.values[1] < -1.0f ? "Down" : "YCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[2], event.values[2] > 1.0f ? "Plus" : event.values[2] < -1.0f ? "Minus" : "ZCenter" );
		}

		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			sensorText += "Linear Acceleration:\n";
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[0], event.values[0] > 1.0f ? "Left" : event.values[0] < -1.0f ? "Right" : "XCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[1], event.values[1] > 1.0f ? "Up" : event.values[1] < -1.0f ? "Down" : "YCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[2], event.values[2] > 1.0f ? "Plus" : event.values[2] < -1.0f ? "Minus" : "ZCenter" );
		}

		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			sensorText += "Rotation Vector:\n";
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[0], event.values[0] > 1.0f ? "Left" : event.values[0] < -1.0f ? "Right" : "XCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[1], event.values[1] > 1.0f ? "Up" : event.values[1] < -1.0f ? "Down" : "YCenter" );
			sensorText += String.format("\tX:\t%f\t\t%s\n", event.values[2], event.values[2] > 1.0f ? "Plus" : event.values[2] < -1.0f ? "Minus" : "ZCenter" );
		}

		if (mSensorType == event.sensor.getType()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (mUDPClient != null)
						mUDPClient.send(event);
				}
			}).start();
		}
		Log.d(TAG, sensorText);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void updateView(int type, float x, float y, float z) {
		Log.d(TAG, "updateView - type: " + type + "\tx: " + x + "\ty: " + y + "\tz: " + z);
		if (type == mSensorType && mSensorView != null)
			mSensorView.updateSensorPosition(type, x, y, z);
	}

	protected class ServerTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			if (mUDPServer != null)
				mUDPServer.stop();
			mUDPServer = null;
		}
		@Override
		protected Void doInBackground(Void... params) {
			mUDPServer = new UDPServer(MainActivity.this);
			mUDPServer.start();
			return null;
		}
	}

	void initUI() {
		LinearLayout frameView = (LinearLayout)findViewById(R.id.sensor_frame);
		mCheckBoxServer = (CheckBox)findViewById(R.id.sensor_server);
		mCheckBoxServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mEditTextServerIP.setEnabled(!isChecked);
				mSpinnerSensorType.setEnabled(isChecked);
				clearUp();
				if (isChecked) {
					mServerTask = new ServerTask();
					mServerTask.execute((Void)null);
					stopSensors();
				}
				else {
					String serverIP = mEditTextServerIP.getText().toString();
					if (mUDPClient == null && serverIP.length() > 0)
						mUDPClient = new UDPClient(serverIP);
					startSensors();
				}
			}
		});
		mEditTextServerIP = (EditText)findViewById(R.id.sensor_ip);
		mSpinnerSensorType = (Spinner)findViewById(R.id.sensor_type);
		mSpinnerSensorType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						mSensorType = Sensor.TYPE_ACCELEROMETER;
						break;
					case 1:
						mSensorType = Sensor.TYPE_GRAVITY;
						break;
					case 2:
						mSensorType = Sensor.TYPE_GYROSCOPE;
						break;
					case 3:
						mSensorType = Sensor.TYPE_LINEAR_ACCELERATION;
						break;
					case 4:
						mSensorType = Sensor.TYPE_ROTATION_VECTOR;
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		View view = findViewById(R.id.sensor_view);
		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		mSensorView = new SensorView(this);
		mSensorView.setLayoutParams(layoutParams);
		frameView.removeView(view);
		frameView.addView(mSensorView);
	}

	void initSensors() {
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager != null) {
			mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
			mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			mLinearAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		}
	}

	public void startSensors() {
		if (!sensorRegistered) {
			mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, mLinearAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
			Log.i(TAG, "Sensors started.");
			sensorRegistered = true;
		}
	}

	public void stopSensors() {
		if (sensorRegistered) {
			mSensorManager.unregisterListener(this, mAccelerometerSensor);
			mSensorManager.unregisterListener(this, mGravitySensor);
			mSensorManager.unregisterListener(this, mGyroscopeSensor);
			mSensorManager.unregisterListener(this, mLinearAccelerometerSensor);
			mSensorManager.unregisterListener(this, mRotationVectorSensor);
			Log.i(TAG, "Sensors stopped.");
			sensorRegistered = false;
		}
	}

	void clearUp() {
		if (mUDPClient != null)
			mUDPClient = null;
		if (mUDPServer != null)
			mUDPServer.stop();
		mUDPServer = null;

		if (mServerTask != null)
			mServerTask = null;

		stopSensors();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initUI();
		initSensors();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearUp();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
