package com.example.myapplication1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedTools {
	private SharedPreferences shared;
	private final String SHARE_KEY = "testshared";

	public SharedTools(Context context) {
		super();
		this.shared = context.getSharedPreferences(SHARE_KEY, Context.MODE_MULTI_PROCESS );
	}

	public boolean getShareBoolean(String key, boolean b) {
		return shared.getBoolean(key, b);
	}

	public int getShareInt(String key, int i) {
		return shared.getInt(key, i);
	}

	public String getShareString(String key, String s) {
		return shared.getString(key, s);
	}

	public void setShareBoolean(String key, boolean value) {
		Editor editor = shared.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void setShareInt(String key, int value) {
		Editor editor = shared.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void setShareString(String key, String value) {
		Editor editor = shared.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public void setShareFloat(String key, float value){
		Editor editor = shared.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
	
	public float getShareFloat(String key, float value){
		return shared.getFloat(key, value);
	}

	/**
	 * 是否包含该Key
	 * @return
	 */
	public boolean contains(String key) {
		return shared.contains(key);
	}
}
