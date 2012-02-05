package com.bango.jajan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.intents.WikitudeARIntent;
import org.openintents.intents.WikitudePOI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.flurry.android.FlurryAgent;
import com.urbanesia.api.android.OAUTHnesia;
import com.urbanesia.beans.SearchResult;

public class Evo extends Activity {
	
	private static List<SearchResult> searchResult = new ArrayList<SearchResult>();
	private List<ListData> l = new ArrayList<ListData>();
	
	private static String coords = "0,0";
	private static String json = "";
	
	private static ListView lv;
	
	protected static final String CONSUMER_KEY = "";      // Get this from Urbanesia
	protected static final String CONSUMER_SECRET = "";   // Get this from Urbanesia
	protected static final String WIKITUDE_API_KEY = "";  // Get this from Wikitude
	protected static final String WIKITUDE_DEV = "";      // Get this from Wikitude
	protected static final String FLURRY_API_KEY = "";    // Get this from Flurry
	
	private WhereAmI whereAmI;
	private ReverseGeo revGeo;
	private DownloadPano downloadPano;
	
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.evo);
        
        lv = (ListView) findViewById(R.id.lv);
        lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				FlurryAgent.onEvent("Click on Business");
				startActivity(new Intent("android.intent.action.VIEW", Uri.parse(searchResult.get(arg2).getBusinessMobileWebURL())));
			}
        });
        
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				Log.v("JAJAN", "Got Location: " + location.getLatitude() + "," + location.getLongitude());
				coords = location.getLatitude() + "," + location.getLongitude();
				
				// Get Location
		        whereAmI = new WhereAmI();
		        whereAmI.execute(lv);
			}

			@Override
			public void onProviderDisabled(String provider) {}

			@Override
			public void onProviderEnabled(String provider) {}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {}
        	
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5000, locationListener);
        
        // Detect Shake
        mSensorListener = new ShakeEventListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
			@Override
			public void onShake() {
				Log.v("JAJAN", "Shake detected..");
				
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(300);
				
				if(whereAmI != null)
					whereAmI.cancel(true);
				
				if(revGeo != null)
					revGeo.cancel(true);
				
				if(downloadPano != null)
					downloadPano.cancel(true);
				
				ListView lv = (ListView) findViewById(R.id.lv);
				lv.setVisibility(View.GONE);
				
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.loadingView);
				rl.setVisibility(View.VISIBLE);
				
				whereAmI = new WhereAmI();
				whereAmI.execute(lv);
			}
        });
	}
	
	public void startARView(View v) {
		WikitudeARIntent wk = new WikitudeARIntent(Evo.this.getApplication(), WIKITUDE_API_KEY, WIKITUDE_DEV);
		
		Collection<WikitudePOI> pois = new ArrayList<WikitudePOI>();
		for(int i=0, max=searchResult.size(); i<max; i++) {
			float lat = Float.valueOf(searchResult.get(i).getBusinessLatitude());
			float lon = Float.valueOf(searchResult.get(i).getBusinessLongitude());
			String name = searchResult.get(i).getBusinessName();
			String desc = (searchResult.get(i).getBusinessParentName().compareTo("") == 0) ? searchResult.get(i).getBusinessAddress1() : searchResult.get(i).getBusinessParentName();
			
			String iconuri = searchResult.get(i).getBusinessPhoto();
			String link = searchResult.get(i).getBusinessMobileWebURL();
			
			final Random randElevation = new Random(); 
			double altitude = randElevation.nextFloat();
			
			WikitudePOI poi = new WikitudePOI(lat, lon, altitude, name, desc);
			
			if(iconuri.compareTo("http://static-10.urbanesia.com/img/default/b/u/default-business_1.api_super_search_v1.jpg") == 0) {
				poi.setIconresource(getResources().getResourceName(R.drawable.biz_default));
			} else {
				poi.setIconuri(iconuri);
			}
			
			poi.setLink(link);
			pois.add(poi);
			
			wk.addPOIs(pois);
			
			try {
				wk.startIntent(Evo.this);
				FlurryAgent.onEvent("AR View - Start");
			} catch(ActivityNotFoundException e) {
				FlurryAgent.onEvent("AR View - Wikitude Not Found");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(
						Evo.this);
				builder.setMessage(
						"Whoops Wikitude will be downloaded for the Augmented Reality view")
						.setCancelable(true)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										String wtude = "market://search?q=pname:com.wikitude";
										Intent wktd = new Intent(
												Intent.ACTION_VIEW, Uri
														.parse(wtude));
										try {
											startActivity(wktd);
											finish();
										} catch(ActivityNotFoundException e1) {
											Toast.makeText(Evo.this, "Cannot launch Android Market, sorry you will need to download Wikitude yourself.", Toast.LENGTH_LONG).show();
										}
									}
								});
				builder.setTitle(Evo.this.getString(R.string.app_name)
						.toString());
				builder.setIcon(R.drawable.icon);
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}
	
	private void changeStatusText(String s) {
		TextView tv = (TextView) findViewById(R.id.statusText);
		tv.setText(s);
	}
	
	private class ReverseGeo extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... c) {
			OAUTHnesia o = new OAUTHnesia(Evo.CONSUMER_KEY, Evo.CONSUMER_SECRET, OAUTHnesia.OAUTH_SAFE_ENCODE);
			
			try {
				final String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+c[0]+"&sensor=true";
				String res = o.sendRequest(url, "");
				
				try {
					String status = new JSONObject(res).get("status").toString();
					if(status.toLowerCase().compareTo("ok") == 0) {
						JSONArray rgeo = new JSONObject(res).getJSONArray("results");
						JSONObject loc = rgeo.getJSONObject(0);
						return loc.getJSONArray("address_components").getJSONObject(0).get("short_name").toString();
					}
				} catch (JSONException e1) {}
				
				res = o.oAuth("get/reverse_geo", "", "ll=" + c[0]);
				
				JSONObject urble = new JSONObject(res).getJSONObject("reverse_geo");
				try {
					JSONArray kel = urble.getJSONArray("kelurahan");
					return kel.getJSONObject(0).get("kelurahan_name").toString();
				} catch(JSONException e1) {
					try {
						JSONArray kel = urble.getJSONArray("kecamatan");
						return kel.getJSONObject(0).get("kecamatan_name").toString();
					} catch(JSONException e2) {
						JSONArray kel = urble.getJSONArray("province");
						return kel.getJSONObject(0).get("province_name").toString();
					}
				}
			} catch (Exception e) {
				return "";
			}
		}
		
		@Override
		protected void onPostExecute(String s) {
			if(s.compareTo("") != 0)
				changeStatusText("Sekitar "+s);
			else
				changeStatusText("Tidak dapat menemukan lokasi anda..");
		}
	}
	
	private class WhereAmI extends AsyncTask<ListView, Void, ListView> {
		@Override
		protected ListView doInBackground(ListView... l) {
			revGeo = new ReverseGeo();
			revGeo.execute(coords);
			
			OAUTHnesia o = new OAUTHnesia(Evo.CONSUMER_KEY, Evo.CONSUMER_SECRET, OAUTHnesia.OAUTH_SAFE_ENCODE);
			try {
				json = o.oAuth(
						"get/super_search", 
						"", 
						"row=100&offset=0&d=2&subcat=Warung,Warteg,Angkringan,Apem,Bakso,Bakwan,Sate,Cakwe,Konro,Nasi+Goreng,Nasi,Jajan,Jajanan,Culinary&ll="+coords
				);
				JSONObject biz = new JSONObject(json).getJSONObject("biz_profile");
				JSONArray ja = biz.names();
				
				for(int i=0, max=ja.length(); i<max; i++) {
					JSONObject attrs = biz.getJSONObject(ja.getString(i)).getJSONObject("attrs");
					SearchResult s = new SearchResult();
					
					s.setBusinessName(attrs.getString("business_name"));
					s.setBusinessUri(attrs.getString("business_uri"));
					s.setBusinessRating(attrs.getString("rating"));
					s.setBusinessParentName(attrs.getString("business_parent_name"));
					s.setBusinessParentUri(attrs.getString("business_parent_uri"));
					s.setBusinessMobileWebURL("http://m.urbanesia.com/profile/" + s.getBusinessUri());
					s.setBusinessReview(attrs.getString("reviews"));
					s.setBusinessReviewCount(attrs.getString("total_reviews"));
					s.setBusinessDistance(attrs.getString("distance_kilometer"));
					s.setBusinessAddress1(attrs.getString("business_address1"));
					s.setBusinessPhoto(attrs.getString("use_photo_full").replace("api_super_search_v1", "biz_thumb"));
					s.setBusinessLatitude(attrs.getString("latitude"));
					s.setBusinessLongitude(attrs.getString("longitude"));
					s.setUrspot(attrs.getString("is_urspot").compareTo("yes") == 0);
					s.setPanoramic(attrs.getString("is_panoramic").compareTo("yes") == 0);
					
					searchResult.add(s);
					//Log.v("JAJAN", "Row " + String.valueOf(i));
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
			return l[0];
		}
		
		@Override
		protected void onPostExecute(ListView lv) {
			loadListView(lv);
		}
	}
	
	private void loadListView(ListView lv) {
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.loadingView);
		rl.setVisibility(View.GONE);
		lv.setVisibility(View.VISIBLE);
		lv.setAdapter(new JajanAdapter());
	}
	
	public void refreshLocation(View v) {
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.loadingView);
		ListView lv = (ListView) findViewById(R.id.lv);
		lv.setVisibility(View.GONE);
		rl.setVisibility(View.VISIBLE);
		new WhereAmI().execute(lv);
	}
	
	private class ListData {
		private int position;
		private View v;
		private Bitmap bitmap;
		
		public ListData(int position, View v) {
			setPosition(position);
			setView(v);
		}
		
		public void setPosition(int position) {
			this.position = position;
		}
		public int getPosition() {
			return position;
		}
		public void setView(View v) {
			this.v = v;
		}
		public View getView() {
			return v;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}
	}
	
	private class JajanAdapter extends BaseAdapter {
		private ImageManager imgMan;

		public int getCount() {
			return searchResult.size();
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return arg0;
		}
		
		private class DownloadImage extends AsyncTask<ListData, Void, ListData> {
			private ImageView img;
			
			@Override
			protected ListData doInBackground(ListData... l) {
				int pos = l[0].getPosition();
				View v = l[0].getView();
				Bitmap ret;
				
				try {
					String imgStr = searchResult.get(pos).getBusinessPhoto();
					Log.v("JAJAN", "Downloading image " + imgStr);
					imgMan = new ImageManager(imgStr);
					
					img = (ImageView) v.findViewById(R.id.bizLogo);
					
					if(imgMan.fileExists()) {
						ret = imgMan.getImage();
					} else {
						ret = imgMan.saveImage();
					}
					
					if(ret == null) {
						return null;
					}
					
					ListData ll = new ListData(pos, v);
					ll.setBitmap(ret);
					
					return ll;
				} catch(Exception e) {
					//e.printStackTrace();
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(ListData ret) {
				if(ret != null && ret.getBitmap() != null) {
					img.setImageBitmap(ret.getBitmap());
					if(l.get(ret.getPosition()).getBitmap() == null) {
						l.get(ret.getPosition()).setBitmap(ret.getBitmap());
					}
				}
			}
			
		}

		public View getView(int position, View v, ViewGroup vg) {
			LayoutInflater inflater = getLayoutInflater();
			
			v = inflater.inflate((position % 2 == 0) ? R.layout.row_even : R.layout.row_odd, vg, false);
			
			final SearchResult s = searchResult.get(position);
			
			TextView tv = (TextView) v.findViewById(R.id.bizName);
			tv.setText(Html.fromHtml(s.getBusinessName()).toString());
			
			tv = (TextView) v.findViewById(R.id.bizAddr);
			tv.setText(s.getBusinessParentName().compareTo("") == 0 ? s.getBusinessAddress1() : s.getBusinessParentName());
			
			tv = (TextView) v.findViewById(R.id.bizReviewCount);
			tv.setText(s.getBusinessReviewCount().compareTo("") == 0 ? "No reviews yet" : s.getBusinessReviewCount() + " Reviews");
			
			tv = (TextView) v.findViewById(R.id.bizReview);
			String t = (s.getBusinessReview().length() > 125) ? s.getBusinessReview().substring(0, 125) : s.getBusinessReview();
			tv.setText(s.getBusinessReview().compareTo("") == 0 ? "" : "\"" + Html.fromHtml(t).toString() + "..\"");
			
			// Lazy load images
			try {
				ListData tmp = l.get(position);
				
				ImageView img = (ImageView) v.findViewById(R.id.bizLogo);
				img.setImageBitmap(tmp.getBitmap());
			} catch(IndexOutOfBoundsException e) {
				ListData lst = new ListData(position, v);
				l.add(lst);
				new DownloadImage().execute(l.get(position));
			}
			
			// Urspot or 360?
			if(s.isPanoramic()) {
				RelativeLayout b = (RelativeLayout) v.findViewById(R.id.badges);
				b.setVisibility(View.VISIBLE);
				b.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						downloadPano = new DownloadPano();
						downloadPano.execute(s.getBusinessUri());
					}
				});
				
				if(s.isPanoramic()) {
					tv = (TextView) v.findViewById(R.id.isPanoramic);
					tv.setVisibility(View.VISIBLE);
				}
				
				b = (RelativeLayout) v.findViewById(R.id.alphaDiv);
				b.setVisibility(View.VISIBLE);
			}
			
			return v;
		}
		
	}
	
	private class DownloadPano extends AsyncTask<String, Void, String> {
		private ProgressDialog dlg;
		protected final String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
		private Bitmap bm;
		
		@Override
		protected void onPreExecute() {
			dlg = new ProgressDialog(Evo.this);
			dlg.setTitle(R.string.app_name);
			dlg.setIcon(R.drawable.icon);
			dlg.setMessage("Downloading..");
			dlg.show();
		}
		
		@Override
		protected String doInBackground(String... s) {
			String ret = "";
			
			OAUTHnesia o = new OAUTHnesia(Evo.CONSUMER_KEY, Evo.CONSUMER_SECRET, OAUTHnesia.OAUTH_SAFE_ENCODE);
			try {
				String res = o.oAuth("get/pano_images", "", "business_uri="+s[0]);
				
				JSONArray pano = new JSONObject(res).getJSONArray("pano_images").getJSONArray(0);
				for(int i=0, max=pano.length(); i<max; i++) {
					String name = pano.getString(i).substring(pano.getString(i).lastIndexOf("/") + 1);
					
					ret += SaveImage(pano.getString(i), name);
					if(i < (max-1))
						ret += ",";
				}
			} catch (Exception e) {}
			
			return ret;
		}
		
		@Override
		protected void onPostExecute(String f) {
			dlg.dismiss();
			
			if(f.compareTo("") != 0) {
				Intent i = new Intent(Evo.this, Panoramic.class);
				i.putExtra("filePath", f);
				startActivity(i);
			} else {
				Toast.makeText(Evo.this, "Gagal memuat Panoramic..", Toast.LENGTH_LONG).show();
			}
		}
		
		private Bitmap LoadImage(String URL) {
			Bitmap bitmap = null;
			InputStream in = null;
			BitmapFactory.Options bmOptions;
			bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;
			try {
				in = OpenHttpConnection(URL);
				bitmap = BitmapFactory.decodeStream(in, null, bmOptions);
				in.close();
			} catch (Exception e1) {
			}
			return bitmap;
		}
		
		private InputStream OpenHttpConnection(String strURL) throws IOException {
			InputStream inputStream = null;
			URL url = new URL(strURL);
			URLConnection conn = url.openConnection();

			try {
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setRequestMethod("GET");
				httpConn.connect();

				if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					inputStream = httpConn.getInputStream();
				}
			} catch (Exception ex) {
			}
			return inputStream;
		}
		
		private String SaveImage(String image, String name) {
			OutputStream outStream = null;
			String path = extStorageDirectory + "/jajan/";
			File t = new File(path);
			t.mkdirs();
			
			if (image.length() > 0) {
				File file = new File(path, name);
				if (file.exists()) {
					Log.i("file exist", "ada");
				} else {
					//file.mkdirs();
					bm = LoadImage(image);
					File f = new File(path);
					f.mkdirs();
					f = new File(path, name);					
					try {
						outStream = new FileOutputStream(f);
						bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
						outStream.flush();
						outStream.close();
					} catch (FileNotFoundException e1) {
						Log.e("ImageManager", "Giving up saving image to SD Card..");
					} catch (IOException e2) {
						Log.e("ImageManager", "Giving up saving image to SD Card..");
					}
				}
			}
			
			return path + "/" + name;
		}
		
	}
	
	public void onStart() {
	   super.onStart();
	   FlurryAgent.onStartSession(this, FLURRY_API_KEY);
	}
	
	public void onStop() {
	   mSensorManager.unregisterListener(mSensorListener);
	   FlurryAgent.onEndSession(this);
	   super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(mSensorListener,
		        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		        SensorManager.SENSOR_DELAY_UI);
	}
	
}
