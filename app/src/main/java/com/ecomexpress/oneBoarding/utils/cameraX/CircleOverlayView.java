package com.ecomexpress.oneBoarding.utils.cameraX;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.ecomexpress.oneBoarding.R;

public class CircleOverlayView extends View {
    Context mContext;
    Paint clearPaint;
    Paint textPaint;
    PorterDuffXfermode porterDuffXfermode;
    public CircleOverlayView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CircleOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CircleOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        clearPaint = new Paint();
        textPaint = new Paint();
        porterDuffXfermode=new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the black background
        canvas.drawColor(Color.argb(150, 0, 0, 0), PorterDuff.Mode.SRC_OVER);

        // Draw a transparent oval in the middle of the view
        int height = getHeight();
// Set the paint to clear mode for transparent areas
        clearPaint.setColor(ContextCompat.getColor(mContext,R.color.transparent));
        clearPaint.setXfermode(porterDuffXfermode);
        RectF mDrawableRect = calculateBounds();
        float mDrawableRadius = Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f);
        canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius-50,  clearPaint);
//        canvas.drawOval(centerX - radius + 40, centerY - radius, centerX + radius - 40, centerY + radius, mBorderPaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        Typeface typeFace = ResourcesCompat.getFont(mContext, R.font.roboto_bold);
        textPaint.setTypeface(typeFace);
        textPaint.setColor(ContextCompat.getColor(mContext,R.color.white));
        textPaint.setTextSize(36);
        int xPos = (getWidth() / 2);
        int yPos = getHeight() / 6;
        canvas.drawText("Take a Selfie", xPos, yPos, textPaint);

        int yPos2 = height - (height/6);
        canvas.drawText("Keep your face within the circle",xPos,yPos2,textPaint);


    }

    private RectF calculateBounds() {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        int sideLength = Math.min(availableWidth, availableHeight);

        float left = getPaddingLeft() + (availableWidth - sideLength) / 2f;
        float top = getPaddingTop() + (availableHeight - sideLength) / 2f;

        return new RectF(left, top, left + sideLength, top + sideLength);
    }
}