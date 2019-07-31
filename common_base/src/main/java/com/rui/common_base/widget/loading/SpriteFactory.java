package com.rui.common_base.widget.loading;


import com.rui.common_base.widget.loading.sprite.Sprite;
import com.rui.common_base.widget.loading.style.Circle;
import com.rui.common_base.widget.loading.style.FadingCircle;
import com.rui.common_base.widget.loading.style.ThreeBounce;

/**
 * Created by ybq.
 */
public class SpriteFactory {

    public static Sprite create(Style style) {
        Sprite sprite = null;
        switch (style) {
            case CIRCLE:
                sprite = new Circle();
                break;
            case FADING_CIRCLE:
                sprite = new FadingCircle();
                break;
            case THREE_BOUNCE:
                sprite = new ThreeBounce();
                break;
            default:
                break;
        }
        return sprite;
    }
}
