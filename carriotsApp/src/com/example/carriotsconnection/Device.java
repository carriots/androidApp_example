package com.example.carriotsconnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Device extends Activity {
	private String items[]=null;
	private int selectedIndex;
	private Spinner dropdown;
	private boolean map;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device);
		//get the parameters from the previous activity to see where to go next
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		map = sharedPreferences.getBoolean("mapClass", false);
		getDevices();
	}
	
	private void getDevices(){
		//get the apikey from shared preferences
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String apikey = sharedPreferences.getString("apikey", "notFound");
		//call another thread to make the http get request of the devices in the background
		new MyAsyncTask().execute(apikey);
	}	
	
	private void deviceSpinner(){
		if(items != null){
			//it the items have been retrieved, use them to populate the spinner
			dropdown = (Spinner)findViewById(R.id.deviceSpinner);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
			dropdown.setAdapter(adapter);
		}else{
			AlertDialog.Builder dialog=new AlertDialog.Builder(this);
			//print the message informing the user that the cause of the problem might be an incorrect apikey
			dialog.setMessage(R.string.alertDevice);
			dialog.setCancelable(false);
			//this option takes the user to the configuration page to enter a new apikey
			dialog.setPositiveButton(R.string.alertYes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent=new Intent();
			    	intent.setClass(getApplicationContext(),Configuration.class);
			    	startActivity(intent);
				}
			});
			//this option cancels the alert message
			dialog.setNegativeButton(R.string.alertNo, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			dialog.create();
			dialog.show();
		}
	}
	
	 private class MyAsyncTask extends AsyncTask<String, Void, String>{
	    	
		    protected String doInBackground(String... params) {
		    	getData(params[0]);
		    	return null;
		    }
		    
		    protected void onPostExecute(String result){
		    	//when the thread is finished executing create the device spinner in the layout
		    	deviceSpinner();
		    }
		    
		    protected void getData(String apikey){
		    	ArrayList<String> deviceList = new ArrayList<String>();
		    	String result="", decodedString="";
				String request = "http://api.carriots.com/devices/";
				HttpURLConnection connection = null;  
				URL url;
				try {
					//open a URLConnection with the carriots server
					//add the header properties and specify the method
					url = new URL(request); 
				    connection = (HttpURLConnection) url.openConnection();
				    connection.addRequestProperty("carriots.apikey", apikey);
				    connection.addRequestProperty("Accept", "application/json");
				    connection.addRequestProperty("Host", "api.carriots.com");
				    connection.setRequestMethod("GET");
				    //use a buffered reader to get the returned json from the server
				    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				    while ((decodedString = in.readLine()) != null) {
				    	result+=decodedString;
				    }
				    in.close();
					//create a json object from the json string
				    JSONObject nodeRoot = new JSONObject(result);
				    //obtain a json array from the value of the key "result"
				    JSONArray res=nodeRoot.getJSONArray("result"); 
				    //go through the array and get the values of the key "id_developer"
				    for (int i = 0; i < res.length(); i++) {
				    	JSONObject childJSON = res.getJSONObject(i);
					    if (childJSON.get("id_developer")!=null){
					    	String value = childJSON.getString("id_developer");
					    	deviceList.add(value);
					    }
					}
				    items = deviceList.toArray(new String[deviceList.size()]);
				    connection.disconnect();
				  }catch (Exception e) {
					  e.printStackTrace();
				  }
		    }
	 }
	 
	public void goToStep2(View v){
		if(items==null){
			AlertDialog.Builder dialog=new AlertDialog.Builder(this);
			//print the message informing the user that the apikey is not valid
			dialog.setMessage(R.string.alertNoDevice);
			dialog.setCancelable(false);
			dialog.setPositiveButton("Ok", null);
			dialog.create();
			dialog.show();
		}else{
			Intent intent=new Intent();
			selectedIndex=dropdown.getSelectedItemPosition();
			//check that the destination is StreamMap or SendStream and put the correct variables accordingly
			if(map){
				intent.setClass(getApplicationContext(), StreamMap.class);
				intent.putExtra("device", items[selectedIndex]);
			}else{
		    	intent.setClass(getApplicationContext(),Json.class);
		    	intent.putExtra("devices", items);
		    	intent.putExtra("selectedIndex", selectedIndex);
			}
	    	startActivity(intent);
		}
	}
	
}