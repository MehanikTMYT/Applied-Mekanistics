package me.ramidzkh.mekae2.ae2;

import appeng.api.parts.IPartItem;
import appeng.parts.p2p.ResourceHandlerP2PTunnelPart;
import appeng.util.InsertionOnlyResourceHandler;
import mekanism.api.chemical.ChemicalResource;
import me.ramidzkh.mekae2.MekCapabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ChemicalP2PTunnelPart extends ResourceHandlerP2PTunnelPart<ChemicalP2PTunnelPart, ChemicalResource> {
    public ChemicalP2PTunnelPart(IPartItem<?> item) {
        super(item, MekCapabilities.CHEMICAL.block(), ChemicalResource.EMPTY, MekanismKeyType.TYPE);
        // Keep the corrected distribution local to this addon; do not modify the shared AE2 fork.
        inputHandler = new InsertionOnlyResourceHandler<>(ChemicalResource.EMPTY) {
            @Override
            public int insert(ChemicalResource resource, int amount, TransactionContext tx) {
                TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                var outputs = getOutputs();
                if (outputs.isEmpty() || amount == 0) return 0;
                int total = distribute(amount, outputs.size(), (index, offered) -> {
                    try (var guard = outputs.get(index).getAdjacentCapability()) {
                        return guard.get().insert(resource, offered, tx);
                    }
                });
                deductTransportCost(total, MekanismKeyType.TYPE, tx);
                return total;
            }
        };
    }

    /** Split the original budget, carrying rejected amounts forward without exceeding that budget. */
    static int distribute(int amount, int outputCount, java.util.function.IntBinaryOperator insert) {
        if (amount <= 0 || outputCount <= 0) return 0;
        int share = amount / outputCount;
        int remainder = amount % outputCount;
        int total = 0;
        for (int index = 0; index < outputCount; index++) {
            int offered = Math.min(amount - total, share + remainder);
            int inserted = insert.applyAsInt(index, offered);
            if (inserted < 0 || inserted > offered) throw new IllegalStateException("Invalid P2P insertion count");
            remainder = offered - inserted;
            total += inserted;
        }
        return total;
    }
}
