package io.pivotal.labs.cfenv.crypto;

import org.junit.Test;

import javax.crypto.interfaces.DHPrivateKey;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CryptoParserTest {

    @Test
    public void shouldParseAnX509Certificate() throws Exception {
        String certificateString = "" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIBxTCCAW+gAwIBAgIJAP6oSMcKL5/RMA0GCSqGSIb3DQEBBQUAMCMxITAfBgNV\n" +
                "BAsTGElBbUFDZXJ0aWZpY2F0ZUF1dGhvcml0eTAeFw0xNjA2MTYxNTA2MzhaFw0x\n" +
                "NjA3MTYxNTA2MzhaMCMxITAfBgNVBAsTGElBbUFDZXJ0aWZpY2F0ZUF1dGhvcml0\n" +
                "eTBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCzDE9WLL7gvS8jKqoEDfg0fo11eV1K\n" +
                "76aoN7+LakBBJlLEMAgO9G1/dYscb9BsSH3lvKrHP3VqOdNxXkyoEeSzAgMBAAGj\n" +
                "gYUwgYIwHQYDVR0OBBYEFOpbFIPCq891mF/ara3TbvBFJQ+WMFMGA1UdIwRMMEqA\n" +
                "FOpbFIPCq891mF/ara3TbvBFJQ+WoSekJTAjMSEwHwYDVQQLExhJQW1BQ2VydGlm\n" +
                "aWNhdGVBdXRob3JpdHmCCQD+qEjHCi+f0TAMBgNVHRMEBTADAQH/MA0GCSqGSIb3\n" +
                "DQEBBQUAA0EArVBMPE+epXTh0DnI9cgkf4+qLcgHAS2k8gC3/cfSc3gCG1v15DbN\n" +
                "xr0CoWh17P9Vb3X1CkOLMhyHYpD3tOpKgw==\n" +
                "-----END CERTIFICATE-----";

        Certificate certificate = CryptoParser.parseCertificate(certificateString);

        assertThat(((X509Certificate) certificate).getSubjectDN().getName(), equalTo("OU=IAmACertificateAuthority"));
    }

    @Test
    public void shouldParseAPKCS8RSAPrivateKey() throws Exception {
        String keyString = "" +
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEA5qUWM0y4rQQWZdr1\n" +
                "D4bxiq1jZH1NSuYn21LApnDnt+Q4/CQ4/OIdh/hZljgFKq1oJg4ToeDubCtSLyR3\n" +
                "L89qDwIDAQABAkEAo6ekFwZrS6jI08EHfdr8bLAXBGi8fVbOFRukwvT+FkYqyc7G\n" +
                "0ja/6hQaO+kIA3ToVlKpyn/9mYTt/R/bXofMgQIhAP6nu78Qcn/Uy3qz/H5iifcf\n" +
                "ZRorAh4DqmjKZ5NGEDqhAiEA59zk2ExZUcpc4n65xVFHBFCT2ystB9NdzXicvNA9\n" +
                "lq8CIADL64Vser81njFTEM4gZsgUHA/Z5JbNciIDyBEo3fIhAiEA0ZITNn4r7YWf\n" +
                "Vwl8GCFSs0+xlNP9q6kci++MnA0M3fkCIHsxjW/AkmNozt8AsfIpBAGQ9teGLDPE\n" +
                "8WVZgWlRIjvA\n" +
                "-----END PRIVATE KEY-----";

        Key key = CryptoParser.parseKey(keyString);

        assertThat(((RSAPrivateKey) key).getPrivateExponent(), hasToString(startsWith("8571299855")));
    }

    @Test
    public void shouldParseAPKCS8ECPrivateKey() throws Exception {
        String keyString = "" +
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgcPpehQM6yoYp8/ZX\n" +
                "ouOlHTYO0WR+SneBWnBB07XgzKGhRANCAAT5PnuxmD5FygLB3x6sO/AEdmtCPKNA\n" +
                "BR867n9Hx2eVFxhxrb8g0ZAXNsYyrLKxNZV3YQVS816O5MN996XKA6bD\n" +
                "-----END PRIVATE KEY-----";

        Key key = CryptoParser.parseKey(keyString);

        assertThat(((ECPrivateKey) key).getParams().getOrder(), hasToString(startsWith("1157920892")));
    }

    @Test
    public void shouldParseAPKCS8DSAPrivateKey() throws Exception {
        String keyString = "" +
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIHHAgEAMIGpBgcqhkjOOAQBMIGdAkEA/8/aIwYwD4TUzee5AQvz4Bk24nAozkCJ\n" +
                "OOK/WEtLmlfdK3pWeZ7WttD65kJFgFZE1hDi0D0ipuXwFIJhqzoMcQIVAORLzKnx\n" +
                "1wfBs3Mngrh3XfyqOmUlAkEAvjDa+zB5mfAfIaYOgpuJzEGnLnj9VGLZEGVC/w3l\n" +
                "5ML3PblMCLMniHzIT3UQjQtTwOfiWa7RdAFrmjU7OQxJCQQWAhQETyaxnsWJO293\n" +
                "DZPkGsuQaq2xdw==\n" +
                "-----END PRIVATE KEY-----";

        Key key = CryptoParser.parseKey(keyString);

        assertThat(((DSAPrivateKey) key).getX(), hasToString(startsWith("2460109266")));
    }

    @Test
    public void shouldParseAPKCS8DiffieHellmanAPrivateKey() throws Exception {
        String keyString = "" +
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIGcAgEAMFMGCSqGSIb3DQEDATBGAkEAshUWtJRfsLnR5aFCGNa22yN2hxI5g95M\n" +
                "gd9ef3/JxE3m6bDawc5elyPGmKTdNSJbLFlXvlWOccZoMCNMDnrHYwIBAgRCAkBz\n" +
                "dbu4cSepe0ZleXxM0EwuVO9TJcYPM3s3aZL7NQl5xbLA0FIJrKI3ectYvxklhdS7\n" +
                "weJkKpkB0lUG2Rvmmt7q\n" +
                "-----END PRIVATE KEY-----";

        Key key = CryptoParser.parseKey(keyString);

        assertThat(((DHPrivateKey) key).getX(), hasToString(startsWith("6047125407")));
    }

}
