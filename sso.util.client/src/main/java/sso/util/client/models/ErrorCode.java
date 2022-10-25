package sso.util.client.models;

public enum ErrorCode {
	success(0), error_permission(104),invalid_params(105), params_valid_fault(106), authorize_fault(401),page_not_found(404), baseUrl_not_config(600),
	secretKey_not_config(601),cookieKey_not_config(602),cookieTime_not_config(603),messageBaseUrl_not_config(604), server_exception(-1000);

	private final int id;

	ErrorCode(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}
}
