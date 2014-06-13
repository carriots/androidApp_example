package com.example.carriotsconnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class SendStream extends Activity {
	private int sent=0;
	private String device, jsonArray, apikey;
	private ArrayList<String> atList=new ArrayList<String>();
	private ArrayList<String> dataList=new ArrayList<String>();
	private boolean gpsChecked, hasLocation, accurateLocation;
	private double lat,lon;
	private LocationRequest myLocationRequest;
	private LocationManager locationManager;
	private LocationListener mlocListener;
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_INTERVAL =1000;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_stream);
		
		//disable the text editing ability of the body edit text box
		EditText body=(EditText) findViewById(R.id.editTextBody);
		body.setKeyListener(null);
		//get the parameters from the previous activity (device and json array)
		Bundle extras=getIntent().getExtras();
		device=extras.getString("device");		
		jsonArray=extras.getString("jsonArray");
		//remove the brackets [ ] from the jsonArray
		jsonArray=jsonArray.substring(1, jsonArray.length() - 1);
		//set the edit text as the value of the json parameter
		body.setText(jsonArray);
		//hide the table that will show the 10 previous streams sent and the label above it
      	TableLayout data=(TableLayout) findViewById(R.id.dataTable);
      	data.setVisibility(TableLayout.GONE);
      	//add an onClickListener to the checkbox to send GPS location with the stream
      	CheckBox gpsBox = (CheckBox) findViewById (R.id.checkGPS);
      	gpsBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            	if(isChecked){
            		gpsChecked=true;
            	}else{
            		gpsChecked=false;
            	}
            }
        }); 
	}
	
	public void sendRequest(View v){
		//on the click of the button get the apikey from shared preferences
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		apikey = sharedPreferences.getString("apikey", "notFound");
		//see if the checkbox to send the gps location is checked
		if(gpsChecked){
	        connect();
		}else{
			//create a new thread to execute the http post request in the background
			new sendStreamTask().execute();
		}
	}
	
	private void createButton(View v){
		//get the layout
		RelativeLayout relative=(RelativeLayout) findViewById(R.id.relative);
		//create a button and set its text 
		Button btnReturn=new Button(v.getContext());
		btnReturn.setText(R.string.btnReturn);
		btnReturn.setId(1);
		//use layout parameters to position the button
		RelativeLayout.LayoutParams paramsReturn = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		paramsReturn.addRule(RelativeLayout.LEFT_OF, R.id.btnSend);
		paramsReturn.addRule(RelativeLayout.ALIGN_BASELINE, R.id.btnSend);
		//add the button to the layout
        relative.addView(btnReturn, paramsReturn);
        //establish an onclicklistener that will go to the "Device" class 
        btnReturn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent intent=new Intent();
				intent.setClass(getApplicationContext(),Device.class);
				startActivity(intent);
			}
        });
        
        //change the text of the send button
        Button btnSend=(Button)findViewById(R.id.btnSend);
        btnSend.setText(R.string.btnSendAgain);
        
        //add maps button
      	Button btnMaps=new Button(v.getContext());
      	btnMaps.setText(R.string.btnMap);
      	btnMaps.setId(2);
      	//use layout parameters to position the button
      	RelativeLayout.LayoutParams paramsMap = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
      	paramsMap.addRule(RelativeLayout.BELOW, R.id.dataTable);
      	paramsMap.addRule(RelativeLayout.ALIGN_RIGHT, R.id.dataTable);
      	//add the button to the layout
        relative.addView(btnMaps, paramsMap);
        //establish an onclicklistener that will go to the "Device" class 
        btnMaps.setOnClickListener(new OnClickListener(){
      		public void onClick(View arg0) {
      			Intent intent=new Intent();
      			intent.putExtra("device", device);
      			intent.setClass(getApplicationContext(),StreamMap.class);
      			startActivity(intent);
      		}
      	});
	}
	
	private void createTable(){
		//add the values of the table that shows the previous 10 streams sent
    	TableLayout dataTable=(TableLayout) findViewById(R.id.dataTable);
		String id, id2, rowId;
		int resID, resID2, resRowID;
		//if the has more than 10 elements remove those elements greater than 10
		if(atList.size()>10){
			for(int i=10; i<atList.size(); i++){
				atList.remove(i);
				dataList.remove(i);
			}
		}
	    for(int i=0; i<atList.size(); i++){
	    	long unixSeconds = Integer.parseInt(atList.get(i));
			Date date = new Date(unixSeconds*1000L);
			TimeZone zone=TimeZone.getDefault();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			sdf.setTimeZone(zone);
			id="r"+(i+1)+"t1";
			resID = getResources().getIdentifier(id, "id", getPackageName());
			TextView t1=(TextView) findViewById(resID);
			t1.setText(sdf.format(date).toString());
			
			id2="r"+(i+1)+"t2";
			resID2 = getResources().getIdentifier(id2, "id", getPackageName());
			TextView t2=(TextView) findViewById(resID2);
			t2.setText(dataList.get(i));

			rowId="tableRow"+(i+1);
			resRowID = getResources().getIdentifier(rowId, "id", getPackageName());
			TableRow row=(TableRow) findViewById(resRowID);
			if(i%2==0){
				row.setBackgroundResource(R.drawable.cell_shape);
			}else{
				row.setBackgroundResource(R.drawable.cell_shape_odd);
			}
	    }
	    TextView previous=(TextView) findViewById(R.id.previousStreamLabel);
      	previous.setText(getString(R.string.previousStreamLabel)+" "+device);
	    dataTable.setVisibility(TableLayout.VISIBLE);
	}
	
	protected void connect(){
    	boolean noCxn=false;
    	//check that Google Play Services available
    	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	   	 if(status!=ConnectionResult.SUCCESS){ 
	   		 int requestCode = 10;
	         Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
	         dialog.show();
	     }else {  	      	
	    	 // Getting LocationManager object from System Service LOCATION_SERVICE
	         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	         if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
	        	 if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
	 	        	AlertDialog.Builder dialog=new AlertDialog.Builder(this);
		    		//print the message informing the user that there are no GPS or network services
		    		dialog.setMessage("GPS and Network services are not activated on your device. Go to settings to change your preferences.");
		    		dialog.setCancelable(false);
		    		dialog.setPositiveButton("Ok", null);
		    		dialog.create();
		    		dialog.show();
		    		noCxn=true;
	        	 }
	         }
	        if (!noCxn){
		        // Creating a criteria object to retrieve provider
		        Criteria criteria = new Criteria();
		        // Getting the name of the best provider
		        locationManager.getBestProvider(criteria, true);	 
	            
			     // Create the LocationRequest object
		        myLocationRequest = LocationRequest.create();
		        // Use high accuracy
		        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		        // Set the update interval to 1 seconds
		        myLocationRequest.setInterval(UPDATE_INTERVAL);
		        // Set the fastest update interval to 1 second
		        myLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		        
		        mlocListener = new MyLocationListener();
		        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
		        new LocationControl().execute();
	        }
	    }
    }
	
	public class MyLocationListener implements LocationListener{
 	    public void onStatusChanged(String provider, int status, Bundle extras) {}
 	    public void onProviderEnabled(String provider) {}
 	    public void onProviderDisabled(String provider) {}

 	    public void onLocationChanged(Location location) {
 	    	lat=location.getLatitude();
            lon=location.getLongitude();
            hasLocation=true;
            if(location.getAccuracy()<100){
            	accurateLocation=true;
            }
 	    }
 	}
	
	private class LocationControl extends AsyncTask<Context, Void, Void>{
        private final ProgressDialog dialog = new ProgressDialog(SendStream.this);

        protected void onPreExecute(){
            this.dialog.setMessage(getString(R.string.gpsSearch));
            this.dialog.show();
        }

        protected Void doInBackground(Context... params){
            Long t = Calendar.getInstance().getTimeInMillis();
            while (!hasLocation && Calendar.getInstance().getTimeInMillis() - t < 15000) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            if(hasLocation){
            	while(!accurateLocation && Calendar.getInstance().getTimeInMillis() - t < 20000){
                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            	}
            }
            return null;
        }

        protected void onPostExecute(final Void unused){
            if(this.dialog.isShowing()){
                this.dialog.dismiss();
            }
            if(locationManager!=null){
    			locationManager.removeUpdates(mlocListener);
    		}
            if (hasLocation){
            	new sendStreamTask().execute();
            }
            else{
            	Toast.makeText(getApplicationContext(), getString(R.string.notFound), Toast.LENGTH_LONG).show();
            }
        }
    }
	
	private class sendStreamTask extends AsyncTask<String, Void, String>{
    	
	    protected String doInBackground(String... params){
	    	return sendData();
	    }
	    
	    protected void onPostExecute(String result){
	    	//show the message returned from Carriots to the user
	    	Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
	    	//if the stream hasn't already been sent, dynamically create a button to go back to step 1
	    	if(sent!=1){
	    		createButton(findViewById(R.id.sendStream));
	    	}
			//add an if to only do this if the response in "ok"
	    	if(result.equalsIgnoreCase("{ \"response\": \"OK\" }")){
	    		new getStreamsTask().execute();
	    	}else{
	    		sent=1;
	    	}
	    }
	    
	    protected String sendData(){ 
			String decodedString="";
			String writeOut="";
			String returnMsg="";
			String request = "https://api.carriots.com/streams";
			URL url;
			HttpURLConnection connection = null;  
			try {
				url = new URL(request); 
				connection = (HttpURLConnection) url.openConnection();
				//establish the parameters for the http post request
				connection.setDoOutput(true);
				connection.addRequestProperty("carriots.apikey", apikey);
				connection.addRequestProperty("Content-Type", "application/json");
				connection.setRequestMethod("POST"); 
				//construct the json to be sent
				writeOut = "{\"protocol\":\"v2\",\"checksum\":\"\"," +
				"\"device\":\"" + device +
				"\",\"at\":\"now\"," +
				"\"data\":";
				//check if the gps box is checked and if so add the coordinates
				if(gpsChecked){
					writeOut += "{\"lat\":\""+lat+"\",\"lon\":\""+lon+"\",";
					String sub=jsonArray.toString();
					sub=sub.substring(1);
					writeOut += sub;
				}else{
					writeOut += jsonArray.toString();
				}
				writeOut += "}";
				//create an output stream writer and write the json to it
				final OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
				osw.write(writeOut);
				osw.close();
				//create a buffered reader to interpret the incoming message from the carriots system
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while ((decodedString = in.readLine()) != null) {
					returnMsg+=decodedString;
				}
				in.close();
				connection.disconnect();
			   	} catch (Exception e) {
			   		e.printStackTrace(); 
			   		returnMsg=""+e;
			   	}
			return returnMsg;
	    }
	}
	
	private class getStreamsTask extends AsyncTask<String, Void, String>{
    	
	    protected String doInBackground(String... params){
	    	return getData();
	    }
	    
	    protected void onPostExecute(String result){
	    	createTable();
	    	sent=1;  	
	    }
	    
	    protected String getData(){ 
			String decodedString="";
			String returnMsg="";
			String request = "http://api.carriots.com/devices/"+device+"/streams/?order=-1&max=10";
			URL url;
			HttpURLConnection connection = null;  
			try {
				url = new URL(request); 
				connection = (HttpURLConnection) url.openConnection();
				//establish the parameters for the http post request
				connection.addRequestProperty("carriots.apikey", apikey);
				connection.addRequestProperty("Content-Type", "application/json");
				connection.setRequestMethod("GET"); 
				//create a buffered reader to interpret the incoming message from the carriots system
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while ((decodedString = in.readLine()) != null) {
					returnMsg+=decodedString;
				}
				in.close();
				JSONObject nodeRoot = new JSONObject(returnMsg);
			    //obtain a json array from the value of the key "result"
			    JSONArray res=nodeRoot.getJSONArray("result"); 
			    //go through the array and get the values of the time "at" and data "data"
			    int i=0;
			    int previousSize=atList.size();
			    do{
			    	JSONObject childJSON = res.getJSONObject(i);
				    if (childJSON.get("at")!=null){
				    	String value = childJSON.getString("at");
				    	//if the data has already been sent once use the set method instead of add
				    	if (sent!=1){
				    		atList.add(value);
				    	}else{
				    		if(previousSize==i){
				    			atList.add(value);
				    		}else{
				    			atList.set(i, value);
				    		}
				    	}
				    }
				    if (childJSON.get("data")!=null){
				    	String value = childJSON.getString("data");
				    	//if the data has already been sent once use the set method instead of add
				    	if (sent!=1){
				    		dataList.add(value);
				    	}else{
				    		if(previousSize==i){
				    			dataList.add(value);
				    		}else{
				    			dataList.set(i, value);
				    		}
				    	}
				    }
			    	i++;
			    }while(i<res.length() && i<10);
				connection.disconnect();
			   	} catch (Exception e) {
			   		e.printStackTrace(); 
			   		returnMsg=""+e;
			   	}
			return returnMsg;
	    }

	}
	
	public void onStop(){
		super.onStop();
		//if the location manager exists, remove the location listener
		if(locationManager!=null){
			locationManager.removeUpdates(mlocListener);
		}
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		//store the state (json sent of not) for the orientation change
		savedInstanceState.putInt("State", sent);
		for(int i=0;i<atList.size();i++)
		{
			savedInstanceState.putString("atList"+i,atList.get(i));
			savedInstanceState.putString("dataList"+i,dataList.get(i));
		}
		savedInstanceState.putInt("arraySize",atList.size());
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		//confirm that the state of the activity is sent
		sent=savedInstanceState.getInt("State");
		if(sent!=0){
			//create the dynamic button that is shown when the json has already been sent
			createButton(findViewById(R.id.sendStream));
			//get all of the values saved from the lists that hold the values of the 10 previous streams and put them back into the array lists
			int size=savedInstanceState.getInt("arraySize",0);
			for(int j=0;j<size;j++)
			{
				atList.add(savedInstanceState.getString("atList"+j));
				dataList.add(savedInstanceState.getString("dataList"+j));
			}
			//reestablish the values of the table with the 10 previous streams
			createTable();
		}
	}

}