package io.pivotal.labs.cfenv;

import io.pivotal.labs.cfenv.crypto.CryptoParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Information about a service. The information comprises some metadata - name, label, plan, and tags - and some structured credentials.
 */
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

    /**
     * Gets the credentials. This is parsed from a JSON object, and so may contain booleans, strings, integers, doubles, nulls, and lists or string-keyed maps of any of these.
     *
     * @return the service's credentials
     */
    public Map<String, Object> getCredentials() {
        return credentials;
    }

    /**
     * Gets the service's URI. The URI is a top-level string entry in the {@linkplain #getCredentials() credentials} map with the key "uri".
     *
     * @return the service's URI
     * @throws NoSuchElementException if the service does not have a URI
     * @throws URISyntaxException     if the URI is malformed
     */
    public URI getUri() throws NoSuchElementException, URISyntaxException {
        String uri = (String) credentials.get("uri");
        if (uri == null) throw new NoSuchElementException("no uri in service: " + name);
        return new URI(uri);
    }

    /**
     * Gets a particular credential from the {@linkplain #getCredentials() credentials}. The credential is identified by a sequence of map keys which plot a path through the hierarchy of nested maps. For example, given these credentials:
     *
     * ----
     * {
     *     "foo": {
     *         "bar": {
     *             "baz": 99
     *         }
     *     }
     * }
     * ----
     *
     * Then the path to the value 99 is the three strings "foo", "bar", and "baz".
     *
     * @param path the path to the credential in the credentials map, as a sequence of map keys
     * @return the credential at the specified path
     * @throws NoSuchElementException if the path leads to a missing element, or through an element which is not a map
     */
    public Object getCredential(String... path) throws NoSuchElementException {
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

    /**
     * Gets a {@link Certificate} from the {@linkplain #getCredentials() credentials}. The certificate is identified by a {@linkplain #getCredential(String...) path}, which should lead to a https://tools.ietf.org/html/rfc7468[PEM-formatted] https://tools.ietf.org/html/rfc5280[X.509 certificate].
     *
     * @param path the path to the certificate in the credentials map, as a sequence of map keys
     * @return the certificate at the specified path
     * @throws NoSuchElementException if the path leads to a missing element, or through an element which is not a map
     * @throws CertificateException on parsing errors
     */
    public Certificate getCertificate(String... path) throws CertificateException {
        String certificateString = (String) getCredential(path);
        return CryptoParser.parseCertificate(certificateString);
    }

    /**
     * Gets a {@link Key} from the {@linkplain #getCredentials() credentials}. The key is identified by a {@linkplain #getCredential(String...) path}, which should lead to a https://tools.ietf.org/html/rfc7468[PEM-formatted] key for one of the following algorithms:
     *
     * - RSA
     * - Elliptic Curve
     * - DSA
     * - Diffie-Hellman
     *
     * encoded in one of the following ways:
     *
     * - https://tools.ietf.org/html/rfc5208[PKCS#8] private key
     * - https://tools.ietf.org/html/rfc2437[PKCS#1] private RSA key, or equivalent OpenSSL traditional format for algorithms other than RSA
     * - https://tools.ietf.org/html/rfc5280[X.509 SubjectPublicKeyInfo] public key
     * - https://tools.ietf.org/html/rfc2437[PKCS#1] public RSA key
     *
     * @param path the path to the key in the credentials map, as a sequence of map keys
     * @return the key at the specified path
     * @throws NoSuchElementException if the path leads to a missing element, or through an element which is not a map
     * @throws InvalidKeySpecException on parsing errors, or if the key is not of a supported type
     */
    public Key getKey(String... path) throws InvalidKeySpecException {
        String keyString = (String) getCredential(path);
        return CryptoParser.parseKey(keyString);
    }

}
