package com.lorien.swipefinishlayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.lorien.swipefinishlayout.view.SwipeFinishLayout;

/**
 * 不展示任何UI，只负责处理Activity滑动退出的事件
 */
public class SwipeFinishActivity extends Activity {
    protected SwipeFinishLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = (SwipeFinishLayout) LayoutInflater.from(this).inflate(R.layout.base, null);
        layout.attachToActivity(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(0, R.anim.slide_bottom_out);
    }

    @Override
    public void finish() {
        if (layout.attachedActivityShouldFinish()) {
            super.finish();
        } else {
            layout.finishActivityBottomOut();
        }
    }
}
