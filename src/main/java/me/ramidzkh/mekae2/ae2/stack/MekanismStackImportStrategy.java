package me.ramidzkh.mekae2.ae2.stack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import me.ramidzkh.mekae2.MekCapabilities;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import me.ramidzkh.mekae2.ae2.MekanismKeyType;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import me.ramidzkh.mekae2.ae2.ChemicalTransfer;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.core.AELog;

public class MekanismStackImportStrategy implements StackImportStrategy {

    private final BlockCapabilityCache<ResourceHandler<ChemicalResource>, Direction> cache;

    public MekanismStackImportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(MekCapabilities.CHEMICAL.block(), level, fromPos, fromSide);
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(MekanismKeyType.TYPE)) {
            return false;
        }

        var handler = cache.getCapability();

        if (handler == null) {
            return false;
        }

        var remainingTransferAmount = context.getOperationsRemaining()
                * (long) MekanismKeyType.TYPE.getAmountPerOperation();

        var inv = context.getInternalStorage();

        // Try to find an extractable resource that fits our filter
        for (var i = 0; i < handler.size() && remainingTransferAmount > 0; i++) {
            var stack = handler.getResource(i).toStack(1);
            var resource = MekanismKey.of(stack);

            if (resource == null
                    // Regard a filter that is set on the bus
                    || context.isInFilter(resource) == context.isInverted()) {
                continue;
            }

            // Check how much of *this* resource we can actually insert into the network, it might be 0
            // if the cells are partitioned or there's not enough types left, etc.
            var amountForThisResource = inv.getInventory().insert(resource, remainingTransferAmount,
                    Actionable.SIMULATE,
                    context.getActionSource());

            // Try to simulate-extract it
            var amount = ChemicalTransfer.extract(handler,resource,amountForThisResource,Actionable.MODULATE);

            if (amount > 0) {
                var inserted = inv.getInventory().insert(resource, amount, Actionable.MODULATE,
                        context.getActionSource());

                if (inserted < amount) {
                    // Be nice and try to give the overflow back
                    var leftover = amount - inserted;
                    leftover -= ChemicalTransfer.insert(handler,resource,leftover,Actionable.MODULATE);

                    if (leftover > 0) {
                        AELog.warn("Extracted %dx%s from adjacent storage and voided it because network refused insert",
                                leftover, resource);
                    }
                }

                var opsUsed = Math.max(1, inserted / MekanismKeyType.TYPE.getAmountPerOperation());
                context.reduceOperationsRemaining(opsUsed);
                remainingTransferAmount -= inserted;
            }
        }

        return false;
    }
}
