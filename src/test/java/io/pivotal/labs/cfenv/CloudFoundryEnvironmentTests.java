package io.pivotal.labs.cfenv;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

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
    public void shouldParseASystemService() throws Exception {
        String json = ResourceUtils.loadResource("system_service.json");

        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));
        Set<String> serviceNames = environment.getServiceNames();

        assertThat(serviceNames, contains("myapp-db"));
    }

    @Test
    public void shouldParseAUserProvidedService() throws Exception {
        String json = ResourceUtils.loadResource("user_provided_service.json");

        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));
        Set<String> serviceNames = environment.getServiceNames();

        assertThat(serviceNames, contains("search-engine"));
    }

    private Environment environment(String name, String value) {
        Map<String, String> environment = Collections.singletonMap(name, value);
        return environment::get;
    }

}
