package com.rui.common_base.widget.loading.style;

import android.animation.ValueAnimator;
import android.os.Build;

import com.rui.common_base.widget.loading.animation.SpriteAnimatorBuilder;
import com.rui.common_base.widget.loading.sprite.CircleLayoutContainer;
import com.rui.common_base.widget.loading.sprite.CircleSprite;
import com.rui.common_base.widget.loading.sprite.Sprite;


/**
 * Created by ybq.
 */
public class FadingCircle extends CircleLayoutContainer {

    @Override
    public Sprite[] onCreateChild() {
        Dot[] dots = new Dot[8];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new Dot();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dots[i].setAnimationDelay(1500 / 8 * i);
            } else {
                dots[i].setAnimationDelay(1500 / 8 * i + -1500);
            }
        }
        return dots;
    }

    private class Dot extends CircleSprite {

        Dot() {
            setAlpha(240);
        }

        @Override
        public ValueAnimator onCreateAnimation() {
            float fractions[] = new float[]{0.1f,  1f};
            return new SpriteAnimatorBuilder(this).
                    alpha(fractions,  240, 20).
                    duration(1500).
                    easeInOut(fractions)
                    .build();
        }
    }
}
