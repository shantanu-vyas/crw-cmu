package edu.cmu.ri.airboat.server;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
	
	public static final String PREFS_PRIVATE = "PREFS_PRIVATE";
	public static final String KEY_MASTER_URI = "KEY_MASTER_URI";
	public static final String KEY_BT_ADDR = "KEY_BT_ADDR";

	private BroadcastReceiver _amarinoReceiver;
	
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
        _amarinoReceiver = new BroadcastReceiver() {
			
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
		};
		registerReceiver(_amarinoReceiver, amarinoFilter);
		sendBroadcast(new Intent(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES));
        
		// Register handler for URI master that changes the color of the URI
		// if a valid ROS core seems to be reached.
		// TODO: Move this to its own class!
		final AutoCompleteTextView masterAddress = (AutoCompleteTextView)findViewById(R.id.MasterAddress);
		masterAddress.addTextChangedListener(new TextWatcher() {
			
			final Handler handler = new Handler();
			final AtomicBoolean _isUpdating = new AtomicBoolean(false);
			final AtomicBoolean _isUpdated = new AtomicBoolean(false);
			
			final class TextUpdate extends AsyncTask<Void, Void, Integer> {
				
				@Override
				protected Integer doInBackground(Void... urls) {
					int textBkgnd = 0xFFFFCCCC;
					
					_isUpdated.set(true);
					_isUpdating.set(true);
					
					try {
						// Try to open the URI in the text box, if it succeeds, make 
						// the box change color accordingly
				        URL url = new URL(masterAddress.getText().toString());
				        if (InetAddress.getByName(url.getHost()).isReachable(300)) {
				        
					        HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
					        if (urlConn != null) {
					        	urlConn.setConnectTimeout(300);
					        	urlConn.setReadTimeout(300);
						        urlConn.connect();
						        if (urlConn.getResponseCode() == HttpURLConnection.HTTP_NOT_IMPLEMENTED)
						        	textBkgnd = 0xFFCCFFCC;
						        urlConn.disconnect();
					        }
				        }
				    } catch (Exception e) {}
				    
				    return textBkgnd;
				}

				@Override
				protected void onPostExecute(Integer result) {
					masterAddress.setBackgroundColor(result);

					// Immediately reschedule if out of date, otherwise delay
				    if (!_isUpdated.get()) {
				    	new TextUpdate().execute((Void[])null);
				    } else {
				    	handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								new TextUpdate().execute((Void[])null);
							}
						}, 2000);
				    }
				    
				    // In any case, we are now done updating
				    _isUpdating.set(false);
				}
			};
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(final Editable s) {
				
				_isUpdated.set(false);
				
				// If an update isn't already running, start one up
				if (!_isUpdating.get()) {
					new TextUpdate().execute((Void[])null);
				}
			}
		});
		
		
        // Register handler for toggle button
        final ToggleButton connectToggle = (ToggleButton)findViewById(R.id.ConnectToggle);
        connectToggle.setChecked(AirboatService.isRunning); // Hack to determine initial service state
        connectToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	
    		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    			Intent intent = new Intent(AirboatActivity.this, AirboatService.class);
    			intent.putExtra(AirboatService.BD_ADDR, connectAddress.getText().toString());
    			intent.putExtra(AirboatService.ROS_MASTER_URI, masterAddress.getText().toString());
    			
				// Save the current BD addr and master URI
				SharedPreferences prefs = getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
				Editor prefsPrivateEditor = prefs.edit();
				prefsPrivateEditor.putString(KEY_BT_ADDR, connectAddress.getText().toString());
				prefsPrivateEditor.putString(KEY_MASTER_URI, masterAddress.getText().toString());
				prefsPrivateEditor.commit();
    			
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
        
        // Set text boxes to previous values
        SharedPreferences prefs = getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
        connectAddress.setText(prefs.getString(KEY_BT_ADDR, connectAddress.getText().toString()));
        masterAddress.setText(prefs.getString(KEY_MASTER_URI, masterAddress.getText().toString()));
    }
    
    @Override
	public void onDestroy() {
    	
    	super.onDestroy();
    	unregisterReceiver(_amarinoReceiver);
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
