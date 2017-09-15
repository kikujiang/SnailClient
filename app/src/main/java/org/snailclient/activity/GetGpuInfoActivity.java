package org.snailclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.snailclient.activity.utils.gpu.GpuGLSurfaceView;

public class GetGpuInfoActivity extends AppCompatActivity {

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String gpuInfo = (String) msg.obj;
            Bundle data = new Bundle();
            data.putString("gpu_name", gpuInfo);
            Intent infoIntent = new Intent(GetGpuInfoActivity.this, InfoActivity.class);
            infoIntent.putExtras(data);
            startActivity(infoIntent);
            finish();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GpuGLSurfaceView glView = new GpuGLSurfaceView(this, mHandler);
        setContentView(glView);
    }

}
