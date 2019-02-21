package bt.tracker.http;

import bt.peer.PeerRegistry;
import bt.protocol.crypto.EncryptionPolicy;
import bt.service.IdentityService;
import org.junit.Test;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class HttpTrackerTest {

    @Test
    public void urlEncodeAllAlphanumeric() {
        HttpTracker httpTracker = new HttpTracker("tempTrackerURL",
                mock(IdentityService.class),
                mock(PeerRegistry.class),
                EncryptionPolicy.PREFER_ENCRYPTED,
                mock(InetAddress.class), 10);
        String expected = "Hello_World";
        String actual = httpTracker.urlEncode(expected.getBytes());
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void urlEncodeSomeAlphanumeric() {
        HttpTracker httpTracker = new HttpTracker("tempTrackerURL",
                mock(IdentityService.class),
                mock(PeerRegistry.class),
                EncryptionPolicy.PREFER_ENCRYPTED,
                mock(InetAddress.class), 10);
        String initExpected = "Hello World";
        String postExpected = "Hello%20World";
        String actual = httpTracker.urlEncode(initExpected.getBytes());
        System.out.println(actual);
        assertEquals(postExpected, actual);
    }
}