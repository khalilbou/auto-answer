package com.everysoft.autoanswer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AutoAnswerPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}