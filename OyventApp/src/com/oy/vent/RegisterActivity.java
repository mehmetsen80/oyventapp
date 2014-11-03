package com.oy.vent;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.oy.vent.model.UserInfo;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity{
	
	private EditText txtEmail = null;
	private EditText txtUsername = null;
	private EditText txtPassword = null;
	private TextView txtLogin = null;
	private TextView txtBrowseNow = null;
	private Button btnRegister = null;
	private static final String TAG = "RegisterActivity.java";//log tag
	private static final String JSON_URL = "http://oyvent.com/ajax/Register.php";//json register url
	private ProgressDialog pDialog;// Progress Dialog	
	
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_register);       
        
        txtEmail = (EditText)findViewById(R.id.txtEmail);        
        txtUsername = (EditText) findViewById(R.id.txtUsername);        
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtEmail.setText("");
        txtUsername.setText("");
        txtPassword.setText("");		
        
        txtLogin = (TextView)findViewById(R.id.loginnow);
        txtLogin.setClickable(true);
        txtLogin.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) {        		
        		goToLoginActivity();
        	}        
        });
        
        txtBrowseNow = (TextView)findViewById(R.id.browsenow);
        txtBrowseNow.setClickable(true);
        txtBrowseNow.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) { 
        		UserInfo userInfo = new UserInfo();
        		userInfo.userID = 0d;
        		userInfo.email = "anonymous@anonymous";
        		userInfo.username = "anonymous";
        		userInfo.password = "anonymous";
        		saveUserInfo(userInfo);
        		goToMainActivity();
        	}        
        });
        
     
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setClickable(true);
        btnRegister.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) {        		
        		goToMainActivity();
        		new RegisterFeed().execute(txtEmail.getText().toString(),txtUsername.getText().toString(),txtPassword.getText().toString());
        	}        
        });       
        
        pDialog = new ProgressDialog(RegisterActivity.this);
	}	
		
	
	//register json feed task
	class RegisterFeed extends AsyncTask<String, String, UserInfo> {		
		
		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog.setMessage("Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		/**
		 * getting Feeds JSON
		 * */
		@Override
		protected UserInfo doInBackground(String... args) {	
			
			UserInfo userInfo = new UserInfo();
			userInfo.message = "Invalid register attempt!";			
			
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("processType", "SIGNUPUSER"));	
			params.add(new BasicNameValuePair("email", args[0]));
			params.add(new BasicNameValuePair("username", args[1]));
			params.add(new BasicNameValuePair("password", args[2]));
						
			// getting JSON string from URL
			String json = jsonParser.makeHttpRequest(JSON_URL, "GET", params);						
			// Check your log cat for JSON response
			Log.d("Register Feed JSON: ", "> " + json);

			try {				
					JSONArray results = new JSONArray(json);							
					if (results != null) {
						// looping through All feeds
						for (int i = 0; i < results.length(); i++) {									
							JSONObject c = results.getJSONObject(i);									
							userInfo.success = c.getBoolean("success");
							userInfo.message = c.getString("message");
							//if successfully registered in get all the user info
							if(userInfo.success)
							{							
								userInfo.userID = c.getDouble("userID");
								userInfo.username = c.getString("username");										
								userInfo.email = c.getString("email");										
								userInfo.lastlogindate = c.getString("lastlogindate");
								userInfo.signupdate = c.getString("signupdate");									
							}
							else
							{							
								Log.d(TAG,"Success: "+userInfo.success+"  Error:"+userInfo.message);
							}
						}
					}				

				} catch (JSONException e) {
						Log.e(TAG,e.getMessage());
				}
			
			
			return userInfo;
		}
		
		/**
		* After completing background task Dismiss the progress dialog
		* **/
		@Override
		protected void onPostExecute(final UserInfo userInfo) {
			// dismiss the dialog after getting user info
			pDialog.dismiss();
			
			if(userInfo.success)
			{
				saveUserInfo(userInfo);
				goToMainActivity();
			}
			else{						
				Toast toast = Toast.makeText(getApplicationContext(), userInfo.message,  Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();	
			}			
		}	
		
	}
	
}
