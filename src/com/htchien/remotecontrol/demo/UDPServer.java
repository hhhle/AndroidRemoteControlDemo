package com.htchien.remotecontrol.demo;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by tedchien on 13/8/30.
 */
public class UDPServer {
	static final String TAG = UDPServer.class.getSimpleName();

	static final int SENSOR_DATA    = 100;
	static final int SENSOR_TYPE    = SENSOR_DATA + 1;
	static final int SENSOR_VALUE   = SENSOR_DATA + 2;

	byte[] receiveData = new byte[1024];
	int serverPort = 9876;
	boolean isStop = false;
	ISensorViewUpdater mSensorViewUpdater;

	public UDPServer(ISensorViewUpdater sensorViewUpdater) {
		mSensorViewUpdater = sensorViewUpdater;
	}

	public void start() {
		Log.i(TAG, "start");
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(serverPort);
			serverSocket.setReuseAddress(true);
			InetAddress serverIPAddress = InetAddress.getLocalHost();
			Log.d(TAG, "Server address: " + serverIPAddress.getHostAddress());

			isStop = false;
			while(!isStop) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String data = new String( receivePacket.getData(), 0, receivePacket.getLength());
				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();
				System.out.println("RECEIVED: " + data + " from " + IPAddress.getHostAddress() + ":" + port);
				process(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null)
				serverSocket.close();
		}
	}

	public void stop() {
		Log.i(TAG, "stop");
		isStop = true;
	}

	protected void process(String data) {
		Log.d(TAG, "process: " + data);
		int type = -1;
		boolean fOK = true;

		try {
			String[] values = data.split("\t");
			float[] fValues = new float[(values.length / 2) - 1];

			if (SENSOR_TYPE == Integer.parseInt(values[0]))
				type = Integer.parseInt(values[1]);
			else
				fOK = false;

			for (int i = 2; i < values.length; i += 2) {
				if (SENSOR_VALUE == Integer.parseInt(values[i]))
					fValues[(i - 2)/2] = Float.parseFloat(values[i+1]);
				else
					fOK = false;
			}

			if (fOK && mSensorViewUpdater != null)
				mSensorViewUpdater.updateView(type, fValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}