package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CloudFoundryEnvironment {

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Set<String> serviceNames = new HashSet<>();

    public CloudFoundryEnvironment(Environment environment) throws JsonProcessingException {

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

    private JsonNode parse(String json) throws JsonProcessingException {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw asJsonProcessingException(e);
        }
    }

    private JsonProcessingException asJsonProcessingException(IOException e) {
        class UnexpectedIOException extends JsonProcessingException {
            public UnexpectedIOException(IOException e) {
                super(e);
            }
        }

        return e instanceof JsonProcessingException ? (JsonProcessingException) e : new UnexpectedIOException(e);
    }

}
