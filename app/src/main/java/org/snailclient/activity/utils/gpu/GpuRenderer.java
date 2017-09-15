package org.snailclient.activity.utils.gpu;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GpuRenderer implements Renderer {

	private static final String TAG = "GpuRenderer";
	private Handler handler;
	
	public GpuRenderer(Handler mHandler){
		super();
		this.handler = mHandler;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int arg1, int arg2) {
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
		Log.d(TAG, "GL_RENDERER = " +gl.glGetString(GL10.GL_RENDERER));   
		Message gpuMsg = Message.obtain();
		gpuMsg.obj = gl.glGetString(GL10.GL_RENDERER);
		handler.sendMessage(gpuMsg);
	}

}
