package io.pivotal.labs.cfenv;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.Set;

public class CloudFoundryService {

    private final String name;
    private final String label;
    private final String plan;
    private final Set<String> tags;
    private final String uri;

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
        if (uri == null) throw new NoSuchElementException("no uri in service: " + name);
        return new URI(uri);
    }

}
