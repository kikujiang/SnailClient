package com.snail.service;

import solo.SocketServer;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SocketService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
  @Override
	public void onCreate() {
		super.onCreate();
		SocketServer ss=new SocketServer(this);
		ss.start();
  }
}
