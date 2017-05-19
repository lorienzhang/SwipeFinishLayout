package com.lorien.swipefinishlayout;

import android.os.Bundle;
import android.view.View;

import com.lorien.swipefinishlayout.view.SwipeFinishLayout;


public class NormalActivity extends SwipeFinishActivity {

    private View mCloseView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        // 只支持下滑退出
        setSlideFinishFlags(SwipeFinishLayout.FLAG_SCROLL_RIGHT_FINISH);

        mCloseView = findViewById(R.id.img_close);
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NormalActivity.this.finish();
            }
        });
    }
}
