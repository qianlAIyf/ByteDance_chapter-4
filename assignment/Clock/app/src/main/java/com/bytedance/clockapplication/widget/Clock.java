package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            if(message.what == 0){
                invalidate();
            }
        }
    };

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }

    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
        handler.sendEmptyMessageDelayed(0,1000);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor
        Paint clockTextPaint = new Paint();
        clockTextPaint.setAntiAlias(true);
        clockTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        clockTextPaint.setColor(Color.WHITE);
        clockTextPaint.setTextSize(80);
        canvas.translate(mCenterX,mCenterY);
        for (int i = 0; i < 12; i++) {
            String number = 6 + i < 12 ? String.valueOf(6 + i) : (6 + i) > 12
                    ? String.valueOf(i - 6) : "12";
            canvas.save();
            canvas.translate(0, mRadius * 5.5f / 7);
            canvas.rotate(-i * 30);
            canvas.drawText(number, -25, 25, clockTextPaint);
            canvas.restore();
            canvas.rotate(30);
        }
        canvas.translate(-mCenterX,-mCenterY);
//        String[] Number = {"12","01","02","03","04","05","06","07","08","09","10","11"};
//        float dis = clockTextPaint.measureText(Number[1])/2;
//        Paint.FontMetrics fontMetrics = clockTextPaint.getFontMetrics();
//        float fontHeight = fontMetrics.descent - fontMetrics.ascent;
//        float radius = mRadius - mWidth * 0.01f;
//        for(int i = 1; i < Number.length; i ++){
//            float x = (float) (Math.cos(Math.PI - Math.PI / 6 * i) * radius - dis);
//            float y = (float) (Math.sin(Math.PI - Math.PI / 6 * i) * radius + dis);
//            canvas.drawText(Number[i],x,y,clockTextPaint);
//        }

    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor
        Paint hoursNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hoursNeedlePaint.setColor(Color.WHITE);
        hoursNeedlePaint.setStyle(Paint.Style.STROKE);
        hoursNeedlePaint.setStrokeWidth(10f);

        Paint minuteNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minuteNeedlePaint.setColor(Color.WHITE);
        minuteNeedlePaint.setStyle(Paint.Style.STROKE);
        minuteNeedlePaint.setStrokeWidth(10f);

        Paint secondNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondNeedlePaint.setColor(Color.WHITE);
        secondNeedlePaint.setStyle(Paint.Style.STROKE);
        secondNeedlePaint.setStrokeWidth(5f);

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        double hours = calendar.get(Calendar.HOUR) + minute / 12 * 0.2;

        float hourX = (float) Math.cos(Math.toRadians(hours * 30 - 90)) * mRadius * 0.5f;
        float hourY = (float) Math.sin(Math.toRadians(hours * 30 - 90)) * mRadius * 0.5f;
        float minuteX = (float) Math.cos(Math.toRadians(minute * 6 - 90)) * mRadius * 0.7f;
        float minuteY = (float) Math.sin(Math.toRadians(minute * 6 - 90)) * mRadius * 0.7f;
        float secondX = (float) Math.cos(Math.toRadians(second * 6 - 90)) * mRadius * 0.7f;
        float secondY = (float) Math.sin(Math.toRadians(second * 6 - 90)) * mRadius * 0.7f;
        canvas.translate(mCenterX,mCenterY);
        canvas.drawLine(0,0,hourX,hourY,hoursNeedlePaint);
        canvas.drawLine(0,0,minuteX,minuteY,minuteNeedlePaint);
        canvas.drawLine(0,0,secondX,secondY,secondNeedlePaint);
        canvas.translate(-mCenterX,-mCenterY);
        handler.sendEmptyMessageDelayed(0,1000);
    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10f);
        canvas.translate(0,0);
        canvas.drawCircle(mCenterX,mCenterY,10, paint);
    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}