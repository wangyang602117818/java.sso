package sso.util.client.models;

import java.util.Date;
import java.util.List;

public class UserDetail {
	public String Id;
	public String UserId;
	public String UserName;
	public String CompanyCode;
	public String CompanyName;
	public String Mobile;
	public String Email;
	public String IdCard;
	public String Sex;
	public boolean IsModified;
	public List<String> DepartmentCode;
	public List<String> DepartmentName;
	public List<String> Role;
	public Date CreateTime;
	public Date LastLoginTime;
	public Date UpdateTime;
}
