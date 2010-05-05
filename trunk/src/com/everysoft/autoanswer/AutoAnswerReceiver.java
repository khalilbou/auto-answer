package com.everysoft.autoanswer;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

public class AutoAnswerReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		// Load preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		// Check phone state
		String phone_state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

		if (phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING) && prefs.getBoolean("enabled", false)) {
			// Check for headset restriction
			if (prefs.getBoolean("headset_only", false)) {
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				Set<BluetoothDevice> bdevices = adapter.getBondedDevices();
				boolean headset_found = false;
				for (BluetoothDevice device : bdevices) {
					//if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE) {
					if (device.getBluetoothClass().getDeviceClass() == 1036) {
						headset_found = true;
					}
				}
				if (!headset_found) return;
			}

			// Check for "second call" restriction
			if (prefs.getBoolean("no_second_call", false)) {
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				if (am.getMode() == AudioManager.MODE_IN_CALL) {
					return;
				}
			}			

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

			// Call a service, since this could take a few seconds
			context.startService(new Intent(context, AutoAnswerIntentService.class));
		}		
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
