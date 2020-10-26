package sample.camel;

public class MyException extends RuntimeException {

	MyException() {
	}

	MyException(String message) {
		super(message);
	}
}
