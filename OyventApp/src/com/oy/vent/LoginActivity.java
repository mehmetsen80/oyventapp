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


public class LoginActivity extends BaseActivity{

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
