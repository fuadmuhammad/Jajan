package com.bango.jajan;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.android.panoramagl.PLTexture;
import com.android.panoramagl.PLView;
import com.android.panoramagl.enumeration.PLViewType;
import com.android.panoramagl.structs.PLRange;
import com.flurry.android.FlurryAgent;

public class Panoramic extends PLView {
	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, Evo.FLURRY_API_KEY);

	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onGLContextCreated(GL10 gl) {
		super.onGLContextCreated(gl);
		
		FlurryAgent.onEvent("Panoramic View - NATIVE - Start");
		
		Bundle the = getIntent().getExtras();
		String filePath = the.getString("filePath");
		showPano(filePath);
	}
	
	private void showPano(String filePath) {
		try {
			/*
			 * Important Note: You must edit AndroidManifest.xml and put
			 * android:configChanges="keyboardHidden|orientation" attribute in
			 * activity else you have memory problems
			 */

			// If you want to use setDeviceOrientationEnabled(true), activity
			// orientation only must be portrait. Eg.
			// android:screenOrientation="portrait"
			this.setDeviceOrientationEnabled(false);

			// You can use accelerometer
			this.setAccelerometerEnabled(false);
			this.setAccelerometerLeftRightEnabled(false);
			this.setAccelerometerUpDownEnabled(false);

			// Scrolling and Inertia
			this.setScrollingEnabled(false);
			this.setInertiaEnabled(false);
			this.setShakeResetEnabled(true);

			this.getCamera().setFovRange(PLRange.PLRangeMake(0.0f, 1.0f));
			
			this.setType(PLViewType.PLViewTypeCubeFaces);
			String imgs[] = filePath.split(",");
			int max = imgs.length;
			for(int i=0; i<max; i++) {
				String[] temp = imgs[i].split("_");
				this.addTextureAndRelease(PLTexture.textureWithImage(BitmapFactory.decodeFile(temp[0]+"_f.jpg")));
				this.addTextureAndRelease(PLTexture.textureWithImage(BitmapFactory.decodeFile(temp[0]+"_b.jpg")));
				this.addTextureAndRelease(PLTexture.textureWithImage(BitmapFactory.decodeFile(temp[0]+"_l.jpg")));
				this.addTextureAndRelease(PLTexture.textureWithImage(BitmapFactory.decodeFile(temp[0]+"_r.jpg")));
				this.addTextureAndRelease(PLTexture.textureWithImage(BitmapFactory.decodeFile(temp[0]+"_u.jpg")));
				this.addTextureAndRelease(PLTexture.textureWithImage(BitmapFactory.decodeFile(temp[0]+"_d.jpg")));
			}
		} catch (Throwable ex) {
			FlurryAgent.onEvent(ex.getMessage());
			Toast.makeText(this, "Oops something went wrong, now we know..", Toast.LENGTH_LONG).show();
			finish();
		}
	}
}
