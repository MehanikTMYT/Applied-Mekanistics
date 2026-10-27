package me.ramidzkh.mekae2.ae2;

import org.jetbrains.annotations.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import me.ramidzkh.mekae2.MekCapabilities;
import mekanism.api.chemical.ChemicalResource;
import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;

public class ChemicalContainerItemStrategy implements ContainerItemStrategy<MekanismKey, ResourceHandler<ChemicalResource>> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var handler = ItemAccess.forStack(stack).getCapability(MekCapabilities.CHEMICAL.item());
        if (handler != null) {
            for (int i = 0; i < handler.size(); i++) {
                var resource = handler.getResource(i);
                if (!resource.isEmpty() && handler.getAmountAsLong(i) > 0) {
                    return new GenericStack(MekanismKey.of(resource.toStack(1)), handler.getAmountAsLong(i));
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable ResourceHandler<ChemicalResource> findCarriedContext(Player player, AbstractContainerMenu menu) {
        return ItemAccess.forPlayerCursor(player, menu).getCapability(MekCapabilities.CHEMICAL.item());
    }

    @Override
    public @Nullable ResourceHandler<ChemicalResource> findPlayerSlotContext(Player player, int slot) {
        return ItemAccess.forPlayerSlot(player, slot).getCapability(MekCapabilities.CHEMICAL.item());
    }

    @Override
    public long extract(ResourceHandler<ChemicalResource> context, MekanismKey what, long amount, Actionable mode) {
        return ChemicalTransfer.extract(context, what, amount, mode);
    }

    @Override
    public long insert(ResourceHandler<ChemicalResource> context, MekanismKey what, long amount, Actionable mode) {
        return ChemicalTransfer.insert(context, what, amount, mode);
    }

    @Override
    public void playFillSound(Player player, MekanismKey what) {
        player.playSound(SoundEvents.BUCKET_FILL, 1.0F, 1.0F);
    }

    @Override
    public void playEmptySound(Player player, MekanismKey what) {
        player.playSound(SoundEvents.BUCKET_EMPTY, 1.0F, 1.0F);
    }

    @Override
    public @Nullable GenericStack getExtractableContent(ResourceHandler<ChemicalResource> context) {
        try (var tx = ChemicalTransfer.open()) {
            var stack = ResourceHandlerUtil.extractFirst(context, resource -> true, Integer.MAX_VALUE, tx);
            return stack == null ? null : new GenericStack(MekanismKey.of(stack.resource().toStack(1)), stack.amount());
        }
    }
}
