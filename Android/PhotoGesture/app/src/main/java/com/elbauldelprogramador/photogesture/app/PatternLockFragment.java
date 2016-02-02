/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.R;
import com.elbauldelprogramador.photogesture.preference.ClearPatternPreference;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;


public class PatternLockFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_pattern_lock);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (!ClearPatternPreference.onDisplayPreferenceDialog(this, preference)) {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
