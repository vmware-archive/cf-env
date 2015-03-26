package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class CloudFoundryEnvironment {

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String serviceName;
    private String serviceUri;

    public CloudFoundryEnvironment(Environment environment) throws JsonProcessingException {
        String vcapServices = environment.lookup(VCAP_SERVICES);
        JsonNode rootNode = parse(vcapServices);

        try {
            serviceName = getServiceName(rootNode);
            serviceUri = getServiceUri(rootNode);
        } catch (NoSuchElementException ex) {
            serviceName = null;
            serviceUri = null;
        }
    }

    private JsonNode parse(String json) throws JsonProcessingException {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw asJsonProcessingException(e);
        }
    }

    private String getServiceUri(JsonNode rootNode) {
        return getServiceNode(rootNode).get("credentials").get("uri").asText();
    }

    private String getServiceName(JsonNode rootNode) {
        return getServiceNode(rootNode).get("name").asText();
    }

    private JsonNode getServiceNode(JsonNode rootNode) throws NoSuchElementException {
        Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.fields();
        return iterator.next().getValue().get(0);
    }

    private JsonProcessingException asJsonProcessingException(IOException e) {
        class UnexpectedIOException extends JsonProcessingException {
            public UnexpectedIOException(IOException e) {
                super(e);
            }
        }

        return e instanceof JsonProcessingException ? (JsonProcessingException) e : new UnexpectedIOException(e);
    }

    public String getName() {
        return serviceName;
    }

    public String getUri() {
        return serviceUri;
    }
}
