package io.pivotal.labs.cfenv;

public class CloudFoundryEnvironmentException extends Exception {

    public CloudFoundryEnvironmentException(String message) {
        super(message);
    }

    public CloudFoundryEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

}
