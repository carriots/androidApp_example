package com.example.carriotsconnection;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class Json extends Activity {
	private String[] items;
	private Spinner dropdown;
	private int cont=0;
	//arrays used to store the ids of editTexts and textViews created dynamically
	private ArrayList<Integer> keysId=new ArrayList<Integer>();
	private ArrayList<Integer> valuesId=new ArrayList<Integer>();
	private TabHost tabHost;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.json);
		//get the devices from a parameter passed from the previous activity
		Bundle extras=getIntent().getExtras();
		items=extras.getStringArray("devices");
		dropdown = (Spinner)findViewById(R.id.deviceSpinner);
		//use the parameters to put each element (device) of the array into a Spinner
		int selectedIndex=extras.getInt("selectedIndex");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		dropdown.setAdapter(adapter);
		dropdown.setSelection(selectedIndex);
		
		//instatiate a tab host to store and create the two tabs (AddParam and Post Body)
        tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        //Add param tab
        TabSpec spec1=tabHost.newTabSpec(getString(R.string.tabAddParam));
        spec1.setContent(R.id.tab1);
        spec1.setIndicator(getString(R.string.tabAddParam));
        //Add post body tab
        TabSpec spec2=tabHost.newTabSpec(getString(R.string.tabPostBody));
        spec2.setIndicator(getString(R.string.tabPostBody));
        spec2.setContent(R.id.tab2);
        //add the tabs to the tab host
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
	}
	
	public void addData(View v){
		//get the view of the table layout
		TableLayout tb=(TableLayout) findViewById(R.id.table);
		//dynamically add a row
        TableRow tr1=new TableRow(v.getContext());
        //dynamically add a text view and set its text
		TextView key=new TextView(v.getContext());
        key.setText(getString(R.string.keyLabel));
        //dynamically add an edit text and set its values
        EditText keyBox=new EditText(v.getContext());
        keyBox.setId(cont);
        keyBox.setWidth(300);
        keyBox.setMaxLines(100);
        //add the text view and edit text to the row
        tr1.addView(key);
        tr1.addView(keyBox);
        //add the row to the table
        tb.addView(tr1);
        //add the edit text id to an array
		keysId.add(keyBox.getId());
		//use a counter to later know the number of elements in the array
		cont++;
		//do the same steps for the "value" text view and edit text box
        TableRow tr2=new TableRow(v.getContext());
        TextView value=new TextView(v.getContext());
        value.setText(getString(R.string.valueLabel));
        EditText valueBox=new EditText(v.getContext());
        valueBox.setId(cont);
        valueBox.setMaxLines(100);
        valueBox.setWidth(300);
        tr2.addView(value);
        tr2.addView(valueBox);
        tb.addView(tr2);
        valuesId.add(valueBox.getId());
        cont++;
	}
	
	public void goToStep3(View v){
		boolean valid=false;
		JSONArray ja=new JSONArray();
		//determine which tab currently has the focus
		if(tabHost.getCurrentTab()==0){
			//get the values of the static elements
			EditText key=(EditText) findViewById(R.id.editTextKey);
			EditText value=(EditText) findViewById(R.id.editTextValue);
			//create a json object
			JSONObject json=new JSONObject();
			//confirm that the static key has a value
			if(!(key.getText().toString()).equals("")){
				try {
					//add the first key-value pair to the json object
					json.put(key.getText().toString(), value.getText().toString());
					valid=true;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			//confirm that there are elements in the array (that dynamic elements have been created)
			if(!keysId.isEmpty()){
				//iterate through the elements of the array
				for (int i=0;i<(cont/2); i++){
					EditText possibleKey=(EditText) findViewById(keysId.get(i));
					//confirm that the key has a value
					if(!(possibleKey.getText().toString()).equals("")){
						EditText possibleValue=(EditText) findViewById(valuesId.get(i));
						try {
							//add the key-value pair to the json object
							json.put(possibleKey.getText().toString(), possibleValue.getText().toString());
							valid=true;
						}catch (JSONException e) {
							e.printStackTrace();
							valid=false;
						}
					}
				}
			}
			//put the json into a json array
			try{
				ja.put(json);
			}catch (Exception e){
				valid=false;
			}
		//in the case of tab 1
		}else{
			//get the text and set the json array
			EditText textBody=(EditText)findViewById(R.id.multiLineJson);
			try {
				//add the String to a json array by converting it into a json object
				ja.put(new JSONObject(textBody.getText().toString()));
				valid=true;	        
			} catch(JSONException ex) {}
		}
		if (valid){
			//if the json is valid add the parameters and send it to the SendStream class
			Intent intent=new Intent();
	    	intent.setClass(getApplicationContext(),SendStream.class);
	    	int selectedIndex=dropdown.getSelectedItemPosition();
	    	//send a parameter with the selected device
	    	intent.putExtra("device", items[selectedIndex]);
	    	//send the json array to the next activity
	    	intent.putExtra("jsonArray", ja.toString());
	    	startActivity(intent);	
		}else{
			//print the message informing the user that there is no Json or it is not valid
	    	AlertDialog.Builder dialog=new AlertDialog.Builder(this);
			dialog.setMessage(R.string.jsonNotValid);
			dialog.setCancelable(false);
			dialog.setPositiveButton("Ok", null);
			dialog.create();
			dialog.show();
		}
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		//in the case orientation change, check that the keyId array is not empty
		if(!keysId.isEmpty()){
			int numPairs=0;
			//iterate through the keysId array
			for (int i=0;i<(cont/2); i++){
				EditText possibleKey=(EditText) findViewById(keysId.get(i));
				EditText possibleValue=(EditText) findViewById(valuesId.get(i));
				//store the keys and values in a bundle to be used in case of another orientation change
				savedInstanceState.putString("keys_"+i, possibleKey.getText().toString());
				savedInstanceState.putString("values_"+i, possibleValue.getText().toString());
				numPairs++;
			}
			//store the number of pairs in the bundle
			savedInstanceState.putInt("numPairs", numPairs);
		}
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		int numPairs=savedInstanceState.getInt("numPairs");
		//get the number of pairs from the previous state and iterate through the bucle for each number
		for(int i=0; i<numPairs; i++){	
			//create the dynamic elements that were present in the previous state
			TableLayout tb=(TableLayout) findViewById(R.id.table);
			
	        TableRow tr2=new TableRow(getApplicationContext());
			TextView key=new TextView(getApplicationContext());
	        key.setText(getString(R.string.keyLabel));
	        key.setTextColor(getResources().getColor(R.color.lightGreen));
	        EditText keyBox=new EditText(getApplicationContext());
	        keyBox.setTextColor(Color.BLACK);
	        keyBox.setId(cont);
	        keyBox.setMaxLines(100);
	        keyBox.setWidth(300);
	        //get the text keys from the previous edit text boxes
	        keyBox.setText(savedInstanceState.getString("keys_"+i));
	        tr2.addView(key);
	        tr2.addView(keyBox);
	        tb.addView(tr2);
			keysId.add(keyBox.getId());
			cont++;
			
	        TableRow tr3=new TableRow(getApplicationContext());
	        TextView value=new TextView(getApplicationContext());
	        value.setTextColor(getResources().getColor(R.color.lightGreen));
	        value.setText(getString(R.string.valueLabel));
	        EditText valueBox=new EditText(getApplicationContext());
	        valueBox.setTextColor(Color.BLACK);
	        valueBox.setId(cont);
	        valueBox.setMaxLines(100);
	        valueBox.setWidth(300);
	        //get the text values from the previous edit text boxes
	        valueBox.setText(savedInstanceState.getString("values_"+i));
	        tr3.addView(value);
	        tr3.addView(valueBox);
	        tb.addView(tr3);
	        valuesId.add(valueBox.getId());
	        cont++;
		}	
	}

}