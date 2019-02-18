package bt.net;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.*;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.nio.file.*;
import java.io.*;

public class ConnectionSourceTest {

    static void reg(String id, Integer branchCount) {
	try {
	    Files.write(Paths.get(System.getProperty("user.home")+"/"+id+".report"), (Integer.toString(branchCount)+"\n").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
	}catch (IOException e) {
	    // Nothing to do.
	}
    }
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

    /**
       Ensure that unreachable peers are rejected.
     **/
    @Test
    public void rejectUnreachable() 
	throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
	Set<PeerConnectionAcceptor> hs = new HashSet<PeerConnectionAcceptor>();
	PeerConnectionAcceptor pca = mock(PeerConnectionAcceptor.class);
	PeerConnectionFactory pcf = mock(PeerConnectionFactory.class);
	PeerConnectionPool pcp = mock(PeerConnectionPool.class);
	PeerConnection mocked = mock(PeerConnection.class);
	when(pcp.getConnection(isA(ConnectionKey.class))).thenReturn(null);
	RuntimeLifecycleBinder rlb = mock(RuntimeLifecycleBinder.class);
	Config c = mock(Config.class);
	ConnectionSource cs = new ConnectionSource(hs, pcf, pcp, rlb, c);
	try {
	    // Guarantee the ban
	    Field field = ConnectionSource.class.getDeclaredField("unreachablePeers");
	    field.setAccessible(true);
	    ConcurrentMap<Peer, Long> unreachablePeers = (ConcurrentMap<Peer, Long>)field.get(cs);
	    when(unreachablePeers.get(isA(Peer.class))).thenReturn(Long.MAX_VALUE);
	    Peer p = mock(Peer.class);
	    TorrentId t = mock(TorrentId.class);
	    CompletableFuture<ConnectionResult> fut = cs.getConnectionAsync(p, t);
	    try {
		ConnectionResult cr = fut.get();
		assertTrue(!cr.isSuccess());
		assertTrue(cr.getMessage().get().equals("Peer is unreachable"));
	    } catch(Exception e) {
		assertTrue(false);
	    }
	}
	catch(Exception e) {
	    assertTrue(false);
	}
    }

}
