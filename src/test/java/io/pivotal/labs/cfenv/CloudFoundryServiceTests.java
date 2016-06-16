package io.pivotal.labs.cfenv;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CloudFoundryServiceTests {

    @Test
    public void shouldRevealAUri() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"uri\": \"http://example.org\"}");

        assertThat(service.getUri(), equalTo(URI.create("http://example.org")));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentUri() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{}");

        service.getUri();
    }

    @Test(expected = URISyntaxException.class)
    public void shouldNotRevealAMalformedUri() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"uri\": \"http colon slash slash example dot org\"}");

        service.getUri();
    }

    @Test
    public void shouldGetACredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"name\": \"Gurgiunt Brabtruc\"}");

        assertThat(service.getCredential("name"), equalTo("Gurgiunt Brabtruc"));
    }

    @Test
    public void shouldGetANullCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"name\": null}");

        assertThat(service.getCredential("name"), equalTo(null));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{}");

        service.getCredential("name");
    }

    @Test
    public void shouldGetANestedCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"britain\": {\"king\": {\"name\": \"Gurgiunt Brabtruc\"}}}");

        assertThat(service.getCredential("britain", "king", "name"), equalTo("Gurgiunt Brabtruc"));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnANonexistentNestedCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"britain\": {\"king\": {}}}");

        assertThat(service.getCredential("britain", "king", "name"), equalTo("Gurgiunt Brabtruc"));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowAnExceptionOnAMalformedNestedCredential() throws Exception {
        CloudFoundryService service = serviceWithCredentials("{\"britain\": {\"king\": true}}");

        assertThat(service.getCredential("britain", "king", "name"), equalTo("Gurgiunt Brabtruc"));
    }

    private CloudFoundryService serviceWithCredentials(String credentials) throws CloudFoundryEnvironmentException {
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(TestEnvironment.withVcapServicesContainingService("myservice", credentials));
        return environment.getService("myservice");
    }

}
