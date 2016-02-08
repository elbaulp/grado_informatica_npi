/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.camera.MakePhotoActivity;
import com.elbauldelprogramador.photogesture.util.PatternLockUtils;
import com.elbauldelprogramador.photogesture.util.PreferenceContract;
import com.elbauldelprogramador.photogesture.util.PreferenceUtils;
import com.elbauldelprogramador.photogesture.util.ThemeUtils;

import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import me.zhanghai.android.patternlock.PatternView;


public class ConfirmPatternActivity extends me.zhanghai.android.patternlock.ConfirmPatternActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

    }

    @Override
    protected boolean isStealthModeEnabled() {
        return !PreferenceUtils.getBoolean(PreferenceContract.KEY_PATTERN_VISIBLE,
                PreferenceContract.DEFAULT_PATTERN_VISIBLE, this);
    }

    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        boolean isCorrect = PatternLockUtils.isPatternCorrect(pattern, this);
        if (isCorrect) {
            startActivity(new Intent(this, MakePhotoActivity.class));
        }

        return isCorrect;
    }

    @Override
    protected void onForgotPassword() {

        startActivity(new Intent(this, ResetPatternActivity.class));

        // Finish with RESULT_FORGOT_PASSWORD.
        super.onForgotPassword();
    }
}
