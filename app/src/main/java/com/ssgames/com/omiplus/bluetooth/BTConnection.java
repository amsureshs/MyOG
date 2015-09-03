package com.ssgames.com.omiplus.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BTConnection {

	private BluetoothSocket btSocket;
	private BTConnectedThread btConnectedThread;
	private BTConnectThread btConnectThread;

	public BluetoothSocket getBtSocket() {
		return btSocket;
	}

	public void setBtSocket(BluetoothSocket btSocket) {
		this.btSocket = btSocket;
	}

	public BTConnectedThread getBtConnectedThread() {
		return btConnectedThread;
	}

	public void setBtConnectedThread(BTConnectedThread btConnectedThread) {
		this.btConnectedThread = btConnectedThread;
	}

	public BTConnectThread getBtConnectThread() {
		return btConnectThread;
	}

	public void setBtConnectThread(BTConnectThread btConnectThread) {
		this.btConnectThread = btConnectThread;
	}

	public BluetoothDevice getBtDevice() {
		if (btSocket == null) {
			return null;
		}
		return btSocket.getRemoteDevice();
	}
	
	public String getName() {
		if(getBtDevice() != null)
			return getBtDevice().getName();
		
		return "";
	}
	
	public void disconnect() {
		btConnectedThread.cancel();
	}
}
