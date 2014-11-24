package com.example.bbvafoursquare;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.os.AsyncTask;

import android.widget.ProgressBar;

public class MainActivity extends FragmentActivity implements LocationListener {
		
	public final static String apiURL = "https://apis.bbvabancomer.com/datathon/tiles/";	
	public static String[] payments_per_tile = new String[150]; // store num_payments of each tile 
	public static String winner_lati = "19.43"; // top_1 result lati found by function find_top_1()
	public static String winner_longi = "-99.205"; // top_1 result longi found by function find_top_1()
	public static int index=0; // top 1 result index in payments_per_tile array
	public static int winner_index=0; // top 1 result index in payments_per_tile array

	private ProgressBar spinner;
    boolean mIsSubmitClicked = false;
    private GoogleMap mMap;
    private LocationManager locationManager;    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.lati);
		// Get the string array
		//String[] queries = {"auto", "car", "gas", "tow", "wash"};
		// Create the adapter and set it to the AutoCompleteTextView 
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, queries);
		//textView.setAdapter(adapter);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initUi();
		String[] queries = {"find nearby","auto", "car", "gas", "tow", "wash", "mechanics", "barsandrestaurants", "bars", "fastfood", "restaurant", "beauty", "beautyclinics", "cosmetics", "beautysalon", "book", "books", "constructionmaterials", "diy", "materials", "paint", "education", "playschool", "education_sub", "fashion", "fashionaccessories", "fashionwomen", "fashionkids", "fashiongeneral", "uniforms", "food", "foodstore", "fishandbutchers", "sweets", "bakery", "health", "ambulance", "healthequipment", "doctors", "pharmacy", "hospital", "lab", "opticians", "home", "homeaccessories", "antiques", "florists", "photo", "gardening", "haberdashery", "homefurniture", "goods", "homeservices", "tobacconists", "hyper", "mall", "jewelry", "jewelry_sub", "leisure", "leisuretime", "billiards", "bowling", "bet", "theatre", "art", "boardgame", "leisureothers", "videorental", "music", "musicbands", "disc", "musicalinstrument", "office", "officeaccessories", "officeequipment", "officesfurniture", "officeservices", "others", "others_sub", "pet", "pets", "services", "warehouse", "architects", "realestate", "cable", "pawnshops", "consulting", "accounting", "contractor", "tempagency", "finance", "fumigate", "funeral", "lawyer", "cleaning", "moving", "otherservices", "designprogramming", "publicity", "repair", "secretary", "telephony", "drycleaner", "shoes", "shoes_sub", "sport", "sportsaccessories", "bikes", "sportclub", "sportcloths", "tech", "tech_sub", "travel", "airline", "travelagency", "hotel", "carrental", "transport"};
        Spinner spinner1 = (Spinner) findViewById(R.id.lati);
        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, queries);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner1.setAdapter(spinnerArrayAdapter);
	}
	
	private void initUi() {
		spinner = (ProgressBar)findViewById(R.id.progressBar1);
	    spinner.setVisibility(View.GONE);
	    Button proc = (Button) findViewById(R.id.process_button);
        proc.setVisibility(View.GONE);
        setUpMapIfNeeded();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.MIN_TIME, Constants.MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER               
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private int winner_payments_per_tile()
	{	
		int winner=0;
		winner_index=0;
		for (int i = 0; i < payments_per_tile.length; i++) {
		  //Log.d("product is ",payments_per_tile[i]);		
			if(payments_per_tile[i] != null && (Integer.parseInt(payments_per_tile[i]) > winner) ){
				winner= Integer.parseInt(payments_per_tile[i]);
				winner_index=i;
			}
		}
		return winner_index;
	}
	
	
    class GetTopOne extends android.os.AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		setProgressBarIndeterminateVisibility(true);
    		Button sub = (Button) findViewById(R.id.submit_button);
    		sub.setEnabled(false);
    		Button proc = (Button) findViewById(R.id.process_button);
    		spinner.setVisibility(View.VISIBLE);
    		mIsSubmitClicked = true;
    	}
        
        protected Void doInBackground(Void... params) {        	
            Spinner latiSpinnerText = (Spinner) findViewById(R.id.lati);
            String merchant_type = latiSpinnerText.getSelectedItem().toString(); // user selection
                                    
            //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();        
            //StrictMode.setThreadPolicy(policy);
            index = 0; // very important to make the back button work.
                            
            String[] lati_array = {"19.43","19.44","19.435","19.395","19.43","19.37","19.43",
            						"19.435","19.425","19.445",  "19.435","19.305","19.37","19.385",
            						"19.44","19.44","19.485","19.48","19.395","41.748"}; // "19.375","19.425","19.365","19.43","19.43","19.43","19.395","19.345","19.385","19.34",  "19.42","19.445","19.405","19.29","19.3","19.29","19.38","19.285","19.35","19.43",  "19.445","19.39","19.41","18.895","19.39","19.435","19.375","19.285","19.35","41.778",  "19.385","19.395","19.38","19.38","19.345","19.36","20.675","19.43","19.37","19.435",  "19.425","25.635","19.47","19.545","19.425","19.38","41.778","19.365","20.675","19.365",  "19.37","19.375","19.415","19.435","19.395","19.405","25.645","19.43","19.345","19.505",  "19.49","25.635","19.355","19.595","25.735","19.285","19.42","19.445","19.37","19.35",  "19.435","19.435","19.305","19.33","41.717","19.355","19.295","19.455","19.42","19.44"};
            String[] longi_array = {"-99.205","-99.18","-99.135","-99.17","-99.18","-99.17",
            						"-99.27","-99.165","-99.18","-99.15","-99.2","-99.215","-99.19","-99.165",
            						"-99.095","-99.21","-99.205","-99.13","-99.12","-99.155"}; // "-99.17","-99.18","-99.17","-99.26","-99.135","-99.16","-99.2","-99.175","-99.18","-99.24",	"-99.205","-99.17","-99.15","-99.155","-99.14","-99.115","-99.645","-99.17","-99.135","-99.19",	"-99.19","-99.14","-99.255","-99.17","-99.12","-99.175","-99.165","-99.165","-99.175","-99.145",  "-99.2","-99.255","-99.05","-99.155","-99.14","-99.275","-99.29","-103.41","-99.195","-99.18",  "-99.14","-99.165","-100.31","-99.175","-99.14","-99.16","-99.175","-99.195","-99.165","-103.345",  "-99.19","-99.16","-99.265","-99.165","-99.21","-99.165","-99.17","-100.29","-99.21","-99.19",  "-99.155","-99.135","-100.28","-99.1","-99.19","-100.4","-99.14","-99.105","-99.22","-99.125",  "-99.13","-99.2","-99.185","-99.2","-99.215","-99.21","-99.185","-99.105","-99.18","-99.165"};

            //String[] lati_array = {"19.435","19.44","19.435"};//"19.395","19.43","19.37","19.43","19.435","19.425","19.445",  "19.435","19.305","19.37","19.385","19.44","19.44","19.485","19.48","19.395","41.748",  "19.375","19.425","19.365","19.43","19.43","19.43","19.395","19.345","19.385","19.34",  "19.42","19.445","19.405","19.29","19.3","19.29","19.38","19.285","19.35","19.43",  "19.445","19.39","19.41","18.895","19.39","19.435","19.375","19.285","19.35","41.778",  "19.385","19.395","19.38","19.38","19.345","19.36","20.675","19.43","19.37","19.435",  "19.425","25.635","19.47","19.545","19.425","19.38","41.778","19.365","20.675","19.365",  "19.37","19.375","19.415","19.435","19.395","19.405","25.645","19.43","19.345","19.505",  "19.49","25.635","19.355","19.595","25.735","19.285","19.42","19.445","19.37","19.35",  "19.435","19.435","19.305","19.33","41.717","19.355","19.295","19.455","19.42","19.44"};
            //String[] longi_array = {"-99.18","-99.18","-99.135"};//"-99.17","-99.18","-99.17","-99.27","-99.165","-99.18","-99.15",  "-99.2","-99.215","-99.19","-99.165","-99.095","-99.21","-99.205","-99.13","-99.12","-99.155", "-99.17","-99.18","-99.17","-99.26","-99.135","-99.16","-99.2","-99.175","-99.18","-99.24",	"-99.205","-99.17","-99.15","-99.155","-99.14","-99.115","-99.645","-99.17","-99.135","-99.19",	"-99.19","-99.14","-99.255","-99.17","-99.12","-99.175","-99.165","-99.165","-99.175","-99.145",  "-99.2","-99.255","-99.05","-99.155","-99.14","-99.275","-99.29","-103.41","-99.195","-99.18",  "-99.14","-99.165","-100.31","-99.175","-99.14","-99.16","-99.175","-99.195","-99.165","-103.345",  "-99.19","-99.16","-99.265","-99.165","-99.21","-99.165","-99.17","-100.29","-99.21","-99.19",  "-99.155","-99.135","-100.28","-99.1","-99.19","-100.4","-99.14","-99.105","-99.22","-99.125",  "-99.13","-99.2","-99.185","-99.2","-99.215","-99.21","-99.185","-99.105","-99.18","-99.165"};
        	String lati;        
        	String longi;   
        	if( merchant_type != null && !merchant_type.isEmpty()) {
	            for (int i = 0; i < lati_array.length; i++) {
	            	lati = lati_array[i];        
	            	longi = longi_array[i];   
	            	// TODO change auto to merchant_type variable.
	            	// query num_payments for this tile for this merchant type and latest month.
	            	String urlString = apiURL + lati + "/" + longi + "/basic_stats?date_min=20140301&date_max=20140331&group_by=month&category=mx_"+merchant_type;
	            		//new CallAPI().execute(urlString);        		
	            	CallAPI_execute(urlString);
	            }
            }
              
            //int winner=0;
            int winner_index = winner_payments_per_tile();
            // top_1 result to be marked on map
            winner_lati=lati_array[winner_index];
            winner_longi=longi_array[winner_index];
    //        spinner.setVisibility(View.GONE);
            myonPostExecute(winner_lati,winner_longi,winner_index);

			return null;
        	
        }
        @Override
        protected void onPostExecute(Void result) {
        	// TODO Auto-generated method stub
        	super.onPostExecute(result);
        	Button sub = (Button) findViewById(R.id.submit_button);
    		sub.setEnabled(true);
            setProgressBarIndeterminateVisibility(false);
        }
    }

    

    // This is the method that is called when the submit button is clicked
    public void find_top_1(View view) {
    	GetTopOne asyncGetTopOne = (GetTopOne) new GetTopOne();
    	asyncGetTopOne.execute();
    } 
            
    private void parseXML( XmlPullParser parser ) throws XmlPullParserException, IOException {    			
        int eventType = parser.getEventType();
        String currentProduct = null;        
        // in case the query fails for some reason 
        payments_per_tile[index]="0";
        int flag=0;
        
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:                	
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("item")){
                        currentProduct = new String();
                    } else if (currentProduct != null){
                        if (name.equalsIgnoreCase("num_payments")){
                            currentProduct = parser.nextText();
                        } 
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("item") && currentProduct != null){
                    	payments_per_tile[index]=currentProduct;
                    	index++;
                    	flag=1;
                    	//Log.d("product is",currentProduct);
                    } 
            } // end switch
            eventType = parser.next();
        }  // end while
        
        // in case the END_TAG is not executed, still increment index
        if(flag==0) index++;
   } // end parseXML

    /*private class CallAPI extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params)  {*/
    	protected String CallAPI_execute(String params)  {
            //API end point to make a call
        	//android.os.Debug.waitForDebugger();
        	String url=params;
        	InputStream in = null;
            String result = null;

            try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            //Setting header parameters
            request.addHeader("Accept", "text/xml");
            request.addHeader("Content-Type", "text/xml");
            request.addHeader("Authorization", "Basic YXBwLmJidmEuc3Jnb3ZpbmRhMTozYTgxNzk4MmViZTBkNDQxNmE0MWUxNmE2ZjNmNzllYTRkZThhNzM0");
            //Executing the call
            HttpResponse httpResponse = httpclient.execute(request);
            int responseCode = httpResponse.getStatusLine().getStatusCode();           
            //Log.d("rammessage",Integer.toString(responseCode));
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
             in = httpResponse.getEntity().getContent();
	            // Parse XML
	            XmlPullParserFactory pullParserFactory;         
	            try {
	              pullParserFactory = XmlPullParserFactory.newInstance();
	              XmlPullParser parser = pullParserFactory.newPullParser();         
	              parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	              parser.setInput(in, null);
	              parseXML(parser);
	            } catch (XmlPullParserException e) {
	              e.printStackTrace();
	            } catch (IOException e) {
	              e.printStackTrace();
	            }
            }
            else {
            	payments_per_tile[index++]="0";
            }                                 
            } // end try
            catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            
            return result;
          } // end function

	protected void myonPostExecute(String winner_lati, String winner_longi,
			Integer winner_index) {
		// android.os.Debug.waitForDebugger();
		Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
		intent.putExtra("winner_lati", winner_lati);
		intent.putExtra("winner_longi", winner_longi);
		startActivity(intent);
	}

	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
        	mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();            
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        LatLng markerLoc = new LatLng(19.43, -99.205);

        mMap.addMarker(new MarkerOptions()
        .position(markerLoc)                                                                        // at the location you needed
        .title("my location")                                                                     // with a title you needed
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        CameraUpdate cameraUpdate3 = CameraUpdateFactory.newLatLngZoom(markerLoc, 8);
        mMap.moveCamera(cameraUpdate3);              
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(19.43, -99.205);
				
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

    //} // end CallAPI       
}
