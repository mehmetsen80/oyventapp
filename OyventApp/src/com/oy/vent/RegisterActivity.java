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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity{

	private EditText txtEmail = null;
	private EditText txtUsername = null;
	private EditText txtPassword = null;
	private Button btnRegister = null;
	private static final String TAG = "RegisterActivity.java";	
	private static final int REQUEST_CODE_MAIN_CLASS = 111;//main activity
	private static final int REQUEST_CODE_LOGIN_CLASS = 222;//login activity	
	private ProgressDialog pDialog;// Progress Dialog	
	private static final String PREF_FILE_NAME = "OyventAppFile";//hard disk file name
	private static final String JSON_URL = "http://oyvent.com/ajax/Register.php";//json register url
	private final JSONParser jsonParser = new JSONParser();//Creating JSON Parser object

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_register);       
        
        txtEmail = (EditText)findViewById(R.id.txtEmail);        
        txtUsername = (EditText) findViewById(R.id.txtUsername);        
        txtPassword = (EditText)findViewById(R.id.txtPassword);             
     
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
	
	@Override
    protected void onStart() {
        super.onStart();
        
        //if userInfo object exists on harddisk then skip to main Activity
        UserInfo userInfo = getUserInfo();        
        
        if(userInfo != null && userInfo.email != "")
        {
        	Log.e(TAG,"userinfo is not null");
        	goToMainActivity();        	
        }
        else{
        	Log.e(TAG,"userinfo is null");
        }
              
    }
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_MAIN_CLASS){
			
			if(resultCode == 5)//if logged out
			{
				Log.d(TAG,"let's remove user");
				removeUser();				
				txtPassword.setText("");
				txtUsername.setText("");
				txtEmail.setText("");
				
				//and then go to login screen
				goToLoginActivity();
			}			
			else if(resultCode == 6)//just exit the application
			{
				finish();
				System.exit(0);
			}			
		}else if(requestCode == REQUEST_CODE_LOGIN_CLASS){
			goToLoginActivity();
		}
	}
	
	//read user info from hard disk
	private UserInfo getUserInfo()
	{		
		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);		
		Gson gson = new Gson();
		String json = sharedPref.getString("userInfo", "");
		UserInfo usr = gson.fromJson(json, UserInfo.class);		
		return usr;
	}
	
	//remove user info from hard disk
	private void removeUser()
	{
		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
		sharedPref.edit().remove("userInfo").commit();
	}
	
	//save user info
	private void saveUserInfo(UserInfo usr)
	{
		SharedPreferences sharedPref =  getSharedPreferences( PREF_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Gson gson = new Gson();
		String json = gson.toJson(usr);
		editor.putString("userInfo", json);
		editor.commit();
	}
		
	//go to the Login Screen
	private void goToLoginActivity(){
		Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
		startActivityForResult(intent, REQUEST_CODE_LOGIN_CLASS);
	}
	
		
	private void goToMainActivity() {
		Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
		startActivityForResult(intent, REQUEST_CODE_MAIN_CLASS);		
	}
	
	
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
								userInfo.userID = c.getString("userID");
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
						e.printStackTrace();
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
