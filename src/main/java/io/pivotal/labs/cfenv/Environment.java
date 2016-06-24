package io.pivotal.labs.cfenv;

/**
 * An environment in which there are string variables; this is the means by which a {@link CloudFoundryEnvironment} obtains the Cloud Foundry environment variables. This abstraction exists mostly to ease testing. Most users will just want to capture a method reference to `System::getenv` as an implementation.
 */
@FunctionalInterface
public interface Environment {

    /**
     * Gets a variable from the environment.
     *
     * @param name the name of the variable
     * @return the value of the variable, or null if there is no such variable
     */
    public String get(String name);

    /**
     * Gets a mandatory variable from the environment.
     *
     * @param name the name of the variable
     * @return the value of the variable
     * @throws CloudFoundryEnvironmentException if there is no such variable
     */
    public default String lookup(String name) throws CloudFoundryEnvironmentException {
        String value = get(name);
        if (value == null || value.isEmpty()) {
            throw new CloudFoundryEnvironmentException("environment variable not defined: " + name);
        }
        return value;
    }

}
