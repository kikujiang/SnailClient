package solo;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;

import org.snailclient.activity.utils.ConnectStatus;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

public class BackService extends Service {
	private static final long HEART_BEAT_RATE = 30 * 1000;
	
	private WeakReference<Socket> mSocket;

	// For heart Beat
	private Handler mHandler = new Handler();
	private Runnable heartBeatRunnable = new Runnable() {

		public void run() {
			if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
				ConnectStatus.setHeartBeats("true");
				boolean isSuccess = sendMsg("");//就发送一个\r\n过去 如果发送失败，就重新初始化一个socket
				if (!isSuccess) {
					mHandler.removeCallbacks(heartBeatRunnable);
				}
			}
			mHandler.postDelayed(this, HEART_BEAT_RATE);
		}
	};

	private long sendTime = 0L;
	private IBackService.Stub iBackService = new IBackService.Stub() {
		public boolean sendMessage(String message) throws RemoteException {
			return sendMsg(message);
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return iBackService;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		ConnectStatus.setConnectStatus("true");
		mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//初始化成功后，就准备发送心跳包
		
	}
	public boolean sendMsg(String msg) {
		if (null == mSocket || null == mSocket.get()) {
			return false;
		}
		Socket soc = mSocket.get();
		try {
			if (!soc.isClosed() && !soc.isOutputShutdown()) {
				OutputStream os = soc.getOutputStream();
				String message = msg + "\r\n";
				os.write(message.getBytes());
				os.flush();
				sendTime = System.currentTimeMillis();//每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
