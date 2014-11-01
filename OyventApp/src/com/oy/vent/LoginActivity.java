package com.oy.vent;



import com.google.gson.Gson;
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


public class LoginActivity extends Activity{

	private Button btnLogin = null;
	private Button btnRegister = null;
	
	private EditText txtUsername = null;
	private EditText txtPassword = null;
	
	// Progress Dialog
	private ProgressDialog pDialog;
	
	private static final String TAG = "LoginActivity.java";	
	private static final int REQUEST_CODE_MAIN_CLASS = 111;
	private static final int REQUEST_CODE_REGISTER_CLASS = 112;	
	private static final String PREF_FILE_NAME = "OyventFileApp";
	
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
        
        
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setClickable(true);
        btnRegister.setOnClickListener(new OnClickListener(){ 
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
		 * getting Feeds JSON
		 * */
		@Override
		protected UserInfo doInBackground(String... args) {	
			
			UserInfo userInfo = new UserInfo();
			
			userInfo.email = args[0];
			userInfo.password = args[1];
			
			//to do: database login requests
			
			return userInfo;
		}
		
		/**
		* After completing background task Dismiss the progress dialog
		* **/
		@Override
		protected void onPostExecute(final UserInfo userInfo) {
			// dismiss the dialog after getting user info
			pDialog.dismiss();
			
			goToMainActivity();
			
			if(userInfo.email.equals("test") && userInfo.password.equals("test"))
			{
				saveUserInfo(userInfo);
				goToMainActivity();
			}
			else{
				userInfo.error = "Please enter username and password";				
				Toast toast = Toast.makeText(getApplicationContext(), userInfo.error,  Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();	
			}
			
		}	
		
	}
}
