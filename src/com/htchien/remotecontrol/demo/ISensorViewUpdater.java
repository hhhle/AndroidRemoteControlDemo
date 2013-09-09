package com.htchien.remotecontrol.demo;

import android.hardware.SensorEvent;

/**
 * Created by tedchien on 13/9/6.
 */
public interface ISensorViewUpdater {
	public void updateView(int sensorType, float x, float y, float z);
}
