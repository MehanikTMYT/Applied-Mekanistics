package me.ramidzkh.mekae2;



import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import me.ramidzkh.mekae2.ae2.AMChemicalStackRenderer;

import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.PortableCellItem;

public class AppliedMekanisticsClient {

    public static void initialize(IEventBus bus) {
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event)->event.enqueueWork(AppliedMekanisticsClient::initializeModels));
        AMChemicalStackRenderer.initialize(bus);
    }
    public static void initializeModels() {
        for (var tier : AMItems.Tier.values()) {
            var cell = AMItems.get(tier);
            var portable = AMItems.getPortableCell(tier);
            var path = cell.getId().getPath();

            appeng.api.client.StorageCellModels.registerModel(cell.get(), AppliedMekanistics.id("block/drive/cells/" + path));
            appeng.api.client.StorageCellModels.registerModel(portable.get(), AppliedMekanistics.id("block/drive/cells/" + path));
        }
    }

}
