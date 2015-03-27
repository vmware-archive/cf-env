package io.pivotal.labs.cfenv;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CloudFoundryEnvironmentTests {

    @Test
    public void shouldRequireOnlyAnEnvironmentToConstruct() throws Exception {
        Environment environment = environment("VCAP_SERVICES", "{}");

        new CloudFoundryEnvironment(environment);
    }

    @Test(expected = CloudFoundryEnvironmentException.class)
    public void shouldThrowAnExceptionOnAMissingVariable() throws Exception {
        new CloudFoundryEnvironment(environment("NOT_VCAP_SERVICES", "{}"));
    }

    @Test(expected = CloudFoundryEnvironmentException.class)
    public void shouldThrowAnExceptionOnInvalidJson() throws Exception {
        new CloudFoundryEnvironment(environment("VCAP_SERVICES", "<json>ceci n'est pas de JSON</json>"));
    }

    @Test
    public void shouldDetectASystemService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json"));
        Set<String> serviceNames = environment.getServiceNames();

        assertThat(serviceNames, contains("myapp-db"));
    }

    @Test
    public void shouldDetectAUserProvidedService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("user_provided_service.json"));
        Set<String> serviceNames = environment.getServiceNames();

        assertThat(serviceNames, contains("search-engine"));
    }

    @Test
    public void shouldTolerateAServiceWithNoUri() throws Exception {
        new CloudFoundryEnvironment(environmentWithVcapServices("syslog.json"));
    }

    @Test
    public void shouldTolerateAServiceWithAMalformedUri() throws Exception {
        new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json", json -> json.replace("postgres://", "postgres:||")));
    }

    @Test
    public void shouldParseAllTheDetailsOfASystemService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json"));
        CloudFoundryService service = environment.getService("myapp-db");

        assertThat(service.getName(), equalTo("myapp-db"));
        assertThat(service.getLabel(), equalTo("elephantsql"));
        assertThat(service.getPlan(), equalTo("turtle"));
        assertThat(service.getTags(), containsInAnyOrder("Data Stores", "Cloud Databases", "Developer Tools", "Data Store", "postgresql", "relational", "New Product"));
        assertThat(service.getUri(), equalTo(URI.create("postgres://dxktcwjm:xxxxxxxx@babar.elephantsql.com:5432/dxktcwjm")));
    }

    @Test
    public void shouldParseAllTheDetailsOfAUserProvidedService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("user_provided_service.json"));
        CloudFoundryService service = environment.getService("search-engine");

        assertThat(service.getName(), equalTo("search-engine"));
        assertThat(service.getLabel(), equalTo("user-provided"));
        assertThat(service.getPlan(), nullValue());
        assertThat(service.getTags(), empty());
        assertThat(service.getUri(), equalTo(URI.create("https://duckduckgo.com/")));
    }

    @Test
    public void shouldParseTheCredentialsOfAService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json"));
        CloudFoundryService service = environment.getService("myapp-db");

        assertThat(service.getCredentials(), entries(containsInAnyOrder(entry("uri", "postgres://dxktcwjm:xxxxxxxx@babar.elephantsql.com:5432/dxktcwjm"), entry("max_conns", "5"))));
    }

    @Test
    public void shouldParseCredentialsContainingVariousTypes() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("syslog.json", json -> json.replace("{}",
                "{" +
                        "\"boolean\": true," +
                        "\"int\": 23," +
                        "\"float\": 3.14," +
                        "\"list\": [1, 2, 3]," +
                        "\"map\": {\"k\": \"v\"}" +
                        "}")));
        CloudFoundryService service = environment.getService("false-syslog");

        assertThat(service.getCredentials(), entries(containsInAnyOrder(
                entry("boolean", true),
                entry("int", 23),
                entry("float", 3.14),
                entry("list", Arrays.asList(1, 2, 3)),
                entry("map", Collections.singletonMap("k", "v")))));
    }

    @Test(expected = URISyntaxException.class)
    public void shouldNotRevealAMalformedUri() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json", json -> json.replace("postgres://", "postgres:||")));

        environment.getService("myapp-db").getUri();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", "{}"));

        environment.getService("no such service");
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentUri() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json", json -> json.replace("uri", "href")));

        environment.getService("myapp-db").getUri();
    }

    private Environment environmentWithVcapServices(String jsonResourceName) throws IOException {
        return environmentWithVcapServices(jsonResourceName, Function.identity());
    }

    private Environment environmentWithVcapServices(String jsonResourceName, Function<String, String> tweak) throws IOException {
        String json = ResourceUtils.loadResource(jsonResourceName);
        String tweakedJson = tweak.apply(json);
        return environment("VCAP_SERVICES", tweakedJson);
    }

    private Environment environment(String name, String value) {
        Map<String, String> environment = Collections.singletonMap(name, value);
        return environment::get;
    }

    private <K, V> Matcher<Map<K, V>> entries(Matcher<Iterable<? extends Map.Entry<K, V>>> matcher) {
        return new FeatureMatcher<Map<K, V>, Set<Map.Entry<K, V>>>(matcher, "entries", "entries") {
            @Override
            protected Set<Map.Entry<K, V>> featureValueOf(Map<K, V> actual) {
                return actual.entrySet();
            }
        };
    }

    private <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
    }

}
