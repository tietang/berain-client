package fengfei.berain.client;

public class BerainException extends Exception {

	private static final long serialVersionUID = 1L;

	public BerainException() {
		super();
	}

	public BerainException(String description) {
		super(description);
	}

	public BerainException(String description, Throwable throwable) {
		super(description, throwable);
	}
}
