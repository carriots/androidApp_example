package com.example.carriotsconnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class StreamMap extends FragmentActivity {	 
    private GoogleMap googleMap;
    private String device, apikey;
    private ArrayList<String> latList=new ArrayList<String>();
    private ArrayList<String> longList=new ArrayList<String>();
    private ArrayList<String> dataList=new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_map);
        
	    //get the parameters from the previous activity (device and json array)
	    Bundle extras=getIntent().getExtras();
	    device=extras.getString("device");
	    
	    //set the text box with the device name
	    TextView deviceName=(TextView)findViewById(R.id.deviceName);
	    deviceName.setText(device);
	    
	    //get the apikey
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		apikey = sharedPreferences.getString("apikey", "notFound");
        
        //get google play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        //if google play isn't available go to error dialog, otherwise get markers
        if(status!=ConnectionResult.SUCCESS){ 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        }else {   
            new getStreamsTask().execute();
        }
    }
    
    private void createMarkers(){
    	// Get SupportMapFragment from activity_stream_map.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //get google map object from the fragment
        googleMap = fm.getMap();
        //set camera zoom icons
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        //if the device has no streams with locations send a message to the user
        if(latList.size()==0){
        	Toast.makeText(getApplicationContext(), getString(R.string.noStreamsLocation), Toast.LENGTH_LONG).show();
        }
        for(int i=0; i<latList.size();i++){
            //add marker to location, set the title and the snippet (the data of the stream)
            googleMap.addMarker(new MarkerOptions()
            .position(new LatLng(Double.parseDouble(latList.get(i)),Double.parseDouble(longList.get(i))))
            .title("Stream "+(i+1)) 
            .snippet(dataList.get(i)) 
            .icon(BitmapDescriptorFactory
            .fromResource(R.drawable.minicarriot)));
            //sets the camera position to the most recent stream
            if(i==0){
            	//sets the target as the coordinate, sets the zoom
                CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(Double.parseDouble(latList.get(i)),Double.parseDouble(longList.get(i))))      
                .zoom(15)                   
                .build();                   
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
        //adds a custom window adapter to show the title and stream information
        googleMap.setInfoWindowAdapter(new WindowAdapter(getLayoutInflater()));
    }
    
    private class WindowAdapter implements InfoWindowAdapter {
    	LayoutInflater inflater=null;

    	WindowAdapter(LayoutInflater inflater) {
    		this.inflater=inflater;
    	}

    	public View getInfoWindow(Marker marker) {
    		return(null);
  	  	}

    	public View getInfoContents(Marker marker) {
    		//inflates the window layout
    		View info=inflater.inflate(R.layout.windowlayout, null);
    		//sets the title of the marker (stream number) as the first text view
    		TextView txt=(TextView)info.findViewById(R.id.title);
    		txt.setText(marker.getTitle());
    		//sets the snippet of the marker (stream data) as the second text view
    		txt=(TextView)info.findViewById(R.id.data);
    		txt.setText(marker.getSnippet());
    		return(info);
  	  }
    }
    
    private class getStreamsTask extends AsyncTask<String, Void, String>{
    	
	    protected String doInBackground(String... params) {
	    	return getData();
	    }
	    
	    protected void onPostExecute(String result){
	    	createMarkers();
	    }
	    
	    protected String getData(){ 
			String decodedString="";
			String returnMsg="";
			//the request gets the 10 most recent streams that contain the "GPS" key
			String request = "http://api.carriots.com/devices/"+device+"/streams/?_contains[]=lat&_contains[]=long&order=-1&max=10";
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
			    //go through the array and get the value of "data"
			    int i=0;
			    do{
			    	JSONObject childJSON = res.getJSONObject(i);
				    if (childJSON.get("data")!=null){
				    	String value = childJSON.getString("data");
				    	dataList.add(value);
				    	JSONObject node=new JSONObject(value);
				    	//get the coordinates from the lat and long keys
				    	latList.add(node.get("lat").toString());
				    	longList.add(node.get("long").toString());
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
   
}