package org.snailclient.activity.utils.upload.bean;

/**
 * "name": "『魔龙世界』2016122811日常版本功能测试",
   "rid": "2644_2",
   "projectName": "魔龙世界",
   "apkUrl": "http://10.206.2.132:8080/rf/apk/121/1482896347216/12161482896347216.apk",
   "apkVersion": "",
   "apkSize": 0,
   "apkLogo": "http://10.206.2.132:8080//SystemTestPlatform/images/default_report_logo.png",
   "appVersion": "2016122811",
   "versionType": ""
 * @author wubo1
 *
 */
public class ReportBean{
	
	private String name;
	private String rid;
	private String apkUrl;
	private String apkVersion;
	private float apkSize;
	private String apkLogo;
	private String appVersion;
	private String versionType;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRid() {
		return rid;
	}
	public void setRid(String rid) {
		this.rid = rid;
	}
	public String getApkUrl() {
		return apkUrl;
	}
	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}
	public String getApkVersion() {
		return apkVersion;
	}
	public void setApkVersion(String apkVersion) {
		this.apkVersion = apkVersion;
	}
	public float getApkSize() {
		return apkSize;
	}
	public void setApkSize(float apkSize) {
		this.apkSize = apkSize;
	}
	public String getApkLogo() {
		return apkLogo;
	}
	public void setApkLogo(String apkLogo) {
		this.apkLogo = apkLogo;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public String getVersionType() {
		return versionType;
	}
	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}
	
}
