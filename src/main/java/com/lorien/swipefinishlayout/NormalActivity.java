package com.lorien.swipefinishlayout;

import android.os.Bundle;
import android.view.View;


public class NormalActivity extends SwipeFinishActivity {

    private View mCloseView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        mCloseView = findViewById(R.id.img_close);
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NormalActivity.this.finish();
            }
        });
    }
}
