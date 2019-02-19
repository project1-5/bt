package bt.torrent.messaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CoverMe {
    /**
       Register branch of id id with some branchCount
    **/
    static void reg(String id, Integer branchCount) {
	try {
	    Files.write(Paths.get(System.getProperty("user.home")+"/"+id+".report"), (Integer.toString(branchCount)+"\n").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
	}catch (IOException e) {
	    // Nothing to do.
	}
    }
}
