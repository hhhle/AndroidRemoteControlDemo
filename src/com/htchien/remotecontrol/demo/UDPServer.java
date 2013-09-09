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
	static final int SENSOR_X       = SENSOR_DATA + 2;
	static final int SENSOR_Y       = SENSOR_DATA + 3;
	static final int SENSOR_Z       = SENSOR_DATA + 4;

	byte[] receiveData = new byte[1024];
	int serverPort = 9876;
	boolean isStop = false;
	ISensorViewUpdater mSensorViewUpdater;

	public UDPServer(ISensorViewUpdater sensorViewUpdater) {
		mSensorViewUpdater = sensorViewUpdater;
	}

	public void start()
	{
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
		isStop = true;
	}

	protected void process(String data) {
		int type = -1;
		float x = 0f, y = 0f, z = 0f;
		boolean fOK = true;

		try {
			String[] values = data.split("\t");

			if (SENSOR_TYPE == Integer.parseInt(values[0]))
				type = Integer.parseInt(values[1]);
			else
				fOK = false;

			if (SENSOR_X == Integer.parseInt(values[2]))
				x = Float.parseFloat(values[3]);
			else
				fOK = false;

			if (SENSOR_Y == Integer.parseInt(values[4]))
				y = Float.parseFloat(values[5]);
			else
				fOK = false;

			if (SENSOR_Z == Integer.parseInt(values[6]))
				z = Float.parseFloat(values[7]);
			else
				fOK = false;

			if (fOK && mSensorViewUpdater != null)
				mSensorViewUpdater.updateView(type, x, y, z);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}