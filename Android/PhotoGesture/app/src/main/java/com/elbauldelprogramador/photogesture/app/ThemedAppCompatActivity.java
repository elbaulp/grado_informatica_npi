/*
 * Copyright (c) 2016 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.elbauldelprogramador.photogesture.app;

import com.elbauldelprogramador.photogesture.util.ThemeUtils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ThemedAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
    }
}
