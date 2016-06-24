package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An environment in which there is a set of uniquely named services, described by a https://docs.cloudfoundry.org/devguide/deploy-apps/environment-variable.html#VCAP-SERVICES[`VCAP_SERVICES`] environment variable.
 */
public class CloudFoundryEnvironment {

    private static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Map<String, CloudFoundryService> services;

    /**
     * Creates a new environment.
     *
     * @param environment the underlying environment from which to obtain the environment variables
     * @throws CloudFoundryEnvironmentException if any of the necessary variables are missing or malformed
     */
    public CloudFoundryEnvironment(Environment environment) throws CloudFoundryEnvironmentException {
        String vcapServices = environment.lookup(VCAP_SERVICES);

        Map<?, ?> rootNode = parse(vcapServices);

        services = rootNode.values().stream()
                .map(this::asCollection)
                .flatMap(Collection::stream)
                .map(this::asMap)
                .map(this::createService)
                .collect(Collectors.toMap(CloudFoundryService::getName, Function.identity()));
    }

    private Map<?, ?> parse(String json) throws CloudFoundryEnvironmentException {
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (IOException e) {
            throw new CloudFoundryEnvironmentException("error parsing JSON: " + json, e);
        }
    }

    private CloudFoundryService createService(Map<?, ?> serviceInstanceNode) {
        String name = (String) serviceInstanceNode.get("name");
        String label = (String) serviceInstanceNode.get("label");
        String plan = (String) serviceInstanceNode.get("plan");
        Set<String> tags = asCollection(serviceInstanceNode.get("tags")).stream()
                .map(String.class::cast)
                .collect(Collectors.toSet());
        Map<String, Object> credentials = castKeysToString(asMap(serviceInstanceNode.get("credentials")));
        return new CloudFoundryService(name, label, plan, tags, credentials);
    }

    private Collection<?> asCollection(Object o) {
        return (Collection<?>) o;
    }

    private Map<?, ?> asMap(Object o) {
        return (Map<?, ?>) o;
    }

    /**
     * Can't use Collectors::toMap because it chokes on null values
     */
    private Map<String, Object> castKeysToString(Map<?, ?> map) {
        Map<String, Object> credentials = new HashMap<>();
        map.forEach((k, v) -> credentials.put((String) k, v));
        return credentials;
    }

    public Set<String> getServiceNames() {
        return services.keySet();
    }

    /**
     * Gets information about a particular service by name.
     *
     * @param serviceName the name of the service to get
     * @return information about the service with the given name
     * @throws NoSuchElementException if there is no service with the given name
     */
    public CloudFoundryService getService(String serviceName) throws NoSuchElementException {
        CloudFoundryService service = services.get(serviceName);
        if (service == null) throw new NoSuchElementException("no such service: " + serviceName);
        return service;
    }

}
