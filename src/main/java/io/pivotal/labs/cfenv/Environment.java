package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.core.JsonProcessingException;

@FunctionalInterface
public interface Environment {

    public String get(String name);

    public default String lookup(String name) throws JsonProcessingException {
        String value = get(name);
        if (value == null) throw new MissingEnvironmentVariableException(name);
        return value;
    }

    public class MissingEnvironmentVariableException extends JsonProcessingException {
        public MissingEnvironmentVariableException(String message) {
            super(message);
        }
    }

}
