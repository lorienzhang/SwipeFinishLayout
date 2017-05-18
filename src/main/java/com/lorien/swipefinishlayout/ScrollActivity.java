package com.lorien.swipefinishlayout;

import android.os.Bundle;
import android.view.View;

public class ScrollActivity extends SwipeFinishActivity {

    private View mCloseView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);
        mCloseView = findViewById(R.id.img_close);
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollActivity.this.finish();
            }
        });
    }
}
