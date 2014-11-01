package com.oy.vent;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity{

	private EditText txtFullName = null;
	private EditText txtEmail = null;
	private EditText txtUsername = null;
	private EditText txtPassword = null;
	private Button btnRegister = null;
	private static final String TAG = "RegisterActivity.java";	
	private static final int REQUEST_CODE_MAIN_CLASS = 111;
	private static final String PREF_FILE_NAME = "OyventAppFile";

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_register);      
        
        txtFullName = (EditText)findViewById(R.id.txtFullName);    
        txtFullName.setEnabled(false);
        
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtEmail.setEnabled(false);
        
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtUsername.setEnabled(false);
        
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtPassword.setEnabled(false);
        
        txtFullName.setText("Test");
        txtEmail.setText("test@test.com");
        txtUsername.setText("5011111111");
        txtPassword.setText("test");
        
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setClickable(true);
        btnRegister.setOnClickListener(new OnClickListener(){ 
        	@Override
			public void onClick(View v) {	
        		
        		goToMainActivity();
        	}
        
        });
        
	}
	
	@Override
    protected void onStart() {
        super.onStart();
              
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
				txtFullName.setText("");
				
				//and then go to login screen
				goToLoginActivity();
			}			
			else if(resultCode == 6)//just exit the application
			{
				finish();
				System.exit(0);
			}
			
		}
	}
	
	//remove user info from hard disk
		private void removeUser()
		{
			SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
			sharedPref.edit().remove("userInfo").commit();
		}
	
	private void goToLoginActivity(){
		Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
		startActivity(intent);
	}
		
	private void goToMainActivity() {
		Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
		startActivityForResult(intent, REQUEST_CODE_MAIN_CLASS);		
	}	
}
