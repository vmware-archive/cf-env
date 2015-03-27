package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CloudFoundryEnvironment {

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Map<String, CloudFoundryService> services;

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
        Map<String, Object> credentials = asMap(serviceInstanceNode.get("credentials")).entrySet().stream()
                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> e.getValue()));
        return new CloudFoundryService(name, label, plan, tags, credentials);
    }

    private Collection<?> asCollection(Object o) {
        return (Collection<?>) o;
    }

    private Map<?, ?> asMap(Object o) {
        return (Map<?, ?>) o;
    }

    public Set<String> getServiceNames() {
        return services.keySet();
    }

    public CloudFoundryService getService(String serviceName) {
        CloudFoundryService service = services.get(serviceName);
        if (service == null) throw new NoSuchElementException("no such service: " + serviceName);
        return service;
    }

}
