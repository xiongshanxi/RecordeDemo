package com.cy.piano;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPython();
        callPythonCodeFromLib();
    }

    void callPythonCodeFromLib(){
        Python py = Python.getInstance();
        py.getModule("callPyLib").callAttr("get_http");
        py.getModule("callPyLib").callAttr("print_numpy");
        py.getModule("callPyLib").callAttr("Love");
    }


    // 初始化 Python环境
    void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
}
