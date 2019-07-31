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
public class Circle extends CircleLayoutContainer {

    @Override
    public Sprite[] onCreateChild() {
        Dot[] dots = new Dot[8];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new Dot();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dots[i].setAnimationDelay(1200 / 8 * i);
            } else {
                dots[i].setAnimationDelay(1200 / 8 * i + -1200);
            }
        }
        return dots;
    }

    private class Dot extends CircleSprite {

        Dot() {
            setScale(0f);
            setAlpha(200);
        }

        @Override
        public ValueAnimator onCreateAnimation() {
            float fractions[] = new float[]{0.6f, 1f};
            return new SpriteAnimatorBuilder(this)
                    .scale(fractions, 1f, 0.6f)
                    .alpha(fractions, 200, 20)
                    .duration(1200)
                    .easeInOut(fractions)
                    .build();
        }

    }
}
