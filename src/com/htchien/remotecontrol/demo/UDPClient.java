package com.htchien.remotecontrol.demo;

import android.hardware.SensorEvent;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by tedchien on 13/8/30.
 */
public class UDPClient {
	final String TAG = UDPClient.class.getSimpleName();

	String serverIP;
	int serverPort;

	UDPClient(String serverIP) {
		this.serverIP = serverIP;
		this.serverPort = 9876;
	}

	public void send(String data) {
		if (serverIP.length() <= 0)
			return;

		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(serverIP);
			byte[] sendData = data.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(SensorEvent sensorEvent) {
		String dataToSend = String.format("%d\t%d",
			UDPServer.SENSOR_TYPE, sensorEvent.sensor.getType());

		for (int i = 0; i < sensorEvent.values.length; i++)
			dataToSend += String.format("\t%d\t%s", UDPServer.SENSOR_VALUE, sensorEvent.values[i]);

		Log.d(TAG, "Sending data: " + dataToSend + " to " + serverIP + ":" + serverPort);
		send(dataToSend);
	}
}
