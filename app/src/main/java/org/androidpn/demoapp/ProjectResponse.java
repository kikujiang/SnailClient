package org.androidpn.demoapp;

import java.util.List;

/**
 * 从服务器返回的json数据
 * @author wubo1
 *
 */
public class ProjectResponse {

	private List<ProjectNew> list;
	private String result;
	private List<String> errorType;
	public List<ProjectNew> getList() {
		return list;
	}
	public void setList(List<ProjectNew> list) {
		this.list = list;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public List<String> getErrorType() {
		return errorType;
	}
	public void setErrorType(List<String> errorType) {
		this.errorType = errorType;
	}
	
	
}
