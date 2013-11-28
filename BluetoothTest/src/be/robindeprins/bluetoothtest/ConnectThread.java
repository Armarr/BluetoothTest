package be.robindeprins.bluetoothtest;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
class ConnectThread extends Thread {
    // Unique UUID for this application
    private static final UUID MY_UUID =
        UUID.fromString("69112490-5861-11e3-949a-0800200c9a66");
    private static final String TAG = "ConnectThread";

	
    private BluetoothSocket socket;
    private BluetoothAdapter adapter;
    private MainActivity main;

    public ConnectThread(MainActivity main, BluetoothAdapter adapter, BluetoothDevice device) {
        this.adapter = adapter;
        this.main = main;
        
        BluetoothSocket tmp = null;

        try {
            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket create() failed", e);
        }
        socket = tmp;
        run();
    }

    public void run() {
        Log.i(TAG, "BEGIN connectThread");
        setName("ConnectThread");

        // Always cancel discovery because it will slow down a connection
        adapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            socket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                socket.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to close() socket during connection failure", e2);
            }
            Log.e(TAG, "Could not connect", e);
            return;
        }

        // Reset the ConnectThread because we're done
        synchronized (main) {
        	// Start the connected thread
        	main.connected(socket);
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
