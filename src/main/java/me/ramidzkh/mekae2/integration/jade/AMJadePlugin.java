package me.ramidzkh.mekae2.integration.jade;

import net.minecraft.resources.Identifier;

import mekanism.api.MekanismAPI;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import appeng.api.AECapabilities;

/**
 * Plugin to remove the mekanism-added chemical handler lines for interfaces and pattern providers.
 */
@WailaPlugin
public class AMJadePlugin implements IWailaPlugin {

    private static final Identifier CHEMICAL = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID,
            "chemical");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addTooltipCollectedCallback((box, accessor) -> {
            if (accessor instanceof BlockAccessor block) {
                var cap = AECapabilities.GENERIC_INTERNAL_INV.getCapability(block.getLevel(), block.getPosition(),
                        block.getBlockState(), block.getBlockEntity(), block.getSide());

                if (cap != null) {
                    box.getTooltip().remove(CHEMICAL);
                }
            }
        });
    }
}
