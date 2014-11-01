/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oy.vent.fragment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.oy.vent.BuildConfig;
import com.oy.vent.R;
import com.oy.vent.util.ImageCache.ImageCacheParams;
import com.oy.vent.util.ImageFetcher;
import com.oy.vent.util.Utils;
import com.oy.vent.AddFeedActivity;
import com.oy.vent.GPSTracker;
import com.oy.vent.ImageDetailActivity;
import com.oy.vent.model.RowItem;
import com.oy.vent.model.ViewHolder;
import com.oy.vent.helper.ImageLoader;
import com.oy.vent.helper.AlertDialogManager;
import com.oy.vent.helper.JSONParser;
import com.oy.vent.helper.ConnectionDetector;


/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener {
    
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private static final String TAG = ImageGridFragment.class.getName();
    private int mImageThumbSize;
    private int mImageThumbWidth;
    private int mImageThumbHeight;
    private int mImageThumbSpacing;
    //private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    
    private GPSTracker gpsTracker; 
    
    // Connection detector
  	ConnectionDetector cd; 	
  
    CustomListViewAdapter customAdapter = null;
    
    private boolean flag_loading = true;  
    public GridView gridView = null;
    private ArrayList<RowItem> listValue;
    
 // Alert dialog manager
  	AlertDialogManager alert = new AlertDialogManager();
  		
  	// Progress Dialog
  	private ProgressDialog pDialog;
  	
  	// Creating JSON Parser object
  	JSONParser jsonParser = new JSONParser();
 // feeds JSONArray
  	JSONArray feeds = null;
  	
  	RowItem rdItem;	
  	
  
  	
  	private LayoutInflater layoutx;
 
  	private Random random = new Random();
  	final float MIN = 10.0f;
  	final float MAX = 100.0f;
  	
  	public int count = 10;	
  	private int currentPage = 0;	
  	
  	private static final int REQUEST_CODE_FEED_CLASS = 666;  
  	private static final int REQUEST_CODE_ADD_FEED_CLASS = 777;  
  	private static final int RESULT_CODE_SEND_IMAGE = 8888;
  	
  	
  	DecimalFormat formatDecimal = new DecimalFormat("0.0"); 

 	
  	
  	private static final String URL_FEEDS = "http://oyvent.com/ajax/Feeds.php";
 	private static final String TAG_URL = "http://www.oyvent.com/";
 	
 	private static final String TAG_FEEDID = "PKFEEDID";
    private static final String TAG_POSTDATE = "POSTDATE";
	private static final String TAG_NAMETHUMB = "NAMETHUMB";
	private static final String TAG_NAMETHUMB2 = "NAMETHUMB2";
	private static final String TAG_NAMETHUMB1 = "NAMETHUMB1";
	private static final String TAG_USERNAMETHUMB2 = "USERNAMETHUMB2";
	private static final String TAG_USERNAMETHUMB1 = "USERNAMETHUMB1";
	private static final String TAG_USERNAMETHUMB0 = "USERNAMETHUMB0";
	private static final String TAG_PHYSICALPATHTHUMB = "PHYSICALPATHTHUMB";
	private static final String TAG_PHYSICALPATHTHUMB2 = "PHYSICALPATHTHUMB2";
	private static final String TAG_PHYSICALPATHTHUMB1 = "PHYSICALPATHTHUMB1";
	private static final String TAG_USERPHYSICALPATHTHUMB2= "USERPHYSICALPATHTHUMB2";
	private static final String TAG_USERPHYSICALPATHTHUMB1= "USERPHYSICALPATHTHUMB1";
	private static final String TAG_USERPHYSICALPATHTHUMB0= "USERPHYSICALPATHTHUMB0";	
	private static final String TAG_FULLNAME = "FULLNAME";	
	private static final String TAG_USERNAME = "USERNAME";
	
	
	private static final String TAG_THUMB_URL = "URLTHUMB";
 	

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG,"onAttach()");
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);        
        
        gpsTracker = new GPSTracker(getActivity());
        if (!gpsTracker.canGetLocation()){
        	gpsTracker.showSettingsAlert();	        	
        }else{        
        	double latitude = gpsTracker.getLatitude();
        	double longitude = gpsTracker.getLongitude();
        	
        	/*double lat2 = 34.776924;
        	double lon2 = -92.464114;*/
        	
        	double lat2 = 34.776924;
        	double lon2 = -92.464114;        	
        	double distance = gpsTracker.getDistance(latitude,longitude,lat2,lon2);        	
        	DecimalFormat f = new DecimalFormat("0.0");  
        	Toast.makeText(getActivity(),"Mile:"+String.valueOf(f.format(distance)),Toast.LENGTH_LONG).show();
        	
        	//Toast.makeText(getActivity(),"Your Location is -\nLat:"+latitude+" \nLong:"+longitude,Toast.LENGTH_LONG).show();
        } 
        
        
        pDialog = new ProgressDialog(getActivity());
        //layoutx = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutx = getActivity().getLayoutInflater();
        
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbWidth = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_width);
        mImageThumbHeight = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_height);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        //mAdapter = new ImageAdapter(getActivity());

        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        //mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbWidth, mImageThumbHeight);        
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        //mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams);
        
        listValue = new ArrayList<RowItem>();
        
        customAdapter = new CustomListViewAdapter(
   				getActivity(),R.layout.fragment_home_list_item,listValue);
        
        
       
        cd = new ConnectionDetector(getActivity().getApplicationContext());
		 
		// Check for internet connection
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(getActivity(), "Internet Connection Error",
               "Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}		
		
		new LoadFeeds().execute();
    }    
  
    
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 	 
    	final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
    	
    	final ImageButton addfeed = (ImageButton)v.findViewById(R.id.add_feed);
		addfeed.setClickable(true);		
		addfeed.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {      	         	    
      	        Intent intentAddFeed = new Intent(getActivity(),AddFeedActivity.class);
				startActivityForResult(intentAddFeed, REQUEST_CODE_ADD_FEED_CLASS);      	    
			}
		});
    	
    	
        final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setAdapter(customAdapter);
        //mGridView.setAdapter(mAdapter);
        
        Log.d(TAG,"I am here!");
        
        mGridView.setOnItemClickListener(this);
        //mGridView.setFocusableInTouchMode(true);
        
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageFetcher.setPauseWork(true);
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            	
            	boolean loadMore = // maybe add a padding 
			            (firstVisibleItem + visibleItemCount >= totalItemCount);
			           
            	Log.d(TAG,"loadMore:"+loadMore); 
			        
			            
            	if(loadMore && !flag_loading)
            	{ 	           
            		flag_loading = true;
			           
            		if(customAdapter != null){				            	
            			Log.d(TAG,"custom adapter not null");
            			addMoreData(); //to do: remove comment	            		  
            		} 
            		else
            		{
            			Log.d(TAG,"custom adapter null");
            		}		            
            	}
            	else
            	{
			        	      	
            	}            	
            }
        });

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (customAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(
                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            
                           
                            
                            if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                customAdapter.setNumColumns(numColumns);
                                customAdapter.setItemHeight(columnWidth);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                                }
                            }
                        }
                    }
                });

        return v;
    }
    
   
    @Override
	 public void onActivityCreated(Bundle savedInstanceState) {
	  super.onActivityCreated(savedInstanceState);	  
	  
   }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    @TargetApi(16)
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {        
    	openDetail(parent, v, position, id);    	
    }

   /* @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                mImageFetcher.clearCache();
                Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @TargetApi(16)
    private void openDetail(AdapterView<?> parent, View v, int position, long id)
    {
    	System.gc();
    	
    	final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
        i.putExtra(ImageDetailActivity.EXTRA_LIST,  listValue);    
       
                
        if (Utils.hasJellyBean()) {
            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
            // show plus the thumbnail image in GridView is cropped. so using
            // makeScaleUpAnimation() instead.
            ActivityOptions options =
                    ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            getActivity().startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }
    }
    
    public void addMoreData()
	{
		new LoadFeeds().execute();		
		Log.d(TAG,"custom Adapter not null!");
		customAdapter.notifyDataSetChanged();	
	}
    
    public float getRandom(float min, float max)
    {
    	return (random.nextFloat() * max) + min;
    }
    
    public void reloadPage(int currPage,String currEventID)
	{
		currentPage = currPage;		
		reloadList();
	}
	
	
	protected void reloadList() {
		currentPage = 0;
    	listValue.clear();	            	
    	customAdapter.notifyDataSetChanged();
	}
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	   super.onActivityResult(requestCode, resultCode, data);
	    
    	Log.d(TAG,"requestCode:"+requestCode);
		Log.d(TAG,"resultCode:"+resultCode);
		
		if(resultCode == RESULT_CODE_SEND_IMAGE)
		{
			Toast.makeText(getActivity(), R.string.feed_added, Toast.LENGTH_LONG).show();
			reloadPage(0,"");
		}else if(requestCode == REQUEST_CODE_FEED_CLASS){//if feed deleted successfully reload the feeds
	    	
	    	if(resultCode == 2)// to do: implement this later
	    	{
	    		Toast.makeText(getActivity(), R.string.feed_deleted, Toast.LENGTH_LONG).show();
	    		
	    	}
	    }else if(requestCode == REQUEST_CODE_ADD_FEED_CLASS)
	    {
	    	Toast.makeText(getActivity(), R.string.feed_added, Toast.LENGTH_LONG).show();
	    	reloadPage(0,"");	    	
	    }
	}
    
    
    public class CustomListViewAdapter extends ArrayAdapter<RowItem> 
    {
		public ImageLoader imageLoader; 
		
		private ViewHolder holder = null;
		private TextView photoId = null;		
		private TextView postDate = null;		
		private ImageView thumb=null;		
		private TextView userName = null;
		private TextView points = null;
		private TextView geo = null;
		private int mItemHeight = 0;
        private int mNumColumns = 0;       
        private GridView.LayoutParams mImageViewLayoutParams;
        private int mActionBarHeight = 0;
        private Context mContext;
       
			   
		
		public CustomListViewAdapter(Context context, int resourceId, List<RowItem> items)
		{   
			super(context, resourceId, items);
			imageLoader=new ImageLoader(context);
			
			
			
			
			mContext = context; 
			 mImageViewLayoutParams = new GridView.LayoutParams(
	                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	            // Calculate ActionBar height
	            TypedValue tv = new TypedValue();
	            if (context.getTheme().resolveAttribute(
	                    android.R.attr.actionBarSize, tv, true)) {
	                mActionBarHeight = TypedValue.complexToDimensionPixelSize(
	                        tv.data, context.getResources().getDisplayMetrics());
	            }
		}
		
		
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{   
			
			final RowItem rowItem= getItem(position);
				
			
			if(convertView == null)
			{				
				convertView = layoutx.inflate(R.layout.fragment_home_list_item, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);				
				Log.d(TAG,"heeeee *********************************");
			}		
			
			// Set empty view with height of ActionBar
            /*convertView.setLayoutParams(new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));*/
			
			holder = (ViewHolder) convertView.getTag();
			
			if(holder == null)
				Log.e(TAG,"holder is null");
						
			postDate = holder.getPostDate();
			postDate.setText(rowItem.postDate);					
				
			userName = holder.getUserName();
			userName.setText(rowItem.username);		
				
			photoId = holder.getPhotoId();
			photoId.setText(rowItem.photoId);	
			
			points = holder.getPoints();
			if(points != null)
				points.setText(String.format("%.2f", rowItem.points));
			
			geo = holder.getGeo();			
			if(geo != null && gpsTracker.canGetLocation()){	
				double distance = gpsTracker.getDistance(gpsTracker.getLatitude(),gpsTracker.getLongitude(),34.770091,-92.336152);       	
				geo.setText(String.valueOf(formatDecimal.format(distance))+" mi");
			}
			// Now handle the ImageView thumbnails
			thumb=holder.getThumb();			
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                  Log.d(TAG,"position ["+position+"]");
                  
                }

              });
            
            //thumb2 = new RecyclingImageView(mContext);           
            //thumb2.setLayoutParams(mImageViewLayoutParams);
			
			if(!rowItem.urlthumb.equals("") && rowItem.urlthumb != null)
			{
				Log.d(TAG, "urlthumb: "+rowItem.urlthumb);
				try
				{
					//imageLoader.DisplayImage(rowItem.thumb , thumb2, Utils.SIZE_THUMB2, false);
					mImageFetcher.loadImage(rowItem.urlsmall, thumb);
				}
				catch(Exception ex)
				{
					Log.e(TAG,ex.getMessage());
				}
					
				thumb.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {	
						openDetail(null, v, position, position);
					}
				});
			}
			else
			{
				try
				{
					//imageLoader.DisplayImage(rowItem.thumb2 , thumb2);
				}
				catch(Exception ex)
				{
					Log.e(TAG,ex.getMessage());
				}
				//holder.getThumb2().setVisibility(View.GONE);
			}				
			
			convertView.setTag(holder);
			//thumb2.setImageURI(Uri.parse(rowItem.thumb2)) ;

			
			
			return convertView;
		}
		
		 public void setItemHeight(int height) {
	            if (height == mItemHeight) {
	                return;
	            }
	            mItemHeight = height;
	            mImageViewLayoutParams =
	                    new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
	            mImageFetcher.setImageSize(height);
	            notifyDataSetChanged();
	        }

	        public void setNumColumns(int numColumns) {
	            mNumColumns = numColumns;
	        }

	        public int getNumColumns() {
	            return mNumColumns;
	        }

		
    }
    
    
    
    /**
	 * Background Async Task to Load all Albums by making http request
	 * */
	class LoadFeeds extends AsyncTask<String, String, Boolean> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			/*pDialog.setMessage("Yükleniyor...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);*/
			//pDialog.show();
		}

		/**
		 * getting Feeds JSON
		 * */
		@Override
		protected Boolean doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("processType", "GETFEEDLIST"));
			//params.add(new BasicNameValuePair("count", Integer.toString(count) ));
			params.add(new BasicNameValuePair("currentPage", Integer.toString(currentPage) ));			
			// getting JSON string from URL
			String json = jsonParser.makeHttpRequest(URL_FEEDS, "GET",params);
				

			// Check your log cat for JSON reponse
			Log.d("Feeds JSON: ", "> " + json);

			try {				
				feeds = new JSONArray(json);
				
				if (feeds != null) {
					// looping through All feeds
					
					if(feeds.length() == 0)
					{
						//getListView().removeFooterView(footerView); 
						
						return true;
					}
					else{
					
						for (int i = 0; i < feeds.length(); i++) {
							
							JSONObject c = feeds.getJSONObject(i);						
						// Storing each json item values in variable
						/*String feedId = c.getString(TAG_FEEDID);						
						String username = "@"+c.getString(TAG_USERNAME);						
						String postdate = c.getString(TAG_POSTDATE);						
						String thumbSrc = TAG_URL+c.getString(TAG_PHYSICALPATHTHUMB)+"/"+c.getString(TAG_NAMETHUMB);
						String thumb2Src = TAG_URL+c.getString(TAG_PHYSICALPATHTHUMB2)+"/"+c.getString(TAG_NAMETHUMB2);
						String thumb1Src = TAG_URL+c.getString(TAG_PHYSICALPATHTHUMB1)+"/"+c.getString(TAG_NAMETHUMB1);
						String userthumb2Src = TAG_URL+c.getString(TAG_USERPHYSICALPATHTHUMB2)+"/"+c.getString(TAG_USERNAMETHUMB2);
						String userthumb1Src = TAG_URL+c.getString(TAG_USERPHYSICALPATHTHUMB1)+"/"+c.getString(TAG_USERNAMETHUMB1);
						String userthumb0Src = TAG_URL+c.getString(TAG_USERPHYSICALPATHTHUMB0)+"/"+c.getString(TAG_USERNAMETHUMB0);
						String fullname = c.getString(TAG_FULLNAME);*/
						
						
						String photoId = c.getString("PKPHOTOID");
						String urllarge = c.getString("URLLARGE");
						String urlmedium = c.getString("URLMEDIUM");
						String urlsmall = c.getString("URLSMALL");
						String urlthumb = c.getString("URLTHUMB");						
						//String username = c.getString("OWNEDBY");
						//String ownedby = c.getString("OWNEDBY");
						String username = "mehmetsen80";
						String ownedby = "memosen80";						
						String postdate = c.getString("POSTDATE");
						String fullname = "";						
						
						Log.d(TAG, "photoId:"+photoId+" postdate:"+postdate);
						
						float points = getRandom(MIN,MAX);
						Log.d(TAG,"points: "+points);
						
						//to do: get this later from db
						double geo =  0.0;
						
						try
				        {
							//rdSection = new RowItem(i,feedId,content,contentTrim,postdate,thumb2Src,fullname,tagname, true);
				            //rdItem = new RowItem(i,feedId,postdate,thumbSrc,thumb2Src,thumb1Src, userthumb0Src, userthumb1Src, userthumb2Src, fullname, username, "", false, points, hashtag);
							rdItem = new RowItem(i,photoId,postdate,urllarge,urlmedium,urlsmall,urlthumb, username, ownedby, false, points, geo);
				        } 
				        catch (ParseException e) 
				        {
				            Log.e(TAG,e.getMessage());
				        }
						
						
						listValue.add(rdItem);
						//listValue.add(rdSection);						
						}
					}
				}else{
					Log.d("Feeds: ", "null");
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return false;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(final Boolean dismiss) {
			// dismiss the dialog after getting all albums
			pDialog.dismiss();
			// updating UI from Background Thread
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */									
					
					if(!dismiss)
					{
						customAdapter.notifyDataSetChanged();					
						flag_loading = false;
						currentPage++;
					}
					else
					{
						
					}
				}
			});

		}

	}	
    

    
    
   
}
