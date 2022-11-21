package sso.util.client.models;

public enum ErrorCode {
	success(0), error_permission(104), invalid_params(105), params_valid_fault(106), authorize_fault(401),
	page_not_found(404), ssoUrl_not_config(600), ssoSecret_not_config(601), ssoCookieKey_not_config(602),
	ssoCookieTime_not_config(603), messageUrl_not_config(604), server_exception(-1000);

	private final int id;

	ErrorCode(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}
}
