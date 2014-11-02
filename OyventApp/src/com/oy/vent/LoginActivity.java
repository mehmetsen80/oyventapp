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
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends Activity{

	private Button btnLogin = null;
	private TextView txtRegister = null;
	
	private EditText txtUsername = null;
	private EditText txtPassword = null;
	
	// Progress Dialog
	private ProgressDialog pDialog;
	
	private static final String TAG = "LoginActivity.java";	
	private static final int REQUEST_CODE_MAIN_CLASS = 111;//main activity
	private static final int REQUEST_CODE_REGISTER_CLASS = 112;	//register activity
	private static final String PREF_FILE_NAME = "OyventFileApp";//hard disk file name
	private static final String JSON_URL = "http://oyvent.com/ajax/Login.php";//json login url
	private final JSONParser jsonParser = new JSONParser();//Creating JSON Parser object
	
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
        
        txtRegister = (TextView)findViewById(R.id.link_to_register);
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
	
	private void goToMainActivity() {
		Intent intent = new Intent(LoginActivity.this,MainActivity.class);
		startActivityForResult(intent, REQUEST_CODE_MAIN_CLASS);		
	}
	
	//go to the Register Screen
	private void goToRegisterActivity(){
		Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
		startActivityForResult(intent, REQUEST_CODE_REGISTER_CLASS);
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
			}
			else if(resultCode == 6)//just exit the application
			{
				finish();
				System.exit(0);
			}
		}
	} 
	
	//save user info to hard disk
	private void saveUserInfo(UserInfo usr)
	{
		SharedPreferences sharedPref =  getSharedPreferences( PREF_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Gson gson = new Gson();
		String json = gson.toJson(usr);
		editor.putString("userInfo", json);
		editor.commit();
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
