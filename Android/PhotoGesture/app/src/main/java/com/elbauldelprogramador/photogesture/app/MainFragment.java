/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.R;
import com.elbauldelprogramador.photogesture.util.PatternLockUtils;
import com.elbauldelprogramador.photogesture.util.PreferenceContract;
import com.elbauldelprogramador.photogesture.util.PreferenceUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class MainFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    Preference mPf;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_main);

        mPf = findPreference("pl");

        setPreferenceTitle();
    }

    @Override
    public void onResume() {
        super.onResume();

        PreferenceUtils.getPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);

        setPreferenceTitle();
    }

    @Override
    public void onPause() {
        super.onPause();

        PreferenceUtils.getPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceContract.KEY_THEME:
                getActivity().recreate();
        }
    }

    private void setPreferenceTitle() {
        if (PatternLockUtils.hasPattern(getContext())) {
            mPf.setTitle(R.string.pref_title_take_photo);
        } else {
            mPf.setTitle(R.string.pref_title_pattern_lock);
        }
    }
}
