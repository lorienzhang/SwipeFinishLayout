package com.lorien.swipefinishlayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends SwipeFinishActivity {

    private List<String> list = new ArrayList<String>();
    private View mCloseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);


        ListView mListView = (ListView) findViewById(R.id.my_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ListViewActivity.this, R.layout.list_item, list);
        mListView.setAdapter(adapter);

        list.add("评论详情，评论详情，评论详情");
        for (int i = 0; i <= 30; i++) {
            list.add("回复: " + i);
        }


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                startActivity(new Intent(ListViewActivity.this, NormalActivity.class));
            }
        });

        mCloseView = findViewById(R.id.img_close);
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListViewActivity.this.finish();
            }
        });
    }
}
