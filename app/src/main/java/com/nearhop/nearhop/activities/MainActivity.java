package com.nearhop.nearhop.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.nearhop.nearhop.R;
import com.nearhop.nearhop.databinding.ActivityMainBinding;

public class MainActivity extends NHActivity {
//    MainActivityBinding binding;
    ActivityMainBinding binding;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.scandevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevices();
            }
        });
        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    void scanForDevices(){

    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
