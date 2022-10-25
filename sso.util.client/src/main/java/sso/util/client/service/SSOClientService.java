package sso.util.client.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import sso.util.client.WebClientRequestHelper;
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
	private WebClientRequestHelper requestHelper = new WebClientRequestHelper();
	public String RemoteUrl;
	public String Token;
	HashMap<String, String> headers = new HashMap<String, String>();
	JwtManager jwtManager = new JwtManager();

	public SSOClientService(String token) {
		RemoteUrl = StringExtention.Trim(jwtManager.ssoBaseUrl, "/");
		Token = token;
		headers.put("Authorization", token);
	}

	public ServiceModel<List<CompanyItem>> GetAllCompany() {
		return requestHelper.Get(RemoteUrl + "/company/getall", headers,
				new ParameterizedTypeReference<ServiceModel<List<CompanyItem>>>() {
				}).block();
	}

	public ServiceModel<List<DepartmentItem>> GetAllDepartment(String companyCode) throws Exception {
		return requestHelper.Get(RemoteUrl + "/department/getDepartments?companyCode=" + companyCode, headers,
				new ParameterizedTypeReference<ServiceModel<List<DepartmentItem>>>() {
				}).block();
	}

	public ServiceModel<List<UserItem>> GetUserList() {
		return GetUserList("", "", 1, 10, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode) {
		return GetUserList(companyCode, "", 1, 10, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode, String filter) {
		return GetUserList(companyCode, filter, 1, 10, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode, String filter, int pageIndex, int pageSize) {
		return GetUserList(companyCode, filter, pageIndex, pageSize, "UserName", "asc");
	}

	public ServiceModel<List<UserItem>> GetUserList(String companyCode, String filter, int pageIndex, int pageSize,
			String orderField, String orderType) {
		return requestHelper.Get(
				RemoteUrl + "/user/getBasic?companyCode=" + companyCode + "&filter=" + filter + "&orderField="
						+ orderField + "&orderType=" + orderType + "&pageIndex=" + pageIndex + "&pageSize=" + pageSize,
				headers, new ParameterizedTypeReference<ServiceModel<List<UserItem>>>() {
				}).block();
	}

	public ServiceModel<List<RoleItem>> GetRoleList() {
		return GetRoleList("", 1, 10);
	}

	public ServiceModel<List<RoleItem>> GetRoleList(String filter) {
		return GetRoleList(filter, 1, 10);
	}

	public ServiceModel<List<RoleItem>> GetRoleList(String filter, int pageIndex, int pageSize) {
		return requestHelper
				.Get(RemoteUrl + "/role/getlist?filter=" + filter + "&pageIndex=" + pageIndex + "&pageSize=" + pageSize,
						headers, new ParameterizedTypeReference<ServiceModel<List<RoleItem>>>() {
						})
				.block();
	}

	public ServiceModel<UserDetail> GetUserDetail(String userId) {
		return requestHelper.Get(RemoteUrl + "/user/getByUserId?userId=" + userId, headers,
				new ParameterizedTypeReference<ServiceModel<UserDetail>>() {
				}).block();
	}

	public ServiceModel<List<String>> GetUserPermissions() {
		return requestHelper.Get(RemoteUrl + "/user/getPermissions", headers,
				new ParameterizedTypeReference<ServiceModel<List<String>>>() {
				}).block();
	}

	public ServiceModel<String> ReplacePermissions(String origin, List<String> names) {
		return requestHelper.Post(RemoteUrl + "/permission/add", new PermissionReplace(origin, names), headers,
				new ParameterizedTypeReference<ServiceModel<String>>() {
				}).block();
	}
}
