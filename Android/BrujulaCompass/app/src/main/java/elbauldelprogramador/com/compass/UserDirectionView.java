package elbauldelprogramador.com.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Alejandro Alcalde (elbauldelprogramador.com) on 1/24/16.
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
