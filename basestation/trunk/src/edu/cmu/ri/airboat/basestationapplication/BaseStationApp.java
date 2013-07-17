package edu.cmu.ri.airboat.basestationapplication;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.cmu.ri.airboat.basestationapplication.BaseStationApp;
import edu.cmu.ri.airboat.basestationapplication.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * This class gets the weather information from Google, displays the information and checks if the 
 * conditions are suitable for performing a test. It also gets the battery information of the phone.
 * If the weather and battery conditions are suitable, then this initiates the task for the boat to perform a test.
 * 
 * @author Syed Ali Hashim Moosavi
 * 
 */

public class BaseStationApp extends Activity {
	
	//Getting information from Google Weather and setting values for them

	class ForecastInformation{
		String city;
		String postal_code;
		String forecast_date;
		String current_date_time;
		String unit_system;
	}

	//The attributes of this class will be used to check whether the conditions are suitable for testing or not
	class CurrentConditions{
		String condition;
		String temp_f;
		String temp_c;
		String humidity;
		String icon;
		String wind_condition;
	}

	//Attributes for displaying the weather forecast
	class ForecastConditions{
		String day_of_week;
		String low;
		String high;
		String icon;
		String condition;
	}

	ForecastInformation forecastInformation;
	CurrentConditions currentConditions;
	List<ForecastConditions> forecastConditionsList;

	Button buttonEnter;
	EditText edittextPlace;
	ImageView iconCurrent;
	TextView textCurrent;
	TextView textInfo;
	ListView listForcast;
	
	//Battery level of the phone
	private int androidBatteryLevel = -1;

	public int getAndroidBatteryLevel() {
		return androidBatteryLevel;
	}

	public void setAndroidBatteryLevel(int androidBatteryLevel) {
		this.androidBatteryLevel = androidBatteryLevel;
	}


	/** Called when the activity is created initially */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Disable strict-mode (TODO: remove this and use handlers)
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		buttonEnter = (Button)findViewById(R.id.enter);
		edittextPlace = (EditText)findViewById(R.id.place);
		iconCurrent = (ImageView)findViewById(R.id.iconcurrent);
		textCurrent = (TextView)findViewById(R.id.textcurrent);
		textInfo = (TextView)findViewById(R.id.textinfo);
		listForcast = (ListView)findViewById(R.id.listforcast);

		buttonEnter.setOnClickListener(EnterOnClickListener);

		//This command can be used to set the battery level at any time
		this.registerReceiver(this.mBatInfoReceiver, 
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	Button.OnClickListener EnterOnClickListener
	= new Button.OnClickListener(){

		public void onClick(View arg0) {
			// TODO: Auto-generated method stub
			String place = edittextPlace.getText().toString();

			String weatherString = QueryGoogleWeather(place);
			Document weatherDoc = convertStringToDocument(weatherString);

			if(parseGoogleWeather(weatherDoc)){
				//Display Weather info
				String c = currentConditions.condition + "\n"
						+ currentConditions.temp_f + "f\n"
						+ currentConditions.temp_c + "c\n"
						+ currentConditions.humidity + "\n"
						+ currentConditions.wind_condition + "\n";

				textCurrent.setText(c);
				Bitmap bm = LoadIcon(currentConditions.icon);
				iconCurrent.setImageBitmap(bm);

				textInfo.setText("City: " + forecastInformation.city + "\n"
						+ "Postal Code: " + forecastInformation.postal_code + "\n"
						+ "Forecast Date: " + forecastInformation.forecast_date + "\n"
						+ "Current date and time: " + forecastInformation.current_date_time + "\n"
						);

				listForcast.setAdapter(new MyCustomAdapter(
						BaseStationApp.this,
						R.layout.row, 
						forecastConditionsList));
			}		
		}
	};

    public class MyCustomAdapter extends ArrayAdapter<ForecastConditions> {

		public MyCustomAdapter(Context context, int textViewResourceId,
				List<ForecastConditions> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater=getLayoutInflater();
			View row=inflater.inflate(R.layout.row, parent, false);
			ImageView iconForecast = (ImageView)row.findViewById(R.id.iforecast);
			TextView textForecast = (TextView)row.findViewById(R.id.tforecast);

			//Displays the forecast conditions
			textForecast.setText(
					forecastConditionsList.get(position).day_of_week + "\n"
							+ " - " + forecastConditionsList.get(position).condition + "\n"
							+ forecastConditionsList.get(position).low + " ~ "
							+ forecastConditionsList.get(position).high);

			Bitmap bm = LoadIcon(forecastConditionsList.get(position).icon);
			iconForecast.setImageBitmap(bm);

			return row;
		}

	}

	private Bitmap LoadIcon(String iconURL)
	{       
		BitmapFactory.Options bmOptions;
		bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;
		String image_URL = "http://www.google.com" + iconURL;

		Bitmap bitmap = null;
		InputStream in = null;       
		try {
			in = OpenHttpConnection(image_URL);
			bitmap = BitmapFactory.decodeStream(in, null, bmOptions);
			in.close();	
		} catch (IOException e1) {
		}
		return bitmap;   	
	}

	private InputStream OpenHttpConnection(String strURL) throws IOException{
		InputStream inputStream = null;
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();

		try{
			HttpURLConnection httpConn = (HttpURLConnection)conn;
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpConn.getInputStream();	
			}	
		}catch (Exception ex){
		}
		return inputStream;	
	}

