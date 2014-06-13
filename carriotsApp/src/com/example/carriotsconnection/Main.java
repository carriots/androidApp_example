package com.example.carriotsconnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

public class Main extends Activity {
	private SharedPreferences sharedPreferences;
	private Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    }
    
    public void clicked(View v){
    	String apikey;
    	Intent intent=new Intent();
    	//determine which event called the clicked method and begin the appropriate action accordingly
		switch (v.getId()) {
    		case R.id.btnStreams: 
    			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    			apikey = sharedPreferences.getString("apikey", "notFound");
    			//check that there is an apikey and if not go to an alert message
    			if (apikey.equals("notFound")){
    				AlertDialog.Builder dialog=new AlertDialog.Builder(this);
    				//print the message informing the user that the apikey has not been established
    				dialog.setMessage(R.string.alertMsg);
    				dialog.setCancelable(false);
    				//this option takes the user to the configuration page to enter their apikey
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
    			}else{
    				intent.setClass(getApplicationContext(),Device.class);
    	    		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    				editor = sharedPreferences.edit();
    				editor.putBoolean("mapClass", false);
    				editor.commit();
    				startActivity(intent);
    			}
    			break;
    		case R.id.btnConfig: intent.setClass(getApplicationContext(),Configuration.class); startActivity(intent); break;
    		case R.id.btnMap: intent.setClass(getApplicationContext(),Device.class);
	    		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
				editor = sharedPreferences.edit();
				editor.putBoolean("mapClass", true);
				editor.commit();
    			startActivity(intent); 
    			break;
    		case R.id.ivLogo: intent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.carriots.com")); startActivity(intent); break;
    	}
    	
    }

}