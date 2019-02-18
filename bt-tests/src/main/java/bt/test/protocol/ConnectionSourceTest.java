package bt.net;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import bt.CountingThreadFactory;
import bt.metainfo.TorrentId;
import bt.runtime.Config;
import bt.service.IRuntimeLifecycleBinder;
import bt.service.RuntimeLifecycleBinder;
import java.util.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.mockito.Mockito.*;

public class ConnectionSourceTest {
    /**
       Given an already existing connection return that one.
     **/
    @Test
    public void acceptExistingConnection()
	throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
	HashSet hs = mock(HashSet.class);
	PeerConnectionFactory pcf = mock(PeerConnectionFactory.class);
	PeerConnectionPool pcp = mock(PeerConnectionPool.class);
	when(pcp.getConnection(isA(ConnectionKey.class))).thenReturn(mock(PeerConnection.class));
	RuntimeLifecycleBinder rlb = mock(RuntimeLifecycleBinder.class);
	Config c = mock(Config.class);
	ConnectionSource cs = new ConnectionSource(hs, pcf, pcp, rlb, c);
	Peer p = mock(Peer.class);
	TorrentId t = mock(TorrentId.class);
	CompletableFuture<ConnectionResult> fut = cs.getConnectionAsync(p, t);
	assertTrue(fut.get().isSuccess());
    }

}
