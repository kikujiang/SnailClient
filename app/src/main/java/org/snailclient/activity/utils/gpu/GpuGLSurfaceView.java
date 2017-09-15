package org.snailclient.activity.utils.gpu;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;

public class GpuGLSurfaceView extends GLSurfaceView {

	GpuRenderer mRenderer;  
    public GpuGLSurfaceView(Context context,Handler mHandler) {  
        super(context);  
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);  
        mRenderer = new GpuRenderer(mHandler);  
        setRenderer(mRenderer);  
    }  
}
