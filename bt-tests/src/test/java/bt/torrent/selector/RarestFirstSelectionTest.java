package bt.torrent.selector;

import bt.test.torrent.selector.UpdatablePieceStatistics;
import bt.torrent.RarestFirstSelectionStrategy;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;

public class RarestFirstSelectionTest {
    private static final Predicate<Integer> acceptAllValidator = i -> true;

    @Test
    public void testSelection() {
        UpdatablePieceStatistics statistics = new UpdatablePieceStatistics(8);

        statistics.setPiecesCount(0, 3, 0, 2, 1, 0, 0, 0);
        Assert.assertTrue(RarestFirstSelectionStrategy.randomizedRarest().getNextPieces(statistics, 2, acceptAllValidator).length > 0);
    }
}
