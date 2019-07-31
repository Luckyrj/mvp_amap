package com.rui.common_base.widget.loading;

/**
 * Created by ybq.
 */
public enum Style {


    CIRCLE(0),
    FADING_CIRCLE(1),
    THREE_BOUNCE(2);
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private int value;

    Style(int value) {
        this.value = value;
    }
}
