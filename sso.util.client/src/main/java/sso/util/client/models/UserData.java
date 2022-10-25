package sso.util.client.models;

import java.util.Map;

public class UserData {
	private String from = null;
	private String userId = null;
	private String userName = null;
	private String lang = null;
	private Map<String, String> extra;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public Map<String, String> getExtra() {
		return extra;
	}
	public void setExtra(Map<String, String> extra) {
		this.extra = extra;
	}
}
