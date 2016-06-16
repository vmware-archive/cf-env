package io.pivotal.labs.cfenv;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudFoundryService {

    private static final Pattern KEY_PATTERN = Pattern.compile("-----BEGIN PRIVATE KEY-----\n(.*)\n-----END PRIVATE KEY-----\n?", Pattern.DOTALL);

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

    public Certificate getCertificate(String... path) throws CertificateException {
        String certificateString = (String) getCredential(path);
        return X509CertificateFactory.INSTANCE.generateCertificate(toStream(certificateString));
    }

    private ByteArrayInputStream toStream(String certificateString) {
        return new ByteArrayInputStream(certificateString.getBytes());
    }

    public Key getKey(String... path) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String keyString = (String) getCredential(path);

        Matcher matcher = KEY_PATTERN.matcher(keyString);
        if (!matcher.matches()) throw new IllegalArgumentException(keyString);
        String keyBytesString = matcher.group(1);

        byte[] keyBytes = Base64.getMimeDecoder().decode(keyBytesString);
        KeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return RSAKeyFactory.INSTANCE.generatePrivate(keySpec);
    }

}
