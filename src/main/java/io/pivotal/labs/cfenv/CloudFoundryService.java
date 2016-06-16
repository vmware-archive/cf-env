package io.pivotal.labs.cfenv;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class CloudFoundryService {

    private final String name;
    private final String label;
    private final String plan;
    private final Set<String> tags;
    private final Map<String, Object> credentials;

    public CloudFoundryService(String name, String label, String plan, Set<String> tags, Map<String, Object> credentials) {
        this.name = name;
        this.label = label;
        this.plan = plan;
        this.tags = tags;
        this.credentials = credentials;
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

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public URI getUri() throws URISyntaxException {
        String uri = (String) credentials.get("uri");
        if (uri == null) throw new NoSuchElementException("no uri in service: " + name);
        return new URI(uri);
    }

    public Object getCredential(String... path) {
        return getCredential(Arrays.asList(path));
    }

    private Object getCredential(List<String> path) {
        Map<?, ?> map;
        if (path.size() == 0) {
            throw new IllegalArgumentException();
        } else if (path.size() == 1) {
            map = credentials;
        } else {
            Object parent = getCredential(head(path));
            if (!(parent instanceof Map)) throw notFound(path);
            map = (Map<?, ?>) parent;
        }

        String tail = tail(path);
        if (!map.containsKey(tail)) throw notFound(path);
        return map.get(tail);
    }

    private <E> List<E> head(List<E> list) {
        return list.subList(0, list.size() - 1);
    }

    private <E> E tail(List<E> name) {
        return name.get(name.size() - 1);
    }

    private NoSuchElementException notFound(List<String> path) {
        return new NoSuchElementException(String.join(".", path));
    }

}
