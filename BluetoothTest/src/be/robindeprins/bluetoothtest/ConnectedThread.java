package be.robindeprins.bluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;


/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
class ConnectedThread extends Thread {
    private BluetoothSocket socket;
    private InputStream inStream;
    private OutputStream outStream;
    private MainActivity main;
    
    private static final String TAG = "ConnectedThread";

    public ConnectedThread(MainActivity main, BluetoothSocket socket) {
        Log.d(TAG, "create ConnectedThread");
        this.socket = socket;
        this.main = main;

        // Get the BluetoothSocket input and output streams
        try {
        	inStream = socket.getInputStream();
        	outStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "sockets not created", e);
        }
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = inStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                synchronized (main) {
                	// Start the connected thread
                	main.receiveMessage(buffer, bytes);
                }
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                synchronized (main) {
                	// Start the connected thread
                	main.connectionLost();
                }
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     * @param buffer  The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            outStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}