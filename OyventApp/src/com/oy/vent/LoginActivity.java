package com.oy.vent;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.oy.vent.helper.JSONParser;
import com.oy.vent.model.UserInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends Activity{

	protected static final String PREF_FILE_NAME = "OYVENTDATA";//hard disk file name
	protected static final int REQUEST_CODE_MAIN_CLASS = 111;//main activity
	protected static final int REQUEST_CODE_LOGIN_CLASS = 222;//login activity
	protected static final int REQUEST_CODE_REGISTER_CLASS = 333;	//register activity
	protected final JSONParser jsonParser = new JSONParser();//Creating JSON Parser object
	
	private Button btnLogin = null;
	private TextView txtRegister = null;
	private TextView txtBrowseNow = null;
	
	private EditText txtUsername = null;
	private EditText txtPassword = null;
	
	// Progress Dialog
	private ProgressDialog pDialog;
	private static final String TAG = "LoginActivity.java";//log tag
	private static final String JSON_URL = "http://oyvent.com/ajax/Login.php";//json login url
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login); 
        
        txtUsername = (EditText)findViewById(R.id.txtUsername);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setClickable(true);		
        btnLogin.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {							
				new LoginFeed().execute(txtUsername.getText().toString(),txtPassword.getText().toString());
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
        
        txtRegister = (TextView)findViewById(R.id.registernow);
        txtRegister.setClickable(true);
        txtRegister.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) {        		
        		goToRegisterActivity();
        	}        
        });
        
        pDialog = new ProgressDialog(LoginActivity.this);        
	}
	
	@Override
    protected void onStart() {
        super.onStart();        
        handleAnonymousUser();//first we lookup the anonymous user        
        UserInfo userInfo = getUserInfo();//if userInfo object exists on harddisk then skip to main Activity
        if(userInfo != null && userInfo.email != "")
             goToMainActivity();    
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	handleAnonymousUser();
        	setResult(6);
        	finish();  	
        }
		return false;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_MAIN_CLASS || 
				requestCode == REQUEST_CODE_LOGIN_CLASS ||
				requestCode == REQUEST_CODE_REGISTER_CLASS){			
			if(resultCode == 5)//if logged out
			{				
				removeUser();				
				goToRegisterActivity();//and then go to login screen
			}			
			else if(resultCode == 6)//just exit the application
			{
				/*Toast toast = Toast.makeText(getApplicationContext(), "resultCode 6",  Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();*/
				
				handleAnonymousUser();
				finish();
				System.exit(0);
			}else{
				removeUser();
				goToRegisterActivity();
			}		
		}
	}
	
	//go to the Register Screen
	protected void goToRegisterActivity(){
		Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
		startActivityForResult(intent, REQUEST_CODE_REGISTER_CLASS);
	}
	
	//go to the home-all local feeds screen
	protected void goToMainActivity() {
		Intent intent = new Intent(LoginActivity.this,MainActivity.class);
		startActivityForResult(intent, REQUEST_CODE_MAIN_CLASS);		
	}
	
	//save user info to hard disk
  	protected void saveUserInfo(UserInfo usr)
  	{
  		SharedPreferences sharedPref =  getSharedPreferences( PREF_FILE_NAME, MODE_PRIVATE);
  		SharedPreferences.Editor editor = sharedPref.edit();
  		Gson gson = new Gson();
  		String json = gson.toJson(usr);
  		editor.putString("userInfo", json);
  		editor.commit();
  	}
  	
  	//read user info from hard disk
  	protected UserInfo getUserInfo()
  	{		
  		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);		
  		Gson gson = new Gson();
  		String json = sharedPref.getString("userInfo", "");
  		UserInfo usr = gson.fromJson(json, UserInfo.class);		
  		return usr;
  	}
  	
  	//remove user info from hard disk
  	protected void removeUser()
  	{
  		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
  		sharedPref.edit().remove("userInfo").commit();
  	}
  	
  	//handle the anonymous user
  	protected void handleAnonymousUser(){ 
  		//if userInfo object exists on harddisk then skip to main Activity
          UserInfo userInfo = getUserInfo();
          if(userInfo != null){        	
          	if(userInfo.email.indexOf("anonymous@anonymous") > -1 && 
          			userInfo.username.indexOf("anonymous") > -1 && 
          			userInfo.password.indexOf("anonymous") > -1)
          		removeUser();
          }
  	}
	
	/*
	
	@Override
	protected void goToRegisterActivity(){
		super.goToRegisterActivity();
	}	
	
	
	
	
	
	@Override
	protected void goToLoginActivity(){
		super.goToLoginActivity();
	}
	
	@Override
	protected void goToMainActivity(){
		super.goToMainActivity();
	}
	
	@Override
	protected void saveUserInfo(UserInfo usr){
		super.saveUserInfo(usr);
	}
	
	@Override
	protected UserInfo getUserInfo(){
		return super.getUserInfo();
	}
	
	@Override
	protected void removeUser(){
		super.removeUser();
	}
	
	@Override
	protected void handleAnonymousUser(){
		super.handleAnonymousUser();
	}	*/
	
	//login json feed task
	class LoginFeed extends AsyncTask<String, String, UserInfo> {		
		
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
		 * getting LOGIN Feeds JSON
		 * */
		@Override
		protected UserInfo doInBackground(String... args) {	
			
			UserInfo userInfo = new UserInfo();
			userInfo.message = "Invalid login attempt!";
			/*userInfo.email = args[0];
			userInfo.password = args[1];*/
			
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("processType", "LOGINUSER"));			
			params.add(new BasicNameValuePair("email", args[0]));
			params.add(new BasicNameValuePair("password", args[1]));
						
			// getting JSON string from URL
			String json = jsonParser.makeHttpRequest(JSON_URL, "GET", params);						
			// Check your log cat for JSON response
			Log.d("Login Feed JSON: ", "> " + json);

			try {				
					JSONArray results = new JSONArray(json);							
					if (results != null) {
						// looping through All feeds
						for (int i = 0; i < results.length(); i++) {									
							JSONObject c = results.getJSONObject(i);									
							userInfo.success = c.getBoolean("success");
							userInfo.message = c.getString("message");									
							//if successfully logged in get all the user info
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
					}//if jsonresult is not null
			}catch (JSONException e) {
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
