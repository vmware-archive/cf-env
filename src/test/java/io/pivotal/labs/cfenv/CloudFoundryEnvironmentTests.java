package io.pivotal.labs.cfenv;

import org.junit.Test;

import java.net.URI;
import java.util.*;

import static io.pivotal.labs.cfenv.EntriesMatcher.entries;
import static io.pivotal.labs.cfenv.EntriesMatcher.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CloudFoundryEnvironmentTests {

    @Test
    public void shouldRequireOnlyAnEnvironmentToConstruct() throws Exception {
        Environment environment = TestEnvironment.with("VCAP_SERVICES", "{}");

        new CloudFoundryEnvironment(environment);
    }

    @Test(expected = CloudFoundryEnvironmentException.class)
    public void shouldThrowAnExceptionOnAMissingVariable() throws Exception {
        new CloudFoundryEnvironment(TestEnvironment.with("NOT_VCAP_SERVICES", "{}"));
    }

    @Test(expected = CloudFoundryEnvironmentException.class)
    public void shouldThrowAnExceptionOnInvalidJson() throws Exception {
        new CloudFoundryEnvironment(TestEnvironment.with("VCAP_SERVICES", "<json>ceci n'est pas de JSON</json>"));
    }

    @Test
    public void shouldDetectASystemService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("system_service.json"));
        Set<String> serviceNames = environment.getServiceNames();

        assertThat(serviceNames, contains("myapp-db"));
    }

    @Test
    public void shouldDetectAUserProvidedService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("user_provided_service.json"));
        Set<String> serviceNames = environment.getServiceNames();

        assertThat(serviceNames, contains("search-engine"));
    }

    @Test
    public void shouldTolerateAServiceWithNoUri() throws Exception {
        new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("syslog.json"));
    }

    @Test
    public void shouldTolerateAServiceWithAMalformedUri() throws Exception {
        new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("system_service.json", json -> json.replace("postgres://", "postgres:||")));
    }

    @Test
    public void shouldParseAllTheDetailsOfASystemService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("system_service.json"));
        CloudFoundryService service = environment.getService("myapp-db");

        assertThat(service.getName(), equalTo("myapp-db"));
        assertThat(service.getLabel(), equalTo("elephantsql"));
        assertThat(service.getPlan(), equalTo("turtle"));
        assertThat(service.getTags(), containsInAnyOrder("Data Stores", "Cloud Databases", "Developer Tools", "Data Store", "postgresql", "relational", "New Product"));
    }

    @Test
    public void shouldParseAllTheDetailsOfAUserProvidedService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("user_provided_service.json"));
        CloudFoundryService service = environment.getService("search-engine");

        assertThat(service.getName(), equalTo("search-engine"));
        assertThat(service.getLabel(), equalTo("user-provided"));
        assertThat(service.getPlan(), nullValue());
        assertThat(service.getTags(), empty());
    }

    @Test
    public void shouldParseTheCredentialsOfAService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("system_service.json"));
        CloudFoundryService service = environment.getService("myapp-db");

        assertThat(service.getCredentials(), entries(containsInAnyOrder(
                entry("uri", "postgres://dxktcwjm:xxxxxxxx@babar.elephantsql.com:5432/dxktcwjm"),
                entry("max_conns", "5")
        )));
    }

    @Test
    public void shouldParseCredentialsContainingVariousTypes() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesFrom("syslog.json", json -> json.replace("{}",
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

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentService() throws Exception {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.with("VCAP_SERVICES", "{}"));

        environment.getService("no such service");
    }

}
