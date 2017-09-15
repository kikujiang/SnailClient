package org.snailclient.activity.utils.upload.bean;

import java.util.List;

public class ResponseReportBean {

	private List<ReportBean> list;
	private String result;
	public List<ReportBean> getList() {
		return list;
	}
	public void setList(List<ReportBean> list) {
		this.list = list;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
}
