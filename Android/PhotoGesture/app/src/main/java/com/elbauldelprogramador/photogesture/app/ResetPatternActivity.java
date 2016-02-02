/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.R;
import com.elbauldelprogramador.photogesture.util.PatternLockUtils;
import com.elbauldelprogramador.photogesture.util.ToastUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ResetPatternActivity extends ThemedAppCompatActivity {

    @Bind(R.id.ok_button)
    Button mOkButton;
    @Bind(R.id.cancel_button)
    Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.reset_pattern_activity);
        ButterKnife.bind(this);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatternLockUtils.clearPattern(ResetPatternActivity.this);
                ToastUtils.show(R.string.pattern_reset, ResetPatternActivity.this);
                finish();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
