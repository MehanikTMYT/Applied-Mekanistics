package me.ramidzkh.mekae2.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

import me.ramidzkh.mekae2.AMItems;
import me.ramidzkh.mekae2.AppliedMekanistics;

import appeng.core.AppEng;

public class ItemModelProvider extends net.neoforged.neoforge.client.model.generators.ItemModelProvider {

    private static final Identifier P2P_TUNNEL_BASE_ITEM = AppEng.makeId("item/p2p_tunnel_base");
    private static final Identifier P2P_TUNNEL_BASE_PART = AppEng.makeId("part/p2p/p2p_tunnel_base");
    private static final Identifier STORAGE_CELL_LED = AppEng.makeId("item/storage_cell_led");
    private static final Identifier PORTABLE_CELL_LED = AppEng.makeId("item/portable_cell_led");
    private static final Identifier PORTABLE_CELL_FIELD = AppEng.makeId("item/portable_cell_screen");
    private static final Identifier P2P_TUNNEL = AppliedMekanistics.id("part/p2p_tunnel_chemical");

    public ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AppliedMekanistics.ID, existingFileHelper);

        existingFileHelper.trackGenerated(P2P_TUNNEL_BASE_ITEM, MODEL);
        existingFileHelper.trackGenerated(P2P_TUNNEL_BASE_PART, MODEL);
        existingFileHelper.trackGenerated(STORAGE_CELL_LED, TEXTURE);
        existingFileHelper.trackGenerated(PORTABLE_CELL_LED, TEXTURE);
        existingFileHelper.trackGenerated(PORTABLE_CELL_FIELD, TEXTURE);
    }

    @Override
    protected void registerModels() {
        var housing = AMItems.CHEMICAL_CELL_HOUSING;

        withExistingParent(housing.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", AppliedMekanistics.id("item/" + housing.getId().getPath()));

        for (var tier : AMItems.Tier.values()) {
            var cell = AMItems.get(tier);
            var portableCell = AMItems.getPortableCell(tier);
            cell(cell, "item/" + cell.getId().getPath());
            portableCell(portableCell, "item/" + portableCell.getId().getPath());
        }

        withExistingParent("item/chemical_p2p_tunnel", P2P_TUNNEL_BASE_ITEM)
                .texture("type", P2P_TUNNEL);
        withExistingParent("part/chemical_p2p_tunnel", P2P_TUNNEL_BASE_PART)
                .texture("type", P2P_TUNNEL);
    }

    private void cell(DeferredItem<?> cell, String background) {
        withExistingParent(cell.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", AppliedMekanistics.id(background))
                .texture("layer1", STORAGE_CELL_LED);
    }

    private void portableCell(DeferredItem<?> portable, String background) {
        withExistingParent(portable.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", AppliedMekanistics.id("item/portable_chemical_cell_housing"))
                .texture("layer1", PORTABLE_CELL_LED)
                .texture("layer2", PORTABLE_CELL_FIELD)
                .texture("layer3", AppliedMekanistics.id(background));
    }
}
