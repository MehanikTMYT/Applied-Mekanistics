package me.ramidzkh.mekae2.ae2;

import java.util.function.IntUnaryOperator;

import appeng.api.config.Actionable;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Boundary between AE's long counts/Actionable API and NeoForge's transactional int transfers. */
public final class ChemicalTransfer {
    private ChemicalTransfer() {
    }

    public static int limit(long requested) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, requested));
    }

    // MEStorage has no TransactionContext parameter. Preserve a caller's transaction when a
    // storage bus routes back into this adapter, rather than opening a second root transaction.
    @SuppressWarnings("deprecation")
    public static Transaction open() {
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }

    @SuppressWarnings("deprecation")
    public static void afterCommit(Runnable callback) {
        var tx = Transaction.getCurrentOpenedTransaction();
        if (tx == null) {
            callback.run();
        } else {
            new net.neoforged.neoforge.transfer.transaction.SnapshotJournal<Boolean>() {
                protected Boolean createSnapshot() { return true; }
                protected void revertToSnapshot(Boolean snapshot) { }
                protected void onRootCommit(Boolean snapshot) { callback.run(); }
            }.updateSnapshots(tx);
        }
    }

    public static int insert(ResourceHandler<ChemicalResource> handler, MekanismKey key, long requested,
            Actionable mode) {
        if (requested <= 0) return 0;
        try (var tx = open()) {
            int moved = handler.insert(ChemicalResource.of(key.getStack()), limit(requested), tx);
            if (mode == Actionable.MODULATE) tx.commit();
            return moved;
        }
    }

    public static int extract(ResourceHandler<ChemicalResource> handler, MekanismKey key, long requested,
            Actionable mode) {
        if (requested <= 0) return 0;
        try (var tx = open()) {
            int moved = handler.extract(ChemicalResource.of(key.getStack()), limit(requested), tx);
            if (mode == Actionable.MODULATE) tx.commit();
            return moved;
        }
    }

    /**
     * Reserve the chemical side before touching ME storage. A short ME operation rolls the
     * reservation back and replays only the accepted amount. This also works for one-way tanks:
     * rollback does not require permission to perform the opposite transfer.
     *
     * Like NeoForge's transfer utilities, replay relies on a handler giving the same result
     * from the same state. MEStorage itself does not expose a transaction/rollback API.
     */
    public static int exchange(ResourceHandler<ChemicalResource> handler, MekanismKey key, long requested,
            boolean intoChemicalHandler, IntUnaryOperator simulateWithMe, IntUnaryOperator exchangeWithMe) {
        int offered = limit(requested);
        if (offered == 0) return 0;
        var resource = ChemicalResource.of(key.getStack());
        try (var outer = open()) {
            int accepted;
            while (true) {
                try (var reservation = Transaction.open(outer)) {
                    int reserved = move(handler, resource, offered, intoChemicalHandler, reservation);
                    if (reserved == 0) return 0;
                    int preview = simulateWithMe.applyAsInt(reserved);
                    if (preview < 0 || preview > reserved) {
                        throw new IllegalStateException("ME storage returned an invalid simulation amount: " + preview);
                    }
                    if (preview < reserved) {
                        // No irreversible ME operation has happened yet. Shrink the reservation first,
                        // so transactional storage-bus destinations are not rolled back after accepting.
                        if (preview == 0) return 0;
                        offered = preview;
                        continue;
                    }
                    accepted = exchangeWithMe.applyAsInt(reserved);
                    if (accepted < 0 || accepted > reserved) {
                        throw new IllegalStateException("ME storage returned an invalid transfer amount: " + accepted);
                    }
                    if (accepted == reserved) {
                        reservation.commit();
                        outer.commit();
                        return accepted;
                    }
                }
                if (accepted > 0) {
                    int replayed = move(handler, resource, accepted, intoChemicalHandler, outer);
                    if (replayed != accepted) {
                        throw new IllegalStateException("Chemical handler violated transactional replay: "
                                + replayed + " != " + accepted);
                    }
                }
                outer.commit();
                return accepted;
            }
        }
    }

    private static int move(ResourceHandler<ChemicalResource> handler, ChemicalResource resource,
            int amount, boolean insert, TransactionContext tx) {
        return insert ? handler.insert(resource, amount, tx) : handler.extract(resource, amount, tx);
    }
}
