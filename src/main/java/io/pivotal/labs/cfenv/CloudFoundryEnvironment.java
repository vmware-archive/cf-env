package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class CloudFoundryEnvironment {

    public static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonNode rootNode;

    public CloudFoundryEnvironment(Environment environment) throws JsonProcessingException {
        String vcapServices = environment.lookup(VCAP_SERVICES);
        this.rootNode = parse(vcapServices);
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

    public String getName() {
        JsonNode serviceNode = getServiceNodeFromRootNode();
        return getServiceNameFromServiceNode(serviceNode);
    }

    public String getUri() {
        JsonNode serviceNode = getServiceNodeFromRootNode();
        return getServiceUriFromServiceNode(serviceNode);
    }

    public String getNameFor(String serviceType) {
        JsonNode serviceNode = getServiceNodeFromRootNodeByServiceType(serviceType);
        return getServiceNameFromServiceNode(serviceNode);
    }

    public String getUriFor(String serviceType) {
        JsonNode serviceNode = getServiceNodeFromRootNodeByServiceType(serviceType);
        return getServiceUriFromServiceNode(serviceNode);
    }

    private JsonNode getServiceNodeFromRootNodeByServiceType(String serviceType) {
        JsonNode serviceTypeNode = rootNode.get(serviceType);
        return serviceTypeNode.get(0);
    }

    private JsonNode getServiceNodeFromRootNode() throws NoSuchElementException {
        JsonNode serviceTypeNode = rootNode.fields().next().getValue();
        return serviceTypeNode.get(0);
    }

    private String getServiceNameFromServiceNode(JsonNode serviceNode) {
        return serviceNode.get("name").asText();
    }

    private String getServiceUriFromServiceNode(JsonNode serviceNode) {
        return serviceNode.get("credentials").get("uri").asText();
    }

}
