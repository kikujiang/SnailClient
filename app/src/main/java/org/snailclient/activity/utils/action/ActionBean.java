package org.snailclient.activity.utils.action;

/**
 * 行为点对象，主要代表游戏中具体行为点的操作
 * @author wubo1
 *
 */
public class ActionBean {
	//行为点名称
	private String name;
	//行为点代号
	private String code;
	//行为点标志位
	private int id;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
