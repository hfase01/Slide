package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.text.style.ReplacementSpan;

import com.devspark.robototextview.util.RobotoTypefaceManager;

/**
 * Created by carlo_000 on 3/11/2016.
 */
public class RoundedBackgroundSpan extends ReplacementSpan {

    private static int CORNER_RADIUS = 8;
    private int backgroundColor = 0;
    private int textColor = 0;
    boolean half;
    Context c;

    public RoundedBackgroundSpan(Context context, @ColorRes int textColor, @ColorRes int backgroundColor, boolean half) {
        super();
        this.backgroundColor = context.getResources().getColor(backgroundColor);
        this.textColor = context.getResources().getColor(textColor);
        this.half = half;
        this.c = context;
    }

    public RoundedBackgroundSpan(@ColorInt int textColor, @ColorInt int backgroundColor, boolean half, Context context) {
        super();
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.half = half;
        this.c = context;
    }


    @Override
    public void draw(Canvas canvas, CharSequence oldText, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        int offset = 0;
        if (half) {
            offset = (bottom - top) / 6;
        }

        paint.setTypeface(RobotoTypefaceManager.obtainTypeface(c, RobotoTypefaceManager.Typeface.ROBOTO_CONDENSED_BOLD));
        if (half) {
            paint.setTextSize(paint.getTextSize() / 2);
        }
        RectF rect = new RectF(x, top + offset, x + measureText(paint, oldText, start, end), bottom - offset);
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
        paint.setColor(textColor);
        float baseLine = -paint.ascent();
        canvas.drawText(oldText, start, end, x, rect.bottom - ((rect.bottom - rect.top - baseLine) / 2), paint); //center the text in the parent span
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        paint.setTypeface(RobotoTypefaceManager.obtainTypeface(c, RobotoTypefaceManager.Typeface.ROBOTO_CONDENSED_BOLD));
        int size = Math.round(paint.measureText(text, start, end));
        if (half) {
            return size / 2;
        }
        return size;
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}