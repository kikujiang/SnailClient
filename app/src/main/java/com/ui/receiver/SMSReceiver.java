package com.ui.receiver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snailclient.activity.sendCommand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.snail.util.Constants;
/**
 * 
 * 类的描述：接收短信广播
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class SMSReceiver extends BroadcastReceiver{
      boolean isReceSMS=true; //是否接收短信
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if(arg1.getAction().equals("com.snailgame.sms")){
			isReceSMS=true;
		}else if(arg1.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			if(isReceSMS){
			 SmsMessage msg = null;
	            Bundle bundle = arg1.getExtras();
	            if (bundle != null) {
	                Object[] pdusObj = (Object[]) bundle.get("pdus");
	                    msg= SmsMessage.createFromPdu((byte[]) pdusObj[0]);
	                    
	                    String msgTxt =msg.getMessageBody();//得到消息的内
	                    Pattern p = Pattern.compile("\\d{6}");
	                    Matcher m = p.matcher(msgTxt);
	                    if(m.find()){
	                    	Toast.makeText(arg0, "验证码为："+m.group(), Toast.LENGTH_LONG).show();
	                    	new sendCommand("view#Editxt_1_" +m.group()+"_T",Constants.port,"127.0.0.1").start();
	                    }else{
	                    	Toast.makeText(arg0, "没有验证码为", Toast.LENGTH_LONG).show();
	                    }
	            }
	            //isReceSMS=false;
			}
			
		}
	}
	 public final SmsMessage[] getMessagesFromIntent(Intent intent)

	    {

	        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");

	        byte[][] pduObjs = new byte[messages.length][];

	 

	        for (int i = 0; i < messages.length; i++)

	        {

	            pduObjs[i] = (byte[]) messages[i];

	        }

	        byte[][] pdus = new byte[pduObjs.length][];

	        int pduCount = pdus.length;

	        SmsMessage[] msgs = new SmsMessage[pduCount];

	        for (int i = 0; i < pduCount; i++)

	        {

	            pdus[i] = pduObjs[i];

	            msgs[i] = SmsMessage.createFromPdu(pdus[i]);

	        }

	        return msgs;

	    }
}
