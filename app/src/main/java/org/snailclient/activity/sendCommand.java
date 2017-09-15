package org.snailclient.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

import com.snail.util.Constants;
/**
 * 
 * 类的描述：发送短信验证码功能
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class sendCommand extends Thread {
	
	private static final String TAG = "sendCommand";
	
	String head;
	int port;
	String ip;
	public sendCommand(String cmd,int port,String ip){
		head=cmd+"\r\n";
		this.port=port;
		this.ip=ip;
	}
	@Override
	public void run() {
		Socket socket = null;
		OutputStream outStream = null;
		BufferedReader input = null;
		try {
			socket = new Socket(ip,port);
			outStream = socket.getOutputStream();
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			outStream.write(head.getBytes());
			Log.e(TAG,"sencCommand send message "+Constants.port+":"+head);
			String line = null;
			while ((line = input.readLine()) != null) {
				Log.d(TAG, line);
				break;
			}
			outStream.close();
			input.close();
			socket.close();
		} catch (NumberFormatException e1) {
			Log.e(TAG,"sencCommand"+e1.getMessage());
		} catch (UnknownHostException e1) {
			Log.e(TAG,"sencCommand"+e1.getMessage());
		} catch (IOException e1) {
			Log.e(TAG,"sencCommand"+e1.getMessage());
		}
	}

}
