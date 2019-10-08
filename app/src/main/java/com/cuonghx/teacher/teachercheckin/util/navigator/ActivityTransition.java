package com.cuonghx.teacher.teachercheckin.util.navigator;

import androidx.annotation.IntDef;

import static com.cuonghx.teacher.teachercheckin.util.navigator.ActivityTransition.FINISH;
import static com.cuonghx.teacher.teachercheckin.util.navigator.ActivityTransition.NONE;
import static com.cuonghx.teacher.teachercheckin.util.navigator.ActivityTransition.START;

@IntDef({NONE, START, FINISH})
@interface ActivityTransition {
    int NONE = 0;
    int START = 1;
    int FINISH = -1;
}
