package com.cuonghx.teacher.teachercheckin.util.navigator;

import androidx.annotation.IntDef;

import static com.cuonghx.teacher.teachercheckin.util.navigator.NavigateAnimation.BOTTOM_UP;
import static com.cuonghx.teacher.teachercheckin.util.navigator.NavigateAnimation.FADED;
import static com.cuonghx.teacher.teachercheckin.util.navigator.NavigateAnimation.LEFT_RIGHT;
import static com.cuonghx.teacher.teachercheckin.util.navigator.NavigateAnimation.NONE;
import static com.cuonghx.teacher.teachercheckin.util.navigator.NavigateAnimation.RIGHT_LEFT;


@IntDef({NONE, RIGHT_LEFT, BOTTOM_UP, FADED, LEFT_RIGHT})
public @interface NavigateAnimation {
    int NONE = 0;
    int RIGHT_LEFT = 1;
    int BOTTOM_UP = 2;
    int FADED = 3;
    int LEFT_RIGHT = 4;
}
