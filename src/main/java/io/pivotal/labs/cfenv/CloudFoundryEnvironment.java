package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CloudFoundryEnvironment {

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Map<String, String> serviceUris = new HashMap<>();

    public CloudFoundryEnvironment(Environment environment) throws CloudFoundryEnvironmentException {
        String vcapServices = environment.lookup(VCAP_SERVICES);

        JsonNode rootNode = parse(vcapServices);

        rootNode.forEach(serviceTypeNode -> {
            serviceTypeNode.forEach(serviceInstanceNode -> {
                String name = serviceInstanceNode.get("name").asText();
                JsonNode uriNode = serviceInstanceNode.get("credentials").get("uri");
                if (uriNode != null) {
                    serviceUris.put(name, uriNode.asText());
                }
            });
        });
    }

    private JsonNode parse(String json) throws CloudFoundryEnvironmentException {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new CloudFoundryEnvironmentException("error parsing JSON: " + json, e);
        }
    }

    public Set<String> getServiceNames() {
        return serviceUris.keySet();
    }

    public URI getUri(String serviceName) throws URISyntaxException {
        return new URI(serviceUris.get(serviceName));
    }

}
