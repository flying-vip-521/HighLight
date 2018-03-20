package com.wooplr.spotlight.target;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;

import com.shuame.utils.SLog;

/**
 * Created by flying on 10/06/16.
 */
public class ViewTarget implements Target {

    private View view;

    private int[] locations;

    public ViewTarget(View view) {
        this.view = view;
    }

    public ViewTarget(View view, int[] locations) {
        this(view);
        this.locations = locations;
    }

    @Override
    public Point getPoint() {
        int[] location;
        if (locations == null){
            location = new int[2];
            view.getLocationInWindow(location);
        }else {
            location = locations;
        }
        return new Point(location[0] + (view.getWidth() / 2), location[1] + (view.getHeight() / 2));
    }

    @Override
    public Rect getRect() {
        int[] location;
        if (locations == null){
            location = new int[2];
            view.getLocationInWindow(location);
        }else {
            location = locations;
        }
        return new Rect(
                location[0],
                location[1],
                location[0] + view.getWidth(),
                location[1] + view.getHeight()
        );
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public int getViewLeft() {
        return getRect().left;
    }

    @Override
    public int getViewRight() {
        return getRect().right;
    }

    @Override
    public int getViewBottom() {
        return getRect().bottom;
    }

    @Override
    public int getViewTop() {
        return getRect().top;
    }

    @Override
    public int getViewWidth() {
        return view.getWidth();
    }

    @Override
    public int getViewHeight() {
        return view.getHeight();
    }


}
