package sso.util.client.models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserItem {
	public String Id;
	public String UserId;
	public String UserName;
	public String CompanyCode;
	public String Sex;
	public String Mobile;
	public String CompanyName;
	public String DepartmentName;
	public String RoleName;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date CreateTime;
}
