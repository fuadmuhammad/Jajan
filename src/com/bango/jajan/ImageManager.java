package com.bango.jajan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class ImageManager {
	protected final static String STORAGE_PATH = Environment.getExternalStorageDirectory().toString();
	private String imgUrl;
	private String fn;
	private String basepath = STORAGE_PATH + "/Jajan/";
	
	public ImageManager(String imgUrl) {
		this.setImgUrl(imgUrl);
		fn = md5(imgUrl) + ".jpg";
	}
	
	protected Drawable getImageFromURL(String url) {
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			is.close();
			return d;
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}
	
	public boolean fileExists() {
        return new File(basepath+fn).exists();
	}
	
	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			//e.printStackTrace();
			return s;
		}
	}
	
	public Bitmap getImage() {
		Bitmap b = BitmapFactory.decodeFile(basepath+fn);
		return (b.getWidth() == 0 && b.getHeight() == 0) ? null : b;
	}
	
	public Bitmap saveImage() {
		Bitmap b = ((BitmapDrawable) getImageFromURL(imgUrl)).getBitmap();
		
		if(b.getWidth() == 0 && b.getHeight() == 0) {
			return null;
		}
		
		FileOutputStream out;
		
		// Create dir
		File dir = new File(basepath);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		try {
			File output = new File(dir, fn);
			out = new FileOutputStream(output);
			
			b.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {} catch (IOException e) {}
		
		return b;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}
	
}
