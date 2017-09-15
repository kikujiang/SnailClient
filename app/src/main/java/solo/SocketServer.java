package solo;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.util.Log;

	
public class SocketServer extends Thread{
	public static final String TAG = "SocketServer";
	ServerSocket server;
    Context context;
	public SocketServer(Context context){
		this.context=context;
		
	}
	@Override 
	public void run(){
		try {
			
			server=new ServerSocket(6666);
			  while(true){
			    Socket socket = null;
				socket =server.accept();
				InetAddress address=socket.getInetAddress();
				String ip=address.getHostAddress();
				Log.d(TAG, "ip is:" + ip);
				new Wthread(socket,context).start();
			  }
	//			  if(bip(ip)){
				//if((ip.contains("172.19.26.210"))||(ip.contains("10.206.0.12"))||ip.contains("172.36.0.19")){
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
