package io.pivotal.labs.cfenv;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

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
    public void shouldParseTheUriFromASystemService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json"));
        URI uri = environment.getUri("myapp-db");

        assertThat(uri, equalTo(URI.create("postgres://dxktcwjm:xxxxxxxx@babar.elephantsql.com:5432/dxktcwjm")));
    }

    @Test
    public void shouldParseTheUriFromAUserProvidedService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("user_provided_service.json"));
        URI uri = environment.getUri("search-engine");

        assertThat(uri, equalTo(URI.create("https://duckduckgo.com/")));
    }

    @Test
    public void shouldTolerateAServiceWithNoUri() throws Exception {
        new CloudFoundryEnvironment(environmentWithVcapServices("syslog.json"));
    }

    @Test
    public void shouldTolerateAServiceWithAMalformedUri() throws Exception {
        new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json", json -> json.replace("postgres://", "postgres:||")));
    }

    @Test(expected = URISyntaxException.class)
    public void shouldNotRevealAMalformedUri() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environmentWithVcapServices("system_service.json", json -> json.replace("postgres://", "postgres:||")));

        environment.getUri("myapp-db");
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

}
