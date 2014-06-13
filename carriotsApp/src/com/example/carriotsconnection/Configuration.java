package com.example.carriotsconnection;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Configuration extends Activity {
	private TextView apikey;
	private static final int CODIGO_PETICION = 100;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);
		//get the shared preferences (apikey) from the previous session
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String savedApikey = sharedPreferences.getString("apikey", "notFound");
		apikey=(TextView)findViewById(R.id.changeApikey);
		//print the apikey in a test view if there is one stored
		if (!(savedApikey.equalsIgnoreCase("notFound"))){
			String hiddenKey=savedApikey.substring(0, 6);
			apikey.setText(hiddenKey+"...");
		}	
	}
	
	public void saveApikey(View v){
		String key="";
		//get the value from the edit text
		EditText newApikey=(EditText) findViewById(R.id.newApikey);
		key=newApikey.getText().toString();
		//check that the apikey is the correct length (64 characters)
		if(key.length()!=64){
			AlertDialog.Builder dialog=new AlertDialog.Builder(this);
			//print the message informing the user that the apikey is not valid
			dialog.setMessage(R.string.alertApikey);
			dialog.setCancelable(false);
			dialog.setPositiveButton("Ok", null);
			dialog.create();
			dialog.show();
		}else{
			//use the edit text value and print the first 6 characters in the apikey text view
			String hiddenKey=key.substring(0, 6);
			apikey.setText(hiddenKey+"...");
			//store the edit text value in shared preferences to be used in future sessions 
			//and in other activities of the application
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			Editor editor = sharedPreferences.edit();
			editor.putString("apikey", key);
			editor.commit();
			newApikey.setText("");
		}
	}
	
	public void scanApikey(View v){
		//when the button scanApikey is clicked, instantiate the IntentIntegrator class that will check if Barcode Scanner is installed 
		IntentIntegrator integrator = new IntentIntegrator();
        integrator.initiateScan(Configuration.this);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		EditText newApikey=(EditText) findViewById(R.id.newApikey);
		//check if the request comes from IntentIntegrator
		if(requestCode==0x0ba7c0de){
			if(intent!=null){
				//if Barcode Scanner is installed get the result of the scan
				IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
				//sent the text box as the result from the scan
				String contents=scanResult.getContents();
		        newApikey.setText(contents);
			}else{
				//if Barcode Scanner isn't installed give the user the option to go to google play and install it or use the default scanner
			    AlertDialog.Builder downloadDialog = new AlertDialog.Builder(this);
			    downloadDialog.setTitle(getString(R.string.titleBarcodeAlert));
			    downloadDialog.setMessage(getString(R.string.msgBarcodeAlert));
			    downloadDialog.setPositiveButton(getString(R.string.alertYes), new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialogInterface, int i) {
			        Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
			        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			        startActivity(intent);
			      }
			    });
			    downloadDialog.setNegativeButton(getString(R.string.alertNo), new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialogInterface, int i) {
			    	  //start the Scanner activity that is the default scanner
			    	  Intent data = new Intent();
			    	  data.setClass(getApplicationContext(), Scanner.class);
			    	  startActivityForResult(data, CODIGO_PETICION);
			      }
			    });
			    downloadDialog.show();	
			}
		}
		//check if the request comes from the default Scanner class
		if(requestCode == CODIGO_PETICION) {
			//sent the text box as the result from the scan
			String apikey = intent.getStringExtra("apikey");
			newApikey.setText(apikey);
		}
	}

}
