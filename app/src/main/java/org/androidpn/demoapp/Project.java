package org.androidpn.demoapp;

import android.graphics.Bitmap;

import com.snail.util.Constants;
/**
 * 
 * 类的描述：测试报告 项目信息类
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public	class Project{
	String url;
	String rid;
	String report;
	String pro;
	String appVersion;
	String apkLogo;
	String appSize;
	String versionType;
	Bitmap bit;
	String packageName;
	long id=0;
	boolean isrun=false;
	public Project(String url,String tid,String report,String pro,String apkLogo,String appVersion,String appSize,String versionType,String packageName){
		this.pro=pro;
		this.report=report;
		this.rid=tid;
		this.url=url;
		this.appVersion=appVersion;
		this.apkLogo=apkLogo; 
		this.appSize=appSize;
		this.versionType=versionType;
		this.packageName = packageName;
	}
	public void setIsRun(boolean isrun){
		this.isrun=isrun;
	}
	public boolean getIsRun(){
		return isrun;
	}
	public String getPro(){
		return pro;
		}
	public String getReport(){
		return report;
		}
	public String getRid(){
		return rid;
		}
	public String getUrl(){
		return url;
		}
	public String getAppVersion(){
		return appVersion;
		}
	public String getAppSize(){
		return appSize;
	}
	public String getVersionType(){
		if(Constants.islaunch){
			return versionType+"兼容性测试";
		}else{
			return versionType+"众测";
		}
	} 
	public String getApkLogo(){
		return apkLogo;

	}
	public void setId(long id){
		this.id=id;
	}
	public long getId(){
		return id;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
}