/*
 * Copyright (c) 2016—2017 Andrei Tomashpolskiy and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bt.torrent.messaging;

import bt.data.Bitfield;
import bt.net.Peer;
import bt.runtime.Config;
import bt.torrent.BitfieldBasedStatistics;
import bt.torrent.selector.PieceSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class Assignments {

    private static final Logger LOGGER = LoggerFactory.getLogger(Assignments.class);

    private static final int MAX_ASSIGNED_PIECES_PER_PEER = 50;

    private Config config;

    private Bitfield bitfield;
    private PieceSelector selector;
    private BitfieldBasedStatistics pieceStatistics;

    private Set<Integer> assignedPieces;
    private Map<Peer, Assignment> assignments;
    private Map<Peer, LinkedList<Integer>> peers;

    private Random random;

    public Assignments(Bitfield bitfield, PieceSelector selector, BitfieldBasedStatistics pieceStatistics, Config config) {
        this.bitfield = bitfield;
        this.selector = selector;
        this.pieceStatistics = pieceStatistics;
        this.config = config;

        this.assignedPieces = new HashSet<>();
        this.assignments = new HashMap<>();
        this.peers = new HashMap<>();

        this.random = new Random(System.currentTimeMillis());
    }

    public Assignment get(Peer peer) {
        return assignments.get(peer);
    }

    public void remove(Assignment assignment) {
        assignment.abort();
        assignments.remove(assignment.getPeer());
        assignedPieces.remove(assignment.getPiece());
    }

    public int count() {
        return assignments.size();
    }

    public int workersCount() {
        return peers.size();
    }

    public Optional<Assignment> assign(Peer peer) {
        LinkedList<Integer> pieces = peers.get(peer);
        if (pieces == null || pieces.isEmpty()) {
            CoverMe.reg("assign", 0);
            return Optional.empty();
        }

        boolean endgame = isEndgame();

        StringBuilder buf = LOGGER.isTraceEnabled() ? new StringBuilder() : null;
        if (LOGGER.isTraceEnabled()) {
            CoverMe.reg("assign", 1);
            buf.append("Trying to claim next assignment for peer ");
            buf.append(peer);
            buf.append(". Number of remaining pieces: ");
            buf.append(bitfield.getPiecesRemaining());
            buf.append(", number of pieces in progress: ");
            buf.append(assignedPieces.size());
            buf.append(", endgame: " + endgame);
            buf.append(". ");
        }

        Optional<Integer> selectedPiece;
        if (endgame) {
            CoverMe.reg("assign", 2);
            // take random piece to minimize number of pieces
            // requested from different peers at the same time
            Integer pieceIndex = pieces.remove(random.nextInt(pieces.size()));
            selectedPiece = Optional.of(pieceIndex);
        } else {
            CoverMe.reg("assign", 3);
            Integer piece;
            boolean assigned = true;
            Iterator<Integer> iter = pieces.iterator();
            do {
                CoverMe.reg("assign", 4);
                piece = iter.next();
                if (bitfield.isComplete(piece)) {
                    CoverMe.reg("assign", 5);
                    iter.remove();
                    if (LOGGER.isTraceEnabled()) {
                        CoverMe.reg("assign", 6);
                        buf.append("Checking next piece in queue: {" + piece + "}; piece is completed. ");
                    }
                    continue;
                }
                assigned = assignedPieces.contains(piece);
                if (assigned && LOGGER.isTraceEnabled()) {
                    CoverMe.reg("assign", 7);
                    buf.append("Checking next piece in queue: {" + piece + "}; piece is assigned. ");
                }
            } while (assigned && iter.hasNext());

            if (!assigned) {
                CoverMe.reg("assign", 8);
                iter.remove();
            }
            selectedPiece = assigned ? Optional.empty() : Optional.of(piece);
        }

        if (LOGGER.isTraceEnabled()) {
            CoverMe.reg("assign", 9);
            if (selectedPiece.isPresent()) {
                CoverMe.reg("assign", 10);
                buf.append(" => Assigning piece #");
                buf.append(selectedPiece.get());
                buf.append(" to current peer");
            } else {
                CoverMe.reg("assign", 11);
                buf.append(" => No pieces to assign.");
            }
            LOGGER.trace(buf.toString());
        }

        return selectedPiece.isPresent() ? Optional.of(assign(peer, selectedPiece.get())) : Optional.empty();
    }

    private boolean isEndgame() {
        // if all remaining pieces are requested,
        // that would mean that we have entered the "endgame" mode
        return bitfield.getPiecesRemaining() <= assignedPieces.size();
    }

    private Assignment assign(Peer peer, Integer piece) {
        Assignment assignment = new Assignment(peer, piece, config.getMaxPieceReceivingTime());
        assignments.put(peer, assignment);
        assignedPieces.add(piece);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Assigning piece #{} to peer: {}", piece, peer);
        }
        return assignment;
    }

    /**
     * Updates the lists of interesting pieces for the provided peers.
     *
     * @return Collection of peers that have interesting pieces and can be given an assignment
     */
    // TODO: select from seeders first
    public Set<Peer> update(Set<Peer> ready, Set<Peer> choking) {
        Iterator<Integer> suggested = selector.getNextPieces(pieceStatistics).iterator();
        if (LOGGER.isTraceEnabled()) {
            CoverMe.reg("update", 0);
            LOGGER.trace("Updating assignments. Piece selector has more pieces: {}, number of ready peers: {}, number of assigned peers: {}",
                    suggested.hasNext(), ready.size(), assignments.size());
        }
        final Set<Peer> readyPeers = new HashSet<>(ready);
        while (suggested.hasNext() && readyPeers.size() > 0) {
            CoverMe.reg("update", 1);
            Integer piece = suggested.next();

            final Iterator<Peer> iter = readyPeers.iterator();
            while (iter.hasNext()) {
                CoverMe.reg("update", 2);
                Peer peer = iter.next();
                Optional<Bitfield> peerBitfield = pieceStatistics.getPeerBitfield(peer);
                if (!peerBitfield.isPresent()) {
                    CoverMe.reg("update", 3);
                    iter.remove();
                    continue;
                }
                LinkedList<Integer> queue = peers.get(peer);
                if (queue != null && queue.size() > MAX_ASSIGNED_PIECES_PER_PEER) {
                    CoverMe.reg("update", 4);
                    iter.remove();
                    continue;
                }
                boolean hasPiece = peerBitfield.get().isVerified(piece);
                if (hasPiece) {
                    CoverMe.reg("update", 5);
                    if (queue == null) {
                        CoverMe.reg("update", 6);
                        queue = new LinkedList<>();
                        peers.put(peer, queue);
                    }
                    if (!queue.contains(piece)) {
                        CoverMe.reg("update", 7);
                        queue.add(piece);
                        if (queue.size() > MAX_ASSIGNED_PIECES_PER_PEER) {
                            CoverMe.reg("update", 8);
                            iter.remove();
                        }
                        if (LOGGER.isTraceEnabled()) {
                            CoverMe.reg("update", 9);
                            LOGGER.trace("Adding piece #{} to peer's queue: {}. Number of pieces in peer's queue: {}",
                                    piece, peer, queue.size());
                        }
                    }
                }
            }
        }

        Iterator<Map.Entry<Peer, LinkedList<Integer>>> iter = peers.entrySet().iterator();
        while (iter.hasNext()) {
            CoverMe.reg("update", 10);
            Map.Entry<Peer, LinkedList<Integer>> e = iter.next();
            Peer peer = e.getKey();
            LinkedList<Integer> pieces = e.getValue();

            if (!ready.contains(peer) || pieces.isEmpty()) {
                CoverMe.reg("update", 11);
                iter.remove();
            }
        }

        Set<Peer> result = new HashSet<>(peers.keySet());
        for (Peer peer : choking) {
            CoverMe.reg("update", 12);
            if (hasInterestingPieces(peer)) {
                CoverMe.reg("update", 13);
                result.add(peer);
            }
        }

        return result;
    }

    private boolean hasInterestingPieces(Peer peer) {
        Optional<Bitfield> peerBitfieldOptional = pieceStatistics.getPeerBitfield(peer);
        if (!peerBitfieldOptional.isPresent()) {
            return false;
        }
        BitSet peerBitfield = peerBitfieldOptional.get().getBitmask();
        BitSet localBitfield = bitfield.getBitmask();
        peerBitfield.andNot(localBitfield);
        return peerBitfield.cardinality() > 0;
    }
}
