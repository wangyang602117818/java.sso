package sso.util.client.service;

import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;

import sso.util.client.HttpClientRequestHelper;
import sso.util.client.JsonSerializerHelper;
import sso.util.client.JwtManager;
import sso.util.client.StringExtention;
import sso.util.client.models.ServiceModel;
import sso.util.client.models.UserDetail;
import sso.util.client.models.UserItem;
import sso.util.client.models.CompanyItem;
import sso.util.client.models.DepartmentItem;
import sso.util.client.models.PermissionReplace;
import sso.util.client.models.RoleItem;

public class SSOClientService {
	private HttpClientRequestHelper requestHelper = new HttpClientRequestHelper();
	public String RemoteUrl;
	public String Token;
	HashMap<String, String> headers = new HashMap<String, String>();
	JwtManager jwtManager = new JwtManager();

	public SSOClientService(String token) {
		RemoteUrl = StringExtention.Trim(jwtManager.ssoBaseUrl, "/");
		Token = token;
		headers.put("Authorization", token);
	}

	public ServiceModel<List<CompanyItem>> GetAllCompany() throws Exception {
		String resp = requestHelper.Get(RemoteUrl + "/company/getall", headers);
		return JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<List<CompanyItem>>>() {
		});
	}

	public ServiceModel<List<DepartmentItem>> GetAllDepartment(String companyCode) throws Exception {
		String resp = requestHelper.Get(RemoteUrl + "/department/getDepartments?companyCode=" + companyCode, headers);
		return JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<List<DepartmentItem>>>() {
		});
	}

	public ServiceModel<List<UserItem>> GetUserList() throws Exception {
		return GetUserList("", "", 1, 10, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode) throws Exception {
		return GetUserList(companyCode, "", 1, 10, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode, String filter) throws Exception {
		return GetUserList(companyCode, filter, 1, 10, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode, String filter, int pageIndex, int pageSize)
			throws Exception {
		return GetUserList(companyCode, filter, pageIndex, pageSize, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode, String filter, int pageIndex, int pageSize,
			String orderField, String orderType) throws Exception {
		String resp = requestHelper.Get(
				RemoteUrl + "/user/getBasic?companyCode=" + companyCode + "&filter=" + filter + "&orderField="
						+ orderField + "&orderType=" + orderType + "&pageIndex=" + pageIndex + "&pageSize=" + pageSize,
				headers);
		return JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<List<UserItem>>>() {
		});
	}

	public ServiceModel<List<RoleItem>> GetRoleList() throws Exception {
		return GetRoleList("", 1, 10);
	}

	public ServiceModel<List<RoleItem>> GetRoleList(String filter) throws Exception {
		return GetRoleList(filter, 1, 10);
	}

	public ServiceModel<List<RoleItem>> GetRoleList(String filter, int pageIndex, int pageSize) throws Exception {
		String resp = requestHelper.Get(
				RemoteUrl + "/role/getlist?filter=" + filter + "&pageIndex=" + pageIndex + "&pageSize=" + pageSize,
				headers);
		return JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<List<RoleItem>>>() {
		});
	}

	public ServiceModel<UserDetail> GetUserDetail(String userId) throws Exception {
		String resp = requestHelper.Get(RemoteUrl + "/user/getByUserId?userId=" + userId, headers);
		return JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<UserDetail>>() {
		});
	}

	public ServiceModel<List<String>> GetUserPermissions() throws Exception {
		String resp = requestHelper.Get(RemoteUrl + "/user/getPermissions", headers);
		return JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<List<String>>>() {
		});
	}

	public ServiceModel<String> ReplacePermissions(String origin, List<String> names) throws Exception {
		String resp = requestHelper.Post(RemoteUrl + "/permission/add", new PermissionReplace(origin, names), headers);
		return JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<String>>() {
		});
	}
}
