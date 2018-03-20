package com.wooplr.spotlight;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.transition.Slide;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shuame.utils.SLog;
import com.wooplr.spotlight.shape.HighLight;
import com.wooplr.spotlight.shape.NormalLineAnimDrawable;
import com.wooplr.spotlight.target.AnimPoint;
import com.wooplr.spotlight.utils.Utils;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.Synchronized;

/**
 * Created by flying on 10/06/16.
 */

public class SpotlightView extends FrameLayout {
    /**
     * OverLay color
     */
    private int maskColor = 0xCC00B0FF;
    private long introAnimationDuration = 600;
    private long fadingTextDuration = 600;
    private long lineAnimationDuration = 600;
    private List<HighLight> highLights = new ArrayList<>();
    private Paint eraser;
    private Handler handler;
    private Bitmap bitmap;
    private Canvas canvas;
    private int width;
    private int height;
    private boolean dismissOnBackPress;
    private View expandView;
    private int headingTvSize = 24;
    private int headingTvSizeDimenUnit = -1;
    private int headingTvColor = Color.parseColor("#eb273f");
    private int lineStroke;
    private PathEffect lineEffect;
    private int lineAndArcColor = Color.parseColor("#eb273f");
    private Typeface mTypeface = null;
    private int softwareBtnHeight;
    private boolean animEnd = false;
    private boolean showing = false;
    private OnSpotlinghViewTouchEvent onSpotlinghViewTouchEvent = null;

    public void setOnSpotlinghViewTouchEvent(OnSpotlinghViewTouchEvent listener) {
        onSpotlinghViewTouchEvent = listener;
    }

    public SpotlightView(Context context) {
        super(context);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setVisibility(INVISIBLE);
        lineStroke = Utils.dpToPx(4);
        dismissOnBackPress = false;
        handler = new Handler();
        eraser = new Paint();
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (bitmap == null || canvas == null) {
                if (bitmap != null) bitmap.recycle();

                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                this.canvas = new Canvas(bitmap);
            }
            this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            this.canvas.drawColor(maskColor);
            for (HighLight highLight : highLights) {
                highLight.draw(this.canvas, eraser);
            }
            canvas.drawBitmap(bitmap, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the view based on the configuration
     * Reveal is available only for Lollipop and above in other only fadein will work
     * To support reveal in older versions use github.com/ozodrukh/CircularReveal
     *
     * @param activity
     */
    public void show(final Activity activity) {
        if (showing) {
            return;
        }
        showing = true;
        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    startFadeinAnimation(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 100);
    }

    /**
     * Dissmiss view with reverse animation
     */
    private void dismiss() {
        startFadeout();
    }

    private void startFadeinAnimation(final Activity activity) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(introAnimationDuration);
        fadeIn.setFillAfter(true);
        setVisibility(VISIBLE);
        startAnimation(fadeIn);
        //200ml后开始画线动画
        postDelayed(new Runnable() {
            @Override
            public void run() {
                for (HighLight highLight : highLights) {
                    if (!highLight.isOnlyHighLight()) {
                        addPathAnimation(activity, highLight);
                    }
                }
            }
        }, 200);
    }

    private void startFadeout() {
        AlphaAnimation fadeIn = new AlphaAnimation(1.0f, 0.0f);
        fadeIn.setDuration(introAnimationDuration);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new MyAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(GONE);
                removeSpotlightView();
            }
        });

        startAnimation(fadeIn);

    }

    private void addPathAnimation(Activity activity, HighLight highLight) {
        View line = new View(activity);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.width = getViewWidth();
        params.height = getViewHeight();
        params.width = getMeasuredWidth();
        addView(line, params);

        //Line animation
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.STROKE);
//        p.setStrokeJoin(Paint.Join.ROUND);
//        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(lineStroke);
        p.setColor(lineAndArcColor);
        p.setPathEffect(lineEffect);

        NormalLineAnimDrawable animDrawable1 = new NormalLineAnimDrawable(p);
        if (lineAnimationDuration > 0)
            animDrawable1.setLineAnimDuration(lineAnimationDuration);
        if (Build.VERSION.SDK_INT < 16) {
            line.setBackgroundDrawable(animDrawable1);
        } else {
            line.setBackground(animDrawable1);
        }

        List<AnimPoint> animPoints = new ArrayList<>();
        Point start = highLight.getDefaultLineStartPoint();
        final Point end = highLight.getLineEndPoint(start);
        animPoints.add(new AnimPoint(start.x, start.y, end.x, end.y));
        animDrawable1.setPoints(animPoints);

        final TextView headingTv = highLight.createHeadingTextView(activity, end);
        addView(initHeadingTextView(headingTv));

        animDrawable1.playAnim();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(fadingTextDuration);
                fadeIn.setFillAfter(true);
                fadeIn.setAnimationListener(new MyAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        animEnd = true;
                    }
                });
                headingTv.startAnimation(fadeIn);
                headingTv.setVisibility(View.VISIBLE);
                if (expandView != null) {
                    AutoUtils.auto(expandView);
                    addView(expandView);
                    expandView.startAnimation(fadeIn);
                    expandView.setVisibility(View.VISIBLE);
                }
            }
        }, 400);

    }

    private TextView initHeadingTextView(TextView headingTv) {
        if (mTypeface != null)
            headingTv.setTypeface(mTypeface);
        if (headingTvSizeDimenUnit != -1) {
            headingTv.setTextSize(headingTvSizeDimenUnit, headingTvSize);
        } else {
            headingTv.setTextSize(headingTvSize);
        }
        headingTv.setVisibility(View.GONE);
        headingTv.setTextColor(headingTvColor);
        return headingTv;
    }

    public interface OnSpotlinghViewTouchEvent {
        boolean onTouchEvent(MotionEvent event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onSpotlinghViewTouchEvent != null) {
            return onSpotlinghViewTouchEvent.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void enableDismissOnBackPress() {
        setFocusableInTouchMode(true);
        setFocusable(true);
        requestFocus();
    }

    private OnRemoveSpotlightView onRemoveSpotlightView = null;

    /**
     * Remove the spotlight view
     */
    @Synchronized
    public void removeSpotlightView() {
        SLog.d("SpotlightView", "removeSpotlightView");
        ViewParent parent = getParent();
        if (parent != null && showing) {
            try {
                ViewGroup viewGroup = ((ViewGroup) parent);
                int index = viewGroup.indexOfChild(this);
                View childView = viewGroup.getChildAt(index);
                if (childView != null && childView == this)
                    viewGroup.removeViewAt(index);
                if (onRemoveSpotlightView != null) {
                    onRemoveSpotlightView.onRemoveAllView();
                    SLog.d("SpotlightView", "removeSpotlightView success");
                }
            }catch (Exception e){
                SLog.e("SpotlightView",e);
            }
        } else {
            SLog.d("SpotlightView", "removeSpotlightView has null");
        }
        showing = false;
    }

    public void setOnRemoveSpotlightView(OnRemoveSpotlightView onRemoveSpotlightView) {
        this.onRemoveSpotlightView = onRemoveSpotlightView;
    }

    public interface OnRemoveSpotlightView {
        void onRemoveAllView();
    }


    private void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    public void setDismissOnBackPress(boolean dismissOnBackPress) {
        this.dismissOnBackPress = dismissOnBackPress;
    }

    public void setIntroAnimationDuration(long introAnimationDuration) {
        this.introAnimationDuration = introAnimationDuration;
    }

    public void setFadingTextDuration(long fadingTextDuration) {
        this.fadingTextDuration = fadingTextDuration;
    }

    public void addHighLightTargets(List<HighLight> highLights) {
        this.highLights.addAll(highLights);
    }

    public void setHeadingTvSize(int headingTvSize) {
        this.headingTvSize = headingTvSize;
    }

    public void setHeadingTvSize(int dimenUnit, int headingTvSize) {
        this.headingTvSizeDimenUnit = dimenUnit;
        this.headingTvSize = headingTvSize;
    }

    public void setHeadingTvColor(int headingTvColor) {
        this.headingTvColor = headingTvColor;
    }

    public void setLineAnimationDuration(long lineAnimationDuration) {
        this.lineAnimationDuration = lineAnimationDuration;
    }


    public void setExpandView(View view) {
        expandView = view;
    }

    public void setLineAndArcColor(int lineAndArcColor) {
        this.lineAndArcColor = lineAndArcColor;
    }

    public void setLineStroke(int lineStroke) {
        this.lineStroke = lineStroke;
    }

    public void setLineEffect(PathEffect pathEffect) {
        this.lineEffect = pathEffect;
    }

    private void setSoftwareBtnHeight(int px) {
        this.softwareBtnHeight = px;
    }

    public void setTypeface(Typeface typeface) {
        this.mTypeface = typeface;
    }

    public boolean showing() {
        return showing;
    }

    public boolean isAnimEnd() {
        return animEnd;
    }

    public void setConfiguration(SpotlightConfig configuration) {
        if (configuration != null) {
            this.maskColor = configuration.getMaskColor();
            this.introAnimationDuration = configuration.getIntroAnimationDuration();
            this.fadingTextDuration = configuration.getFadingTextDuration();
            this.dismissOnBackPress = configuration.isDismissOnBackpress();
            this.headingTvSize = configuration.getHeadingTvSize();
            this.headingTvSizeDimenUnit = configuration.getHeadingTvSizeDimenUnit();
            this.headingTvColor = configuration.getHeadingTvColor();
            this.lineAnimationDuration = configuration.getLineAnimationDuration();
            this.lineStroke = configuration.getLineStroke();
            this.lineAndArcColor = configuration.getLineAndArcColor();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (dismissOnBackPress) {
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                dismiss();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private int getViewHeight() {
        if (getWidth() > getHeight()) {
            return getHeight();
        } else {
            return (getHeight() - softwareBtnHeight);
        }
    }

    private int getViewWidth() {
        if (getWidth() > getHeight()) {
            return (getWidth() - softwareBtnHeight);
        } else {
            return getWidth();
        }
    }

    private static int getSoftButtonsBarHeight(Activity activity) {
        try {
            // getRealMetrics is only available with API 17 and +
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                DisplayMetrics metrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                if (metrics.heightPixels > metrics.widthPixels) {
                    //Portrait
                    int usableHeight = metrics.heightPixels;
                    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                    int realHeight = metrics.heightPixels;
                    if (realHeight > usableHeight)
                        return realHeight - usableHeight;
                    else
                        return 0;
                } else {
                    //Landscape
                    int usableHeight = metrics.widthPixels;
                    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                    int realHeight = metrics.widthPixels;
                    if (realHeight > usableHeight)
                        return realHeight - usableHeight;
                    else
                        return 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private class MyAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    /**
     * Builder Class
     */
    public static class Builder {
        private SpotlightView spotlightView;
        private Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;
            spotlightView = new SpotlightView(activity);
            spotlightView.setSoftwareBtnHeight(getSoftButtonsBarHeight(activity));
        }

        public Builder maskColor(int maskColor) {
            spotlightView.setMaskColor(maskColor);
            return this;
        }

        public Builder introAnimationDuration(long delayMillis) {
            spotlightView.setIntroAnimationDuration(delayMillis);
            return this;
        }

        public Builder highLight(List<HighLight> highLights) {
            spotlightView.addHighLightTargets(highLights);
            return this;
        }

        public Builder dismissOnBackPress(boolean dismissOnBackPress) {
            spotlightView.setDismissOnBackPress(dismissOnBackPress);
            return this;
        }


        public Builder setTypeface(Typeface typeface) {
            spotlightView.setTypeface(typeface);
            return this;
        }


        public Builder fadeinTextDuration(long fadinTextDuration) {
            spotlightView.setFadingTextDuration(fadinTextDuration);
            return this;
        }

        public Builder headingTvSize(int headingTvSize) {
            spotlightView.setHeadingTvSize(headingTvSize);
            return this;
        }

        public Builder headingTvSize(int dimenUnit, int headingTvSize) {
            spotlightView.setHeadingTvSize(dimenUnit, headingTvSize);
            return this;
        }

        public Builder headingTvColor(int color) {
            spotlightView.setHeadingTvColor(color);
            return this;
        }


        public Builder lineAndArcColor(int color) {
            spotlightView.setLineAndArcColor(color);
            return this;
        }

        public Builder lineAnimDuration(long duration) {
            spotlightView.setLineAnimationDuration(duration);
            return this;
        }

        public Builder lineStroke(int stroke) {
            spotlightView.setLineStroke(Utils.dpToPx(stroke));
            return this;
        }

        public Builder lineEffect(@Nullable PathEffect pathEffect) {
            spotlightView.setLineEffect(pathEffect);
            return this;
        }

        public Builder setConfiguration(SpotlightConfig configuration) {
            spotlightView.setConfiguration(configuration);
            return this;
        }

        public Builder setExpandView(View expandView) {
            spotlightView.setExpandView(expandView);
            return this;
        }

        public SpotlightView build() {
            if (spotlightView.dismissOnBackPress) {
                spotlightView.enableDismissOnBackPress();
            }
            return spotlightView;
        }

        public SpotlightView show() {
            spotlightView.show(activity);
            return spotlightView;
        }

    }
}