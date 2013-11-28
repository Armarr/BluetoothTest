package be.robindeprins.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// Intent request codes
    public static final int REQUEST_DISCOVERABLE_BT = 0;
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    
    public static final String TAG = "MainActivity";
    
	private Button connectButton;
	private Button sendButton;
	private EditText editText;
	private TextView textView;
	
    private BluetoothAdapter bluetoothAdapter;
   
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        connectButton = (Button) findViewById(R.id.button1);
        sendButton = (Button) findViewById(R.id.button2);
        editText = (EditText) findViewById(R.id.editText1);
        textView = (TextView) findViewById(R.id.textView1);
        
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    if (bluetoothAdapter == null) {
	    	Toast.makeText(getApplicationContext(), "No bluetooth adapter found, device not supported.", Toast.LENGTH_LONG).show();
	    	finish();
            return;
	    }
    	connectButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	           public void onClick(View arg0) {
	        		connectButtonClicked();
	           }
	       });
    	
    	sendButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	           public void onClick(View arg0) {
	        		sendButtonClicked();
	           }
	       });
	}
	
	private void connectButtonClicked(){
	     Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	     startActivityForResult(enableBtIntent, REQUEST_DISCOVERABLE_BT);
	}
	
	private void sendButtonClicked(){
		if(connectedThread != null){
			connectedThread.write(editText.getText().toString().getBytes());
		}
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_DISCOVERABLE_BT:
            if (resultCode != Activity.RESULT_CANCELED) {
       	     	// Launch the DeviceListActivity to see devices and do scan
       	     	Intent serverIntent = new Intent(this, DeviceListActivity.class);
       	     	startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
            break;
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data);
            }
            break;
        }
    }
    
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = null;
        try{
        	device = bluetoothAdapter.getRemoteDevice(address);
        }catch(IllegalArgumentException e){
        	Log.v(TAG, "Unknown device: " + address);
        }
        if(device != null){
        	// Attempt to connect to the device
            if(connectThread != null){
            	connectThread.cancel();
            }
            connectThread = new ConnectThread(this, bluetoothAdapter, device);
        }
    }
    
	public void connected(BluetoothSocket socket) {
    	Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
		connectThread = null;
		
		if(connectedThread != null){
			connectedThread.cancel();
		}
		connectedThread = new ConnectedThread(this, socket);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void receiveMessage(byte[] buffer, int bytes) {
		String message = new String(buffer, 0, bytes);
		textView.setText(message);
	}

	public void connectionLost() {
		connectedThread = null;
    	Toast.makeText(getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show();
	}

}
