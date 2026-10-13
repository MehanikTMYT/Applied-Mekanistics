package me.ramidzkh.mekae2.ae2.stack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import me.ramidzkh.mekae2.MekCapabilities;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import me.ramidzkh.mekae2.ae2.ChemicalTransfer;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.storage.MEStorage;

public class MekanismExternalStorageStrategy implements ExternalStorageStrategy {

    private final BlockCapabilityCache<ResourceHandler<ChemicalResource>, Direction> cache;

    public MekanismExternalStorageStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(MekCapabilities.CHEMICAL.block(), level, fromPos, fromSide);
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var storage = cache.getCapability();

        if (storage == null) {
            return null;
        }

        return new ChemicalHandlerFacade(storage, extractableOnly, injectOrExtractCallback);
    }
}
