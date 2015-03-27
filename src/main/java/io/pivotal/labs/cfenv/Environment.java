package io.pivotal.labs.cfenv;

@FunctionalInterface
public interface Environment {

    public String get(String name);

    public default String lookup(String name) throws CloudFoundryEnvironmentException {
        String value = get(name);
        if (value == null || value.isEmpty()) {
            throw new CloudFoundryEnvironmentException("environment variable not defined: " + name);
        }
        return value;
    }

}
