package com.htchien.remotecontrol.demo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class MainActivity extends Activity implements SensorEventListener, ISensorViewUpdater {
	final String TAG = MainActivity.class.getSimpleName();

	LinearLayout mFrameView;
	CheckBox mCheckBoxServer;
	EditText mEditTextServerIP;
	Button mButtonConnect;
	Spinner mSpinnerSensorType;
	int mSensorType = Sensor.TYPE_ACCELEROMETER;
	SensorView mSensorView;
	SensorRenderer mSensorRenderer;
	GLSurfaceView mGLSurfaceView;

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

	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock;

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
		Log.d(TAG, "onAccuracyChanged: " + sensor + ":" + accuracy);
	}

	@Override
	public void updateView(int type, float[] values) {
		Log.d(TAG, "updateView - type: " + type + "\tvalues: " + values);
		if (type == mSensorType && mSensorView != null) {
			if (mSensorType != Sensor.TYPE_ROTATION_VECTOR)
				mSensorView.updateSensorPosition(values);
			else {
				mSensorRenderer.updateRenderer(values);
			}
		}
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
		mFrameView = (LinearLayout)findViewById(R.id.sensor_frame);
		mCheckBoxServer = (CheckBox)findViewById(R.id.sensor_server);
		mCheckBoxServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mEditTextServerIP.setEnabled(!isChecked);
				mButtonConnect.setEnabled(!isChecked);
				clearUp();
				if (isChecked) {
					mServerTask = new ServerTask();
					mServerTask.execute((Void)null);
					stopSensors();
				}
			}
		});
		mEditTextServerIP = (EditText)findViewById(R.id.sensor_ip);
		mButtonConnect = (Button)findViewById(R.id.sensor_connect);
		mButtonConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String serverIP = mEditTextServerIP.getText().toString();
				if (mUDPClient == null && serverIP.length() > 0)
					mUDPClient = new UDPClient(serverIP);
				startSensors();
			}
		});
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

				if (mSensorType == Sensor.TYPE_ROTATION_VECTOR) {
					if (mSensorView.getParent() == mFrameView) {
						mFrameView.removeView(mSensorView);
						mFrameView.addView(mGLSurfaceView);
					}
				}
				else {
					if (mGLSurfaceView.getParent() == mFrameView) {
						mFrameView.removeView(mGLSurfaceView);
						mFrameView.addView(mSensorView);
					}
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
		mFrameView.removeView(view);
		mFrameView.addView(mSensorView);

		mSensorRenderer = new SensorRenderer();
		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setLayoutParams(layoutParams);
		mGLSurfaceView.setRenderer(mSensorRenderer);

	}

	void initSensors() {
		Log.i(TAG, "initSensors");
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
		Log.i(TAG, "clearUp");
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
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get an instance of the PowerManager
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		// Create a bright wake lock
		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

		initUI();
		initSensors();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
		mWakeLock.acquire();
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();

		// and release our wake-lock
		mWakeLock.release();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		clearUp();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOpetionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
