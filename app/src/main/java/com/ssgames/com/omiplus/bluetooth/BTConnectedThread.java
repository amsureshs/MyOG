package com.ssgames.com.omiplus.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BTConnectedThread extends Thread {

    private static final String TAG = BTConnectedThread.class.getSimpleName();

	private final BluetoothSocket btSocket;
	private final InputStream socInStream;
	private final OutputStream socOutStream;
	private final BTConnectedThreadListener ctListener;

	public BTConnectedThread(BluetoothSocket socket,
			BTConnectedThreadListener listener) {
		btSocket = socket;
		ctListener = listener;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		try {
			tmpIn = btSocket.getInputStream();
			tmpOut = btSocket.getOutputStream();
		} catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "Error during get input and out streams");
		}

		socInStream = tmpIn;
		socOutStream = tmpOut;
	}

	public void run() {
		byte[] buffer = new byte[1024];
		int bytes;

		// Keep listening to the InputStream while connected
		while (true) {
			try {
				// Read from the InputStream
				bytes = socInStream.read(buffer);
				if (bytes == -1) {
					ctListener.btConnectedNewDataReceived(btSocket, buffer);
				}
			} catch (IOException e) {
                Log.v(TAG, "Error during reading data");
                e.printStackTrace();
				ctListener.btConnectedNewDataReadFailed(btSocket);
				break;
			}
		}
	}

	public void write(byte[] buffer) {
		try {
			socOutStream.write(buffer);
			ctListener.btConnectedDataWriteSucceeded(btSocket);
		} catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "Error during writing data");
			ctListener.btConnectedDataWriteFailed(btSocket, buffer);
		}
	}

	public void cancel() {
		try {
			btSocket.close();
		} catch (IOException e) {
			Log.v(TAG, "Socket closing error");
		}
	}

}
