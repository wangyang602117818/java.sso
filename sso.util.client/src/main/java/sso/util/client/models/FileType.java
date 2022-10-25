package sso.util.client.models;

public enum FileType {
	all(0), video(1), image(2), audio(3), office(4), pdf(5), text(6), attachment(7);

	private final int id;

	FileType(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}
}
