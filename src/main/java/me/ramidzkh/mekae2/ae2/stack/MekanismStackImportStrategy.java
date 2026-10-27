package me.ramidzkh.mekae2.ae2.stack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import me.ramidzkh.mekae2.MekCapabilities;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import me.ramidzkh.mekae2.ae2.MekanismKeyType;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import me.ramidzkh.mekae2.ae2.ChemicalTransfer;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;

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

            long allowed = Math.min(remainingTransferAmount, handler.getAmountAsLong(i));
            long acceptable = inv.getInventory().insert(resource, ChemicalTransfer.limit(allowed),
                    Actionable.SIMULATE, context.getActionSource());
            int inserted = ChemicalTransfer.exchange(handler, resource, acceptable, false,
                    reserved -> (int) inv.getInventory().insert(resource, reserved, Actionable.SIMULATE,
                            context.getActionSource()),
                    reserved -> (int) inv.getInventory().insert(resource, reserved, Actionable.MODULATE,
                            context.getActionSource()));
            if (inserted > 0) {
                long perOperation = MekanismKeyType.TYPE.getAmountPerOperation();
                context.reduceOperationsRemaining((inserted + perOperation - 1) / perOperation);
                remainingTransferAmount = Math.max(0, context.getOperationsRemaining()) * perOperation;
            }
        }

        return false;
    }
}
