package edu.cmu.ri.airboat.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import edu.cmu.ri.airboat.server.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import at.abraxas.amarino.AmarinoIntent;

public class AirboatActivity extends Activity {
	private static final String logTag = AirboatActivity.class.getName();
	
	/** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {

    	// Create the "main" layout from the included XML file 
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Register handler for Bluetooth address box
        final AutoCompleteTextView connectAddress = (AutoCompleteTextView)findViewById(R.id.ConnectAddress);
        connectAddress.setThreshold(1);
        connectAddress.addTextChangedListener(new TextWatcher() {
        	
        	public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			public void afterTextChanged(Editable s) {
				
				// Remove all ':' and invalid chars from string
				String clean = s.toString().replaceAll("[:[^[\\d[a-f][A-F]]]]", "");
				
				// Insert them back in correct locations
				StringBuffer str = new StringBuffer(clean);
				if (str.length() > 2)
					str.insert(2, ':');
				if (str.length() > 5)
					str.insert(5, ':');
				if (str.length() > 8)
					str.insert(8, ':');
				if (str.length() > 11)
					str.insert(11, ':');
				if (str.length() > 14)
					str.insert(14, ':');
				if (str.length() > 17)
					str.delete(17, str.length());
				
				// If something changed, update string
				if (!str.toString().equals(s.toString()))
					s.replace(0, s.length(), str.toString().toUpperCase());
			}
		});
        
        // Create a filter that listens to Amarino connection events
        IntentFilter amarinoFilter = new IntentFilter();
        amarinoFilter.addAction(AmarinoIntent.ACTION_CONNECTED_DEVICES);
        amarinoFilter.addAction(AmarinoIntent.ACTION_CONNECTED);
        amarinoFilter.addAction(AmarinoIntent.ACTION_DISCONNECTED);
        
		// Create a listener to update the connected devices autocomplete list
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        connectAddress.setAdapter(adapter);
        registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// Check for events indicating connection or disconnection
				if (intent.getAction().equals(AmarinoIntent.ACTION_CONNECTED)) {
					final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
					connectAddress.post(new Runnable() {
						public void run() {
							adapter.add(address);
							adapter.notifyDataSetChanged();
						}
					});
				} else if (intent.getAction().equals(AmarinoIntent.ACTION_DISCONNECTED)) {
					final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
					connectAddress.post(new Runnable() {
						public void run() {
							adapter.remove(address);
							adapter.notifyDataSetChanged();
						}
					});					
				} else if (intent.getAction().equals(AmarinoIntent.ACTION_CONNECTED_DEVICES)) {
					final String[] devices = intent.getStringArrayExtra(AmarinoIntent.EXTRA_CONNECTED_DEVICE_ADDRESSES);
					connectAddress.post(new Runnable() {
						public void run() {
							adapter.clear();
							if (devices != null)
								for (String device : devices) 
									adapter.add(device);
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
		}, amarinoFilter);
		sendBroadcast(new Intent(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES));
        
        // Register handler for toggle button
        final ToggleButton connectToggle = (ToggleButton)findViewById(R.id.ConnectToggle);
        connectToggle.setChecked(AirboatService.isRunning); // Hack to determine initial service state
        connectToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	
    		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    			Intent intent = new Intent(AirboatActivity.this, AirboatService.class);
    			intent.putExtra(AirboatService.BD_ADDR, connectAddress.getText().toString());
    			
    			if (isChecked) {
    				Log.i(logTag, "Starting background service.");
    				startService(intent);
    			} else {
    				Log.i(logTag, "Stopping background service.");
    				stopService(intent);
    			}
    		}
    	});
        
        // Register handler for debug button
        final Button debugToggle = (Button)findViewById(R.id.DebugButton);
        debugToggle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Start up the debug control activity
				startActivity(new Intent(AirboatActivity.this, AirboatControlActivity.class));
			}
		});
        
        // Register listener for log events to this entire package
/*        final ListView logText = (ListView)findViewById(R.id.LogList);
        final ArrayAdapter<String> logAdapter = new ArrayAdapter<String>(this, R.layout.row);
        logText.setAdapter(logAdapter);
        
        // Set up java logger to send log events to the visible log list                   //remove this
        final Logger pkgLogger = Logger.getLogger(AirboatActivity.class.getPackage().getName());
        pkgLogger.addHandler(new Handler() {
			
			public void publish(LogRecord record) {
				String className = record.getSourceClassName();
				int lastClass = record.getSourceClassName().lastIndexOf('.');
				final String entry = (lastClass < 0 ? className : className.substring(lastClass + 1)) + ":  " + record.getMessage() + ((record.getThrown() == null ? "" : (" : " + record.getThrown())));
				
				logText.post(new Runnable() {
					
					public void run() {
						// TODO: replace this with a more efficient list update
						if (logAdapter.getCount() > LOG_HISTORY_LENGTH) {
							logAdapter.remove(logAdapter.getItem(0));
						}
						
						// Print out the simple calling class name followed by the message (i.e. "LinkedList: Added element.")
						logAdapter.add(entry);
						logAdapter.notifyDataSetChanged();
					}
				});
			}
			
			public void flush() {
				logText.post(new Runnable() {
					
					public void run() {
						logAdapter.notifyDataSetChanged();
						logText.postInvalidate();
					}
				});
			}
			
			public void close() {
				logText.post(new Runnable() {
					
					public void run() {
						logAdapter.clear();
						logAdapter.notifyDataSetChanged();
					}
				});
			}
		});
        */
        // Display current IP address
        final TextView addrText = (TextView)findViewById(R.id.IpAddressText);
        addrText.setText(getLocalIpAddress());
    }
    
    /**
     * Helper function that retrieves first valid (non-loopback) IP address
     * over all available interfaces.
     * 
     * @return Text representation of current local IP address.
     */
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(logTag, "Failed to get local IP.", ex);
		}
		return null;
	}

}
