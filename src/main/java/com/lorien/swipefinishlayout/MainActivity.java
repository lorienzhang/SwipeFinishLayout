package com.lorien.swipefinishlayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
    }

    public void normalView(View view) {
        startActivity(new Intent(this, NormalActivity.class));
        overridePendingTransition(R.anim.slide_bottom_in, 0);
    }

    public void listView(View view) {
        startActivity(new Intent(this, ListViewActivity.class));
        overridePendingTransition(R.anim.slide_bottom_in, 0);
    }

    public void scrollView(View view) {
        startActivity(new Intent(this, ScrollActivity.class));
        overridePendingTransition(R.anim.slide_bottom_in, 0);
    }
}
