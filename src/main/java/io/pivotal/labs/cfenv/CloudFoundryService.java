package io.pivotal.labs.cfenv;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class CloudFoundryService {

    private String name;
    private String label;
    private String plan;
    private Set<String> tags;
    private String uri;

    public CloudFoundryService(String name, String label, String plan, Set<String> tags, String uri) {
        this.name = name;
        this.label = label;
        this.plan = plan;
        this.tags = tags;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getPlan() {
        return plan;
    }

    public Set<String> getTags() {
        return tags;
    }

    public URI getUri() throws URISyntaxException {
        return new URI(uri);
    }

}

