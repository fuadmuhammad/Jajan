package com.urbanesia.api.android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class CellTowerLocation {
	public boolean isInit = false;
	protected static final String INJECT_LOC = "-6.2764597575843,106.82021759985";
	
	public CellTowerLocation() {
		isInit = true;
	}
	
	public static String getLocation(Context ctx) throws IOException {
		if("generic".equals(Build.BRAND)) {
			return INJECT_LOC;
		}
		
		try {
			TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
	    	GsmCellLocation gsmLoc = (GsmCellLocation) tm.getCellLocation();
	    	int cellID = gsmLoc.getCid();
	    	int lac = gsmLoc.getLac();
			
			String urlString = "http://www.google.com/glm/mmap";         
			
			URL url = new URL(urlString); 
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) conn;        
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true); 
			httpConn.setDoInput(true);
			httpConn.connect(); 
			
			OutputStream outputStream = httpConn.getOutputStream();
			WriteData(outputStream, cellID, lac);       
			
			InputStream inputStream = httpConn.getInputStream();  
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			
			dataInputStream.readShort();
			dataInputStream.readByte();
			int code = dataInputStream.readInt();
			if (code == 0) {
			    double lat = (double) dataInputStream.readInt() / 1000000D;
			    double lng = (double) dataInputStream.readInt() / 1000000D;
			    dataInputStream.readInt();
			    dataInputStream.readInt();
			    dataInputStream.readUTF();
			    
			    String ret = Double.toString(lat) + "," + Double.toString(lng);
			    //Log.v("JAJAN", ret);
			    return ret;
			}
			else {        	
				return "0,0";
			}
		} catch(NullPointerException e) {
			return "0,0";
		}
    }
	
	private static void WriteData(OutputStream out, int cellID, int lac) 
    	throws IOException {    	
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(3);
        dataOutputStream.writeUTF("");

        dataOutputStream.writeInt(cellID);  
        dataOutputStream.writeInt(lac);     

        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();    	
    }
}