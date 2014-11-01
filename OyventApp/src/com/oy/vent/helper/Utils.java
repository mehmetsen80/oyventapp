package com.oy.vent.helper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class Utils {
	
	public static String TAG = "Utils.java";
	public static int SIZE_VIDEO = 170;
	public static int SIZE_THUMB2 = 170;
	public static int SIZE_PROFILE_THUMB = 60;
	public static int SIZE_THUMB = 80;
	
	
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
    public static boolean isAppInstalled(String uri, Context context) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
    
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        final int heightRatio = Math.round((float) height / (float) reqHeight);
        final int widthRatio = Math.round((float) width / (float) reqWidth);

        // Choose the smallest ratio as inSampleSize value, this will guarantee
        // a final image with both dimensions larger than or equal to the
        // requested height and width.
        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }

    return inSampleSize;
}
    
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
    
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
          Matrix matrix = new Matrix();
          matrix.postRotate(angle);
          return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
    public static String getYouTubeVideoID(String url)
	 {
	   	String videoID = "";
	   	String pattern = "";
	   	Pattern compiledPattern = null;
	   	Matcher matcher = null;
		
		//first get video id from [watch, videos or embed] patterns
		
	   	//i.e String url = "<iframe width=\"420\" height=\"315\" src=\"//www.youtube.com/embed/iGZ2nlkTuEE\" frameborder=\"0\" allowfullscreen></iframe>";
		//i.e String url = "<iframe width='420' height='315' src='//www.youtube.com/embed/iGZ2nlkTuEE\' frameborder='0' allowfullscreen></iframe>";
		//i.e String url = "http://gdata.youtube.com/feeds/api/videos/xTmi7zzUa-M&whatever";
		//i.e String url = "http://www.youtube.com/watch?v=iGZ2nlkTuEE";		
		pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^&?#'\"]*";					
		compiledPattern  = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		matcher = compiledPattern.matcher(url);		
		
		while(matcher.find())
		{
			System.out.println("Video ID matched from [embed watch videos] link:"+matcher.group());
			videoID =  matcher.group();				
			break;
		}				
					
		//if embed, watch and videos pattern empty then get video id from YouTube URl
		if(videoID.isEmpty())
		{	
			System.out.println("Let's try to find from YouTube URL");
			
			//i.e:  String url = "http://youtu.be/iGZ2nlkTuEE"
			//i.e:  String url = "http://youtube.com/iGZ2nlkTuEE";			
			pattern = "(?<=youtu\\.be\\/)[^&#'\"]*|(?<=youtube\\.com\\/)[^&?#'\"]*";			
			compiledPattern  = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			matcher = compiledPattern.matcher(url);
			
			while(matcher.find()) {
				System.out.println("Video ID from YouTube Url:"+matcher.group());
				videoID = matcher.group();
				break;
			}			
		}
	   	
	   	return videoID;
	    	
	  }
    
    public static String getYouTubeThumbNailUrl(String url)
    {
    	String thumbnail = "";
    	String videoID = getYouTubeVideoID(url);
    	Log.d(TAG,"Retreived YouTube Video ID: "+videoID);
		if(!videoID.isEmpty())
		{
			thumbnail = getYouTubeThumbNailUrl(videoID,0);	
			Log.d(TAG,"Retreived YouTube Thumbnail: "+thumbnail);
		}
		
		return thumbnail;
    }
	 
	 //Get YouTube Thumbnail Image Url
	 public static String getYouTubeThumbNailUrl(String videoID,int size)
	 {
		 String thumbnail = "";
		 
		 switch(size)
		 {
		 	case 0: //full size
		 		thumbnail = "http://img.youtube.com/vi/"+videoID+"/0.jpg";
		 		break;
		 	case 1: //small size 1
		 		thumbnail = "http://img.youtube.com/vi/"+videoID+"/1.jpg";
		 		break;
		 	case 2: //small size 2
		 		thumbnail = "http://img.youtube.com/vi/"+videoID+"/2.jpg";
		 		break;
		 	case 3: //small size 3
		 		thumbnail = "http://img.youtube.com/vi/"+videoID+"/3.jpg";
		 		break;
		 	
		 }
		 
		 return thumbnail;
	 }
	 
	 public static String getYouTubeUrl(String code)
	 {
		String url = "";
		
		String videoID = getYouTubeVideoID(code);
	    Log.d(TAG,"Retreived YouTube Video ID: "+videoID);
		if(!videoID.isEmpty())
		{
			url = "http://www.youtube.com/watch?v="+videoID;
			
		}
		
		 return url; 
	 }
	 
	 
}