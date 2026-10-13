package me.ramidzkh.mekae2.ae2.stack;

import net.minecraft.network.chat.Component;

import me.ramidzkh.mekae2.AMText;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import me.ramidzkh.mekae2.ae2.ChemicalTransfer;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;

public record ChemicalHandlerFacade(ResourceHandler<ChemicalResource> handler, boolean extractableOnly,
        Runnable changeListener) implements MEStorage {

    /**
     * Clamp reported values to avoid overflows when amounts get too close to Long.MAX_VALUE.
     */
    private static final long MAX_REPORTED_AMOUNT = 1L << 42;

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof MekanismKey key)) {
            return 0;
        }

        var inserted = ChemicalTransfer.insert(handler,key,amount,mode);

        if (inserted > 0 && mode == Actionable.MODULATE) {
            this.changeListener.run();
        }

        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof MekanismKey key)) {
            return 0;
        }

        var extracted = ChemicalTransfer.extract(handler,key,amount,mode);

        if (extracted > 0 && mode == Actionable.MODULATE) {
            this.changeListener.run();
        }

        return extracted;
    }

    @Override
    public Component getDescription() {
        return GuiText.ExternalStorage.text(AMText.CHEMICALS);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var i = 0; i < handler.size(); i++) {
            var stack = handler.getResource(i).toStack(1);
            var key = MekanismKey.of(stack);

            if (key == null) {
                continue;
            }

            // Skip resources that cannot be extracted if that filter was enabled
            if (extractableOnly && ChemicalTransfer.extract(handler,key,1,Actionable.SIMULATE)==0) {
                continue;
            }

            out.add(key, Math.min(handler.getAmountAsLong(i), MAX_REPORTED_AMOUNT));
        }
    }
}
