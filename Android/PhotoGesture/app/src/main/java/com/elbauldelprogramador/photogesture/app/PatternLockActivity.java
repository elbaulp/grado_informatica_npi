/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.R;
import com.elbauldelprogramador.photogesture.util.AppUtils;
import com.elbauldelprogramador.photogesture.util.PatternLockUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class PatternLockActivity extends ThemedAppCompatActivity {

    private static final String KEY_CONFIRM_STARTED = "confirm_started";

    private boolean mConfirmStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppUtils.setActionBarDisplayUp(this);

        setContentView(R.layout.pattern_lock_activity);

        if (savedInstanceState != null) {
            mConfirmStarted = savedInstanceState.getBoolean(KEY_CONFIRM_STARTED);
        }
        if (!mConfirmStarted) {
            PatternLockUtils.confirmPatternIfHas(this);
            mConfirmStarted = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_CONFIRM_STARTED, mConfirmStarted);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AppUtils.navigateUp(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (PatternLockUtils.checkConfirmPatternResult(this, requestCode, resultCode)) {
            mConfirmStarted = false;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
