/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.R;
import com.elbauldelprogramador.photogesture.util.ThemeUtils;

import android.os.Bundle;

public class MainActivity extends ThemedAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
    }
}
