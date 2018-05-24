package com.jz.image_selector.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jz.image_selector.ImageSelectorActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageSelectorActivity.startSelect(MainActivity.this, 1, 2
                        , ImageSelectorActivity.MODE_SINGLE, true, new ArrayList<String>());


            }
        });

        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageSelectorActivity.startSelect(MainActivity.this, 1, 2
                        , ImageSelectorActivity.MODE_SINGLE, false, new ArrayList<String>());


            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null && requestCode == 1) {
                final List<String> images = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);
                final String path = images.get(0);
                Toast.makeText(this, "path:" + path, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


