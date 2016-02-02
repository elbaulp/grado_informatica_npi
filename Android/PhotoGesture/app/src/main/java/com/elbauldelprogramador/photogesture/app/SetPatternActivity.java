/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.util.AppUtils;
import com.elbauldelprogramador.photogesture.util.PatternLockUtils;
import com.elbauldelprogramador.photogesture.util.ThemeUtils;

import android.os.Bundle;
import android.view.MenuItem;

import java.util.List;

import me.zhanghai.android.patternlock.PatternView;

public class SetPatternActivity extends me.zhanghai.android.patternlock.SetPatternActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

        AppUtils.setActionBarDisplayUp(this);
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
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);
    }
}
