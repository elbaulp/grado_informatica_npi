/*
 * Copyright 2016 Alejandro Alcalde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elbauldelprogramador.com.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by:
 *
 * Alejandro Alcalde (elbauldelprogramador.com)
 * Cristina Heredia
 *
 * on 1/24/16.
 *
 * This file is part of BrujulaCompass
 *
 * This class is intended to show the user an arrow to where he/she needs to head based on the given
 * voice instruction
 */
public class UserDirectionView extends ImageView {

    private float mDirection;
    private Drawable arrow;

    public UserDirectionView(Context context) {
        super(context);
        mDirection = 0.0f;
        arrow = null;
    }

    public UserDirectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDirection = 0.0f;
        arrow = null;
    }

    public UserDirectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDirection = 0.0f;
        arrow = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (arrow == null) {
            arrow = getDrawable();
            arrow.setBounds(0, 0, getWidth(), getHeight());
        }

        canvas.save();
        canvas.rotate(mDirection, getWidth() / 2, getHeight() / 2);
        arrow.draw(canvas);
        canvas.restore();
    }

    public void updateDirection(float direction) {
        mDirection = direction;
        invalidate();
    }

}
