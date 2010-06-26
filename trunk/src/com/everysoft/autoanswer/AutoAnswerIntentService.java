package com.everysoft.autoanswer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

public class AutoAnswerIntentService extends IntentService {

	public AutoAnswerIntentService() {
		super("AutoAnswerIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getBaseContext();
		
		// Load preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		// Let the phone ring for a set delay
		try {
			Thread.sleep(Integer.parseInt(prefs.getString("delay", "2")) * 1000);
		} catch (InterruptedException e) {
			// We don't really care
		}

		// Make sure the phone is still ringing
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
			return;
		}
			
		// Simulate a press of the headset button to pick up the call
		Intent button_down = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		button_down.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(button_down, null);
		Intent button_up = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		button_up.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(button_up, null);
		
		// Enable the speakerphone
		if (prefs.getBoolean("use_speakerphone", false)) {
			enableSpeakerPhone(context);
		}
		return;
	}
	
	private void enableSpeakerPhone(Context context) {
		AudioManager audio_manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audio_manager.setSpeakerphoneOn(true);
	}
}
