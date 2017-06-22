package com.underdog.hydrate.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 * Created by chandrasekhar.dandu on 6/9/2017.
 */

public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private float from;
    private float to;

    public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
        super();
        this.progressBar = progressBar;
        this.from = from;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        progressBar.setProgress((int) value);
    }
}