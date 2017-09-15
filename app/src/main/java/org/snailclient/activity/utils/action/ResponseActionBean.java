package org.snailclient.activity.utils.action;

import java.util.List;

/**
 * 服务器返回的行为点的对象
 * @author wubo1
 *
 */
public class ResponseActionBean {

	//存放行为点的列表对象
	private List<ActionBean> list;
	//获取行为点是否存在
	private String result;
	
	public List<ActionBean> getList() {
		return list;
	}
	public void setList(List<ActionBean> list) {
		this.list = list;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
