package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CloudFoundryEnvironment {

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Set<String> serviceNames = new HashSet<>();

    public CloudFoundryEnvironment(Environment environment) throws CloudFoundryEnvironmentException {

        String vcapServices = environment.lookup(VCAP_SERVICES);

        JsonNode rootNode = parse(vcapServices);

        rootNode.forEach(serviceTypeNode -> {
            serviceTypeNode.forEach(serviceInstanceNode -> {
                String name = serviceInstanceNode.get("name").asText();
                serviceNames.add(name);
            });
        });
    }

    public Set<String> getServiceNames() {
        return serviceNames;
    }

    private JsonNode parse(String json) throws CloudFoundryEnvironmentException {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new CloudFoundryEnvironmentException("error parsing JSON: " + json, e);
        }
    }

}
