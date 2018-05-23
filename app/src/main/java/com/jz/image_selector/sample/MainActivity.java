package com.jz.image_selector.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jz.image_selector.ImageSelectorActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*ImageSelectorActivity.startSelect(MainActivity.this, 1, 2
                        , ImageSelectorActivity.MODE_SINGLE, new ArrayList<String>());*/

                ImageSelectorActivity.startSelect(MainActivity.this, 1, 2
                        , ImageSelectorActivity.MODE_MULTI, new ArrayList<String>());
            }
        });


    }


}


