package me.ramidzkh.mekae2.ae2.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import me.ramidzkh.mekae2.MekCapabilities;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import me.ramidzkh.mekae2.ae2.ChemicalTransfer;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;

public class MekanismStackExportStrategy implements StackExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MekanismStackExportStrategy.class);
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

        var inv = context.getInternalStorage();

        var extracted = StorageHelper.poweredExtraction(
                context.getEnergySource(),
                inv.getInventory(),
                what,
                amount,
                context.getActionSource(),
                Actionable.SIMULATE);

        var wasInserted = ChemicalTransfer.insert(storage,mekanismKey,extracted,Actionable.SIMULATE);

        if (wasInserted > 0) {
            extracted = StorageHelper.poweredExtraction(
                    context.getEnergySource(),
                    inv.getInventory(),
                    what,
                    wasInserted,
                    context.getActionSource(),
                    Actionable.MODULATE);

            wasInserted = ChemicalTransfer.insert(storage,mekanismKey,extracted,Actionable.MODULATE);

            if (wasInserted < extracted) {
                // Be nice and try to give the overflow back
                var leftover = extracted - wasInserted;
                leftover -= inv.getInventory().insert(what, leftover, Actionable.MODULATE, context.getActionSource());

                if (leftover > 0) {
                    LOGGER.error("Storage export: adjacent block unexpectedly refused insert, voided {}x{}", leftover,
                            what);
                }
            }
        }

        return wasInserted;
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
