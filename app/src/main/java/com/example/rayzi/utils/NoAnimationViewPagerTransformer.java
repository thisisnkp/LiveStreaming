package com.example.rayzi.utils;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class NoAnimationViewPagerTransformer implements ViewPager.PageTransformer {

   /* @Override
    public void transformPage(View view, float position) {
        if (position < 0) {
            view.setScrollX((int) ((float) (view.getWidth()) * position));
        } else if (position > 0) {
            view.setScrollX(-(int) ((float) (view.getWidth()) * -position));
        } else {
            view.setScrollX(0);
        }
    }*/

    @Override
    public void transformPage(View page, float position) {

        page.setTranslationX(-position * page.getWidth());

        page.setAlpha(1 - Math.abs(position));

    }
}
