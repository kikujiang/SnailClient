package com.snail.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * 类的描述：发送表单信息给web 针对测试平台bug 接口做的
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class SocketHttpRequester extends Thread{
	 private static final String CHARSET = "utf-8"; // 设置编码
    String multipart_form_data = "multipart/form-data";  
    String twoHyphens = "--";  
    String boundary = "****************fD4fH3gL0hK7aI6";    // 数据分隔符  
    String lineEnd = "\r\n";    // The value is "\r\n" in Windows. 
    String actionUrl;
    Handler handler;
    Set<Entry<String, String>> set;
    File files;
      public SocketHttpRequester(String actionUrl, Set<Entry<String, String>> set, File files,Handler handler){
    	  this.actionUrl=actionUrl;
    	  this.set=set;
    	  this.files=files;
    	  this.handler=handler;
      }
    /* 
     * 上传图片内容，格式请参考HTTP 协议格式。 
     * 人人网Photos.upload中的”程序调用“http://wiki.dev.renren.com/wiki/Photos.upload#.E7.A8.8B.E5.BA.8F.E8.B0.83.E7.94.A8 
     * 对其格式解释的非常清晰。 
     * 格式如下所示： 
     * --****************fD4fH3hK7aI6 
     * Content-Disposition: form-data; name="upload_file"; filename="apple.jpg" 
     * Content-Type: image/jpeg 
     * 
     * 这儿是文件的内容，二进制流的形式 
     */  
    private void addImageContent(File file, DataOutputStream output) {  

            StringBuilder split = new StringBuilder();  
            split.append(twoHyphens + boundary + lineEnd);  
            split.append("Content-Disposition: form-data; name=\"pics"+ "\"; filename=\"" + file.getName() + "\"" + lineEnd);  
           split.append("Content-Type:application/octet-stream" + lineEnd);  
            split.append(lineEnd);  
            try {  
      
                // 发送图片数据  
                output.writeBytes(split.toString());  
            	output.flush();
                FileInputStream fis = new FileInputStream(file.getAbsoluteFile());
                byte[] buffer = new byte[8192]; // 8k
                int count = 0;
                // 读取文件
                while ((count = fis.read(buffer)) != -1){
                	output.write(buffer, 0, count);
                }
                fis.close();
                output.writeBytes(lineEnd);  
            } catch (IOException e) {  
                throw new RuntimeException(e);  
            }  
 
    }  
      
    /* 
     * 构建表单字段内容，格式请参考HTTP 协议格式（用FireBug可以抓取到相关数据）。(以便上传表单相对应的参数值) 
     * 格式如下所示： 
     * --****************fD4fH3hK7aI6 
     * Content-Disposition: form-data; name="action" 
     * // 一空行，必须有 
     * upload 
     */  
    private void addFormField(Set<Map.Entry<String,String>> params, DataOutputStream output) {  
        StringBuilder sb = new StringBuilder();  
        for(Entry<String, String> param : params) {  
            sb.append(twoHyphens + boundary+lineEnd);  
            sb.append("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + lineEnd); 
            sb.append("Content-Type: text/plain; charset=" + CHARSET + lineEnd);
            sb.append("Content-Transfer-Encoding: 8bit" + lineEnd);
            sb.append(lineEnd);  
            try {
				sb.append(URLEncoder.encode(param.getValue(),"utf-8") + lineEnd);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
        }  
        try {   
            output.writeBytes(sb.toString());// 发送表单字段数据  
        } catch (IOException e) {  
            throw new RuntimeException(e);  
        }  
    }  
      
    /** 
     * 直接通过 HTTP 协议提交数据到服务器，实现表单提交功能。 
     * @param actionUrl 上传路径 
     * @param set 请求参数key为参数名，value为参数值 
     * @param files 上传文件信息 
     * @return 返回请求结果 
     */  
    public void run() {  
        HttpURLConnection conn = null;  
        DataOutputStream output = null;  
        BufferedReader input = null;
        try {  
            URL url = new URL(actionUrl);  
            conn = (HttpURLConnection) url.openConnection();  
            conn.setReadTimeout(5 * 60 * 1000);
            conn.setConnectTimeout(120000);  
            conn.setDoInput(true);        // 允许输入  
            conn.setDoOutput(true);        // 允许输出  
            conn.setUseCaches(false);    // 不使用Cache  
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Connection", "keep-alive");  
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");
            conn.setRequestProperty("Content-Type", multipart_form_data + ";boundary=" + boundary);  
            conn.setRequestProperty("Charset", "utf-8");
              
            conn.connect();  
            output = new DataOutputStream(conn.getOutputStream());  
              
          
              
          
            //output.writeBytes(lineEnd);
            addImageContent(files,output);    // 添加图片内容  
            addFormField(set, output);    // 添加表单字段内容  
        	//output.flush();
            output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);// 数据结束标志  
            output.flush();  
           
            int code = conn.getResponseCode();  
            if(code != 200) {  
                //throw new RuntimeException("请求‘" + actionUrl +"’失败！");  
            	Log.i("zlulan", "提交bug连接失败");
            }  
              
            input = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
            StringBuilder response = new StringBuilder();  
            String oneLine;  
            while((oneLine = input.readLine()) != null) {  
                response.append(oneLine + lineEnd);  
            }  
              
            Log.i("zlulan", "结果"+response.toString()) ;  
            Message msg=new Message();
            msg.arg1=1;
            msg.obj=response.toString();
            handler.sendMessage(msg);
        } catch (IOException e) {  
            throw new RuntimeException(e);  
        } finally {  
            // 统一释放资源  
            try {  
                if(output != null) {  
                    output.close();  
                }  
                if(input != null) {  
                    input.close();  
                }  
            } catch (IOException e) {  
                throw new RuntimeException(e);  
            }  
              
            if(conn != null) {  
                conn.disconnect();  
            }  
        }  
    }  
    
}
    