	//This method parses the Google Weather information to set the values of Current and Forecast weather Information
	private boolean parseGoogleWeather(Document srcDoc){

		boolean result = false;

		forecastInformation = new ForecastInformation();
		currentConditions = new CurrentConditions();

		//-- Get forecast_information
		NodeList forecast_information = srcDoc.getElementsByTagName("forecast_information");
		if (forecast_information.getLength() > 0){

			//Assume place found if "forecast_information" exist
			result = true;

			NodeList infoChilds = forecast_information.item(0).getChildNodes();    	

			for(int i=0; i<infoChilds.getLength(); i++){
				Node n = infoChilds.item(i);

				String nName = n.getNodeName();
				String nValue 
				= n.getAttributes().getNamedItem("data").getNodeValue().toString();
				if (nName.equalsIgnoreCase("city")){
					forecastInformation.city = nValue;
				}
				else if((nName.equalsIgnoreCase("postal_code"))){
					forecastInformation.postal_code = nValue;
				}
				else if((nName.equalsIgnoreCase("forecast_date"))){
					forecastInformation.forecast_date = nValue;
				}
				else if((nName.equalsIgnoreCase("current_date_time"))){
					forecastInformation.current_date_time = nValue;
				}
				else if((nName.equalsIgnoreCase("unit_system"))){
					forecastInformation.unit_system = nValue;
				}
			}
		}

		//Get current_conditions
		NodeList current_conditions = srcDoc.getElementsByTagName("current_conditions");
		if(current_conditions.getLength()>0){
			NodeList currentChilds = current_conditions.item(0).getChildNodes();

			for(int i=0; i<currentChilds.getLength(); i++){
				Node n = currentChilds.item(i);

				String nName = n.getNodeName();
				String nValue 
				= n.getAttributes().getNamedItem("data").getNodeValue().toString();
				if (nName.equalsIgnoreCase("condition")){
					currentConditions.condition = nValue;
				}
				else if((nName.equalsIgnoreCase("temp_f"))){
					currentConditions.temp_f = nValue;
				}
				else if((nName.equalsIgnoreCase("temp_c"))){
					currentConditions.temp_c = nValue;
				}
				else if((nName.equalsIgnoreCase("humidity"))){
					currentConditions.humidity = nValue;
				}
				else if((nName.equalsIgnoreCase("icon"))){
					currentConditions.icon = nValue;
				}
				else if((nName.equalsIgnoreCase("wind_condition"))){
					currentConditions.wind_condition = nValue;
				}
			}
		}

		//-- Get forecast_conditions
		NodeList forecast_conditions = srcDoc.getElementsByTagName("forecast_conditions");
		if (forecast_conditions.getLength()>0){
			int forecast_conditions_length = forecast_conditions.getLength();

			forecastConditionsList = new ArrayList<ForecastConditions>();

			for(int j=0; j<forecast_conditions_length; j++){

				ForecastConditions tmpForecastConditions = new ForecastConditions();

				NodeList forecasrChilds = forecast_conditions.item(j).getChildNodes();

				for(int i=0; i<forecasrChilds.getLength(); i++){

					Node n = forecasrChilds.item(i);

					String nName = n.getNodeName();
					String nValue 
					= n.getAttributes().getNamedItem("data").getNodeValue().toString();

					if (nName.equalsIgnoreCase("condition")){
						tmpForecastConditions.condition = nValue;
					}else if((nName.equalsIgnoreCase("day_of_week"))){
						tmpForecastConditions.day_of_week = nValue;
					}else if((nName.equalsIgnoreCase("low"))){
						tmpForecastConditions.low = nValue;
					}else if((nName.equalsIgnoreCase("high"))){
						tmpForecastConditions.high = nValue;
					}else if((nName.equalsIgnoreCase("icon"))){
						tmpForecastConditions.icon = nValue;
					}
				}
				forecastConditionsList.add(tmpForecastConditions);
			}
		}

		return result;
	}

	private Document convertStringToDocument(String src){
		Document dest = null;

		DocumentBuilderFactory dbFactory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			Toast.makeText(BaseStationApp.this, 
					e1.toString(), Toast.LENGTH_LONG).show();
		} catch (SAXException e) {
			e.printStackTrace();
			Toast.makeText(BaseStationApp.this, 
					e.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(BaseStationApp.this, 
					e.toString(), Toast.LENGTH_LONG).show();
		}

		return dest;
	}

	//Gets the weather information from Google Weather
	private String QueryGoogleWeather(String p){

		String uriPlace = Uri.encode(p);

		String qResult = "";
		String queryString = "http://www.google.com/ig/api?hl=en&weather=" + uriPlace;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(queryString);

		try {
			HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

			if (httpEntity != null){
				InputStream inputStream = httpEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();

				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");	
				}

				qResult = stringBuilder.toString();	
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Toast.makeText(BaseStationApp.this, 
					e.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(BaseStationApp.this, 
					e.toString(), Toast.LENGTH_LONG).show();
		}

		return qResult;
	}

	/*
	 * After the weather info is obtained and displayed, we check for battery from the Arduino and 
	 * the Base station and then trigger the boat to perform a test.
	 * 
     /*
	 * We can have three parameters of checking the battery. Checking phone's battery, Arduino's battery and 
	 * the battery left on the  base station. How these parameters are used is to be decided.
	 */

	//This function sets the battery level of the phone 
	public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			if (rawlevel >= 0 && scale > 0) {
				setAndroidBatteryLevel((rawlevel * 100) / scale);
			}
		}
	};
	
	/* 
	 * This command can be used to set the battery level of the phone at any time
		this.registerReceiver(this.mBatInfoReceiver, 
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	*/
	
	//TODO:Functions to get battery level of Arduino and Base Station

	/* 
	 * Example:
	 * If currentConditions.condition != OUTCAST and androidBatteryLevel > 50 then
	 * Perform Lawnmover test
	 */

}
