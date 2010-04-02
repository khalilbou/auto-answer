package com.everysoft.autoanswer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

public class AutoAnswerReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Load preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		// Check phone state
		String phone_state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		
		if (phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING) && prefs.getBoolean("enabled", false)) {
			
			// Check for contact restrictions
			String which_contacts = prefs.getString("which_contacts", "all");
			if (!which_contacts.equals("all")) {
				int is_starred = isStarred(context, number);
				if (which_contacts.equals("contacts") && is_starred < 0) {
					return;
				}
				else if (which_contacts.equals("starred") && is_starred < 1) {
					return;
				}
			}
			
			// Let the phone ring for approximately two seconds
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// We don't really care
			}
			
			// Simulate a press of the headset button to pick up the call
			Intent new_intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			new_intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
			context.sendOrderedBroadcast(new_intent, null);
			
			// Enable the speakerphone
			if (prefs.getBoolean("use_speakerphone", false)) {
				enableSpeakerPhone(context);
			}
		}
	}
	
	private void enableSpeakerPhone(Context context) {
		AudioManager audio_manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audio_manager.setSpeakerphoneOn(true);
	}
	
	// returns -1 if not in contact list, 0 if not starred, 1 if starred
	private int isStarred(Context context, String number) {
		int starred = -1;
        Cursor c = context.getContentResolver().query(
                Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),
                new String[] {PhoneLookup.STARRED},
                null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                starred = c.getInt(0);
            }
            c.close();
        }
        return starred;
	}
}