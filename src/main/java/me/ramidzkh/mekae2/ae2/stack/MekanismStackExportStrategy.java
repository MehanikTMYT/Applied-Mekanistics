package me.ramidzkh.mekae2.ae2.stack;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import me.ramidzkh.mekae2.MekCapabilities;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import me.ramidzkh.mekae2.ae2.ChemicalTransfer;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;

public class MekanismStackExportStrategy implements StackExportStrategy {

    private final BlockCapabilityCache<ResourceHandler<ChemicalResource>, Direction> cache;

    public MekanismStackExportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(MekCapabilities.CHEMICAL.block(), level, fromPos, fromSide);
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof MekanismKey mekanismKey)) {
            return 0;
        }

        var storage = cache.getCapability();

        if (storage == null) {
            return 0;
        }

        var inv = context.getInternalStorage().getInventory();
        long available = StorageHelper.poweredExtraction(context.getEnergySource(), inv, what,
                ChemicalTransfer.limit(amount), context.getActionSource(), Actionable.SIMULATE);
        return ChemicalTransfer.exchange(storage, mekanismKey, available, true,
                reserved -> (int) StorageHelper.poweredExtraction(context.getEnergySource(), inv, what,
                        reserved, context.getActionSource(), Actionable.SIMULATE),
                reserved -> (int) StorageHelper.poweredExtraction(context.getEnergySource(), inv, what,
                        reserved, context.getActionSource(), Actionable.MODULATE));
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof MekanismKey mekanismKey)) {
            return 0;
        }

        var storage = cache.getCapability();

        if (storage == null) {
            return 0;
        }

        return ChemicalTransfer.insert(storage,mekanismKey,amount,mode);
    }
}
