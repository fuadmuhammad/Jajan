package com.urbanesia.api.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class OAUTHnesia {
	private static final String BASE_URL = "http://api1.urbanesia.com/";
	public static final int OAUTH_SAFE_ENCODE = 1;
	public static final int OAUTH_NO_SAFE_ENCODE = 0;

	private String CONSUMER_KEY;
	private String CONSUMER_SECRET;
	private String USER_KEY;
	private String USER_SECRET;

	private String API_URI;

	private String SAFE_ENCODE = "0";

	public OAUTHnesia(String cons_key, String cons_secret, int safe_encode) {
		setConsumerKey(cons_key);
		setConsumerSecret(cons_secret);
		if (safe_encode == 1) {
			setSafeEncode("1");
		}
	}

	public String oAuth(String oUri, String post, String get)
			throws InvalidKeyException, NoSuchAlgorithmException,
			ClientProtocolException, IOException {
		setApiUri(oUri);
		// Check POST values, add oauth requirements
		String oPost = "oauth_consumer_key=" + getConsumerKey()
				+ "&oauth_nonce=" + getNonce() + "&oauth_signature_method="
				+ "HMAC-SHA1" + "&oauth_timestamp=" + getTime()
				+ "&oauth_version=" + "1.0";

		if (post.compareTo("") == 0) {
			post = oPost;
		} else {
			post += "&" + oPost;
		}

		// Trigger safe encoding
		if (getSafeEncode().compareTo("1") == 0) {
			post += "&safe_encode=" + Integer.toString(OAUTH_SAFE_ENCODE);
		}

		// Check GET values
		if (get.compareTo("") != 0) {
			String[] g = get.split("&");
			int max = g.length;
			int j = 0;
			String getify = "";
			for (int i = 0; i < max; i++) {
				if (j == 1)
					getify += "&";
				String[] temp = g[i].split("=");
				getify += URLEncoder.encode(temp[0]) + "="
						+ URLEncoder.encode(temp[1]);
				j = 1;
			}
			get = "&" + getify;
		}

		String request = post + get;

		// Encode Request
		String requestify = encodeForOAuth(request);

		// Generate Base Signature
		String base_sig = generateBaseSignature(requestify);

		// Sign to Generate Signature for oAuth
		String signature = sha1(base_sig, getConsumerSecret() + "&");

		// Send to Urbanesia
		String oauth_sig = "?oauth_signature=";
		oauth_sig += URLEncoder.encode(signature);
		String url = BASE_URL + getApiUri() + oauth_sig + get;

		return this.sendRequest(url, post);
	}

	public void setApiUri(String s) {
		this.API_URI = s;
	}

	public String getApiUri() {
		return this.API_URI;
	}

	public void setSafeEncode(String s) {
		this.SAFE_ENCODE = s;
	}

	public String getSafeEncode() {
		return this.SAFE_ENCODE;
	}

	public void setConsumerKey(String s) {
		this.CONSUMER_KEY = s;
	}

	public String getConsumerKey() {
		return this.CONSUMER_KEY;
	}

	public void setConsumerSecret(String s) {
		this.CONSUMER_SECRET = s;
	}

	public String getConsumerSecret() {
		return this.CONSUMER_SECRET;
	}

	public void setUserKey(String s) {
		this.USER_KEY = s;
	}

	public String getUserKey() {
		return this.USER_KEY;
	}

	public void setUserSecret(String s) {
		this.USER_SECRET = s;
	}

	public String getUserSecret() {
		return this.USER_SECRET;
	}

	public String sendRequest(String url, String postString)
			throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();

		if (postString.compareTo("") != 0) {
			// GET & POST Request
			HttpPost post = new HttpPost(url);

			// Create POST List
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			String[] exp = postString.split("&");
			int max = exp.length;
			for (int i = 0; i < max; i++) {
				String[] kv = exp[i].split("=");
				pairs.add(new BasicNameValuePair(kv[0], kv[1]));
			}
			post.setEntity(new UrlEncodedFormEntity(pairs));

			// Get HTTP Response & Parse it
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				// Log.i("HTTPError", "Result of conversion: [" + result + "]");

				instream.close();
				return result;
			}
		}

		return "";
	}

	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private String generateBaseSignature(String s) {
		String sig = "POST&" + URLEncoder.encode(BASE_URL + getApiUri()) + "&"
				+ URLEncoder.encode(s);

		return sig;
	}

	private String encodeForOAuth(String s) {
		// Sort the requests
		String[] par = s.split("&");
		Arrays.sort(par);

		// URL Encode Key and Values
		int max = par.length;
		int j = 0;
		String postify = "";
		for (int i = 0; i < max; i++) {
			if (j == 1)
				postify += "&";
			String[] temp = par[i].split("=");
			postify += temp[0] + "="
					+ temp[1];
			j = 1;
		}

		return postify;
	}

	private String getNonce() {
		return md5(Long.toString(System.currentTimeMillis()));
	}

	private String getTime() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	private String sha1(String s, String keyString)
			throws UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeyException {

		SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"),
				"HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(key);

		byte[] bytes = mac.doFinal(s.getBytes("UTF-8"));

		return new String(Base64.encodeBase64(bytes));
	}

	private String md5(String s) {
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
			e.printStackTrace();
		}
		return "";
	}

}