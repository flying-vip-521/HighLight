package com.wooplr.spotlight.shape;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shuame.utils.SLog;
import com.wooplr.spotlight.target.ViewTarget;
import com.zhy.autolayout.utils.AutoUtils;

/**
 * Created by flying.
 */

public class HighLight {
    private final String TAG = HighLight.class.getSimpleName();
    private ViewTarget target;
    private Point centerPoint;
    private Type type = Type.CIRCLE;
    private int conner = 15;
    private boolean onlyHighLight;
    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;
    private Point lineStartPoint = new Point();
    private int lineHeight = 300;
    private String headText;
    private int gravity = Gravity.CENTER;
    private int headTextMarginSide = 20;
    private Offset offset = Offset.CENTER;
    private int radius = 0;
    private RectF highLightRect = new RectF();
    private boolean lineDown = false;


    public HighLight(ViewTarget target) {
        init(target, true);
    }

    public HighLight(ViewTarget target, boolean onlyHighLight) {
        init(target, onlyHighLight);
    }

    private void init(ViewTarget target, boolean onlyHighLight) {
        this.target = target;
        this.onlyHighLight = onlyHighLight;
        centerPoint = getFocusPoint();
    }

    public HighLight setPadding(int padding) {
        int newPadding = AutoUtils.getPercentWidthSize(padding);
        paddingLeft = newPadding;
        paddingRight = newPadding;
        paddingBottom = newPadding;
        paddingTop = newPadding;
        return this;
    }

    public HighLight setPadding(int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
        this.paddingLeft = AutoUtils.getPercentWidthSize(paddingLeft);
        this.paddingRight = AutoUtils.getPercentWidthSize(paddingRight);
        this.paddingTop = AutoUtils.getPercentWidthSize(paddingTop);
        this.paddingBottom = AutoUtils.getPercentWidthSize(paddingBottom);
        return this;
    }


    public Point getDefaultLineStartPoint() {
        lineStartPoint.x = centerPoint.x;
        if (type != Type.CIRCLE) {
            if (offset == Offset.LEFT) {
                lineStartPoint.x = Math.round(highLightRect.left + highLightRect.height() / 2 + (highLightRect.width() - highLightRect.height()) / 8);
            } else if (offset == Offset.RIGHT) {
                lineStartPoint.x = Math.round(highLightRect.right - highLightRect.height() / 2 - (highLightRect.width() - highLightRect.height()) / 8);
            }
        }
        if (type == Type.CIRCLE) {
            lineStartPoint.y = centerPoint.y - radius;
            if (lineDown) {
                lineStartPoint.y = centerPoint.y + radius;
            }
        } else {
            lineStartPoint.y = Math.round(highLightRect.top);
            if (lineDown) {
                lineStartPoint.y = Math.round(highLightRect.bottom);
            }
        }
        return lineStartPoint;
    }

    public Point getLineEndPoint(Point start) {
        Point end = new Point();
        end.x = start.x;
        end.y = start.y - lineHeight;
        if (lineDown) {
            end.y = start.y + lineHeight;
        }
        return end;
    }

    public HighLight setLineHeight(int designHeight) {
        if (designHeight > 10) {
            this.lineHeight = getLineHeight(designHeight);
        }
        return this;
    }

    public HighLight setHeadText(String text) {
        this.headText = text;
        return this;
    }

    public String getHeadText() {
        return headText;
    }

    private int getLineHeight(int design) {
        return design * target.getView().getResources().getDisplayMetrics().heightPixels / 1334;
    }

    public HighLight setRadius(int radius) {
        this.radius = AutoUtils.getPercentWidthSizeBigger(radius);
        return this;
    }

    public int getRadius() {
        return radius;
    }

    public HighLight setOffset(Offset offset) {
        this.offset = offset;
        return this;
    }

    public HighLight setLineDown(boolean lineDown) {
        this.lineDown = lineDown;
        return this;
    }

    public Offset getOffset() {
        return offset;
    }

    public void draw(Canvas canvas, Paint eraser) {
        switch (type) {
            case RECT:
                drawRect(canvas, eraser, false);
                break;
            case ROUND_RECT:
                drawRect(canvas, eraser, true);
                break;
            case ARC_RECT:
                drawArcRect(canvas, eraser);
                break;
            case CIRCLE:
                drawCircle(canvas, eraser);
                break;
        }
    }

    public boolean isOnlyHighLight() {
        return onlyHighLight;
    }

    public HighLight setOnlyHighLight(boolean onlyHighLight) {
        this.onlyHighLight = onlyHighLight;
        return this;
    }

    public ViewTarget getTarget() {
        return target;
    }

    public HighLight setType(Type type) {
        this.type = type;
        return this;
    }

    private void drawArcRect(Canvas canvas, Paint eraser) {
        Rect r = target.getRect();
        int arc = (target.getViewHeight() + paddingBottom + paddingTop) / 2;
        highLightRect = new RectF(r.left - paddingLeft - arc, r.top - paddingTop, r.right + paddingRight + arc, r.bottom + paddingBottom);
        SLog.v(TAG, "drawArcRect.top = " + highLightRect.top);
        canvas.drawRoundRect(highLightRect, arc, arc, eraser);
    }

    private void drawRect(Canvas canvas, Paint eraser, boolean roundConner) {
        Rect r = target.getRect();
        highLightRect = new RectF(r.left - paddingLeft, r.top - paddingTop, r.right + paddingRight, r.bottom + paddingBottom);
        if (roundConner) {
            canvas.drawRoundRect(highLightRect, conner, conner, eraser);
        } else {
            canvas.drawRect(highLightRect, eraser);
        }
    }

    private void drawCircle(Canvas canvas, Paint eraser) {
        if (radius <= 0) {
            radius = calculateRadius(paddingTop + paddingBottom / 2);
        }
        canvas.drawCircle(centerPoint.x, centerPoint.y, radius, eraser);
    }

    private Point getFocusPoint() {
        return target.getPoint();
    }

    private int calculateRadius(int padding) {
        int side;
        int minSide = Math.min(target.getRect().width() / 2, target.getRect().height() / 2);
        int maxSide = Math.max(target.getRect().width() / 2, target.getRect().height() / 2);
        side = (minSide + maxSide) / 2;
        return side + padding;
    }

    public HighLight setRoundConner(int conner) {
        this.conner = conner;
        return this;
    }

    public HighLight setGravityLeft(int marginLeft) {
        this.gravity = Gravity.LEFT;
        headTextMarginSide = AutoUtils.getPercentWidthSizeBigger(marginLeft);
        return this;
    }

    public HighLight setGravityRight(int marginRight) {
        this.gravity = Gravity.RIGHT;
        headTextMarginSide = AutoUtils.getPercentWidthSizeBigger(marginRight);
        return this;
    }

    public TextView createHeadingTextView(Activity activity, Point end) {
        final TextView headingTv = new TextView(activity);
        FrameLayout.LayoutParams headingParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        if (lineDown) {
            headingParams.topMargin = end.y + 15;
            headingParams.gravity = this.gravity | Gravity.TOP;
        } else {
            headingParams.bottomMargin =  getSceenHeight(activity) - end.y + 15;
            headingParams.gravity = this.gravity | Gravity.BOTTOM;
        }
        if (gravity == Gravity.LEFT) {
            headingParams.leftMargin = headTextMarginSide;
        } else if (gravity == Gravity.RIGHT) {
            headingParams.rightMargin = headTextMarginSide;
        }
        headingTv.setLayoutParams(headingParams);
        headingTv.setText(headText);
        return headingTv;
    }

    public static boolean isNavigationBarShow(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y!=size.y;
        }else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if(menu || back) {
                return false;
            }else {
                return true;
            }
        }
    }

    public static int getNavigationBarHeight(Activity activity) {
        if (!isNavigationBarShow(activity)){
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }


    public static int getSceenHeight(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getHeight()+getNavigationBarHeight(activity);
    }

    public Point getPoint() {
        return centerPoint;
    }

    public enum Type {
        CIRCLE,
        RECT,
        ROUND_RECT,
        ARC_RECT,
    }

    public enum Offset {
        CENTER,
        LEFT,
        RIGHT,
    }
}
