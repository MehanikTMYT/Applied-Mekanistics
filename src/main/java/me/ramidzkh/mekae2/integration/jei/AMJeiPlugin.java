package me.ramidzkh.mekae2.integration.jei;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import me.ramidzkh.mekae2.AppliedMekanistics;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;

@JeiPlugin
public class AMJeiPlugin implements IModPlugin {
    public static mezz.jei.api.runtime.IJeiRuntime runtime;
    @Override public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime value){runtime=value;}
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(AppliedMekanistics.ID, "jei_plugin");
    }

    public AMJeiPlugin() {
        if (isModLoaded("ae2")) {
            AE2JeiIntegrationHelper.register();
        }
    }

    private static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }
}
