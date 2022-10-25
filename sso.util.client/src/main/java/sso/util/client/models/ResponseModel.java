package sso.util.client.models;

public class ResponseModel<T> {
	private int code;
	private String message;
	private T result;
	private long count;

	public ResponseModel(ErrorCode code, T result) {
		this(code, result, 0);
	}

	public ResponseModel(ErrorCode code, T result, long count) {
		this.code = code.getValue();
		this.message = code.toString();
		this.result = result;
		this.count = count;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
