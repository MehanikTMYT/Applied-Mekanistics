package me.ramidzkh.mekae2;

import net.minecraft.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import me.ramidzkh.mekae2.ae2.ChemicalP2PTunnelPart;
import me.ramidzkh.mekae2.item.ChemicalPortableCellItem;
import me.ramidzkh.mekae2.item.ChemicalStorageCell;


import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.items.AEBaseItem;
import appeng.items.materials.MaterialItem;
import appeng.items.parts.PartItem;

import appeng.items.storage.StorageTier;

public class AMItems {

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            AppliedMekanistics.ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppliedMekanistics.ID);

    public static void initialize(IEventBus bus) {
        TABS.register(bus);
        ITEMS.register(bus);
    }

    private static Item basic(Item.Properties props) {
        return new MaterialItem(props);
    }

    private static Item.Properties properties() {
        return new Item.Properties();
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(AMText.CREATIVE_TAB.formatted())
                    .icon(() -> new ItemStack(AMItems.CHEMICAL_CELL_64K.get()))
                    .displayItems((params, output) -> {
                        for (var entry : ITEMS.getEntries()) {
                            var item = entry.get();

                            // For block items, the block controls the creative tab
                            if (item instanceof AEBaseBlockItem baseItem
                                    && baseItem.getBlock() instanceof AEBaseBlock baseBlock) {
                                baseBlock.addToMainCreativeTab(params, output);
                            } else if (item instanceof AEBaseItem baseItem) {
                                baseItem.addToMainCreativeTab(params, output);
                            } else {
                                output.accept(item);
                            }
                        }
                    })
                    .build());

    public static final DeferredItem<Item> CHEMICAL_CELL_HOUSING = ITEMS.registerItem("chemical_cell_housing",
            AMItems::basic);

    public static final DeferredItem<Item> CHEMICAL_CELL_1K = ITEMS.registerItem("chemical_storage_cell_1k",
            props -> new ChemicalStorageCell(props.stacksTo(1), StorageTier.SIZE_1K));
    public static final DeferredItem<Item> CHEMICAL_CELL_4K = ITEMS.registerItem("chemical_storage_cell_4k",
            props -> new ChemicalStorageCell(props.stacksTo(1), StorageTier.SIZE_4K));
    public static final DeferredItem<Item> CHEMICAL_CELL_16K = ITEMS.registerItem("chemical_storage_cell_16k",
            props -> new ChemicalStorageCell(props.stacksTo(1), StorageTier.SIZE_16K));
    public static final DeferredItem<Item> CHEMICAL_CELL_64K = ITEMS.registerItem("chemical_storage_cell_64k",
            props -> new ChemicalStorageCell(props.stacksTo(1), StorageTier.SIZE_64K));
    public static final DeferredItem<Item> CHEMICAL_CELL_256K = ITEMS.registerItem("chemical_storage_cell_256k",
            props -> new ChemicalStorageCell(props.stacksTo(1), StorageTier.SIZE_256K));

    public static final DeferredItem<Item> PORTABLE_CHEMICAL_CELL_1K = ITEMS.registerItem(
            "portable_chemical_cell_1k",
            props -> new ChemicalPortableCellItem(18, AMMenus.PORTABLE_CHEMICAL_CELL_TYPE, StorageTier.SIZE_1K,
                    props.stacksTo(1), 0x80caff));
    public static final DeferredItem<Item> PORTABLE_CHEMICAL_CELL_4K = ITEMS.registerItem(
            "portable_chemical_cell_4k",
            props -> new ChemicalPortableCellItem(18, AMMenus.PORTABLE_CHEMICAL_CELL_TYPE, StorageTier.SIZE_4K,
                    props.stacksTo(1), 0x80caff));
    public static final DeferredItem<Item> PORTABLE_CHEMICAL_CELL_16K = ITEMS.registerItem(
            "portable_chemical_cell_16k",
            props -> new ChemicalPortableCellItem(18, AMMenus.PORTABLE_CHEMICAL_CELL_TYPE, StorageTier.SIZE_16K,
                    props.stacksTo(1), 0x80caff));
    public static final DeferredItem<Item> PORTABLE_CHEMICAL_CELL_64K = ITEMS.registerItem(
            "portable_chemical_cell_64k",
            props -> new ChemicalPortableCellItem(18, AMMenus.PORTABLE_CHEMICAL_CELL_TYPE, StorageTier.SIZE_64K,
                    props.stacksTo(1), 0x80caff));
    public static final DeferredItem<Item> PORTABLE_CHEMICAL_CELL_256K = ITEMS.registerItem(
            "portable_chemical_cell_256k",
            props -> new ChemicalPortableCellItem(18, AMMenus.PORTABLE_CHEMICAL_CELL_TYPE, StorageTier.SIZE_256K,
                    props.stacksTo(1), 0x80caff));

    public static final DeferredItem<PartItem<ChemicalP2PTunnelPart>> CHEMICAL_P2P_TUNNEL = Util.make(() -> {

        return ITEMS.registerItem("chemical_p2p_tunnel",
                props -> new PartItem<>(props, ChemicalP2PTunnelPart.class, ChemicalP2PTunnelPart::new));
    });

    public static DeferredItem<Item> get(Tier tier) {
        return switch (tier) {
            case _1K -> CHEMICAL_CELL_1K;
            case _4K -> CHEMICAL_CELL_4K;
            case _16K -> CHEMICAL_CELL_16K;
            case _64K -> CHEMICAL_CELL_64K;
            case _256K -> CHEMICAL_CELL_256K;
        };
    }

    public static DeferredItem<Item> getPortableCell(Tier tier) {
        return switch (tier) {
            case _1K -> PORTABLE_CHEMICAL_CELL_1K;
            case _4K -> PORTABLE_CHEMICAL_CELL_4K;
            case _16K -> PORTABLE_CHEMICAL_CELL_16K;
            case _64K -> PORTABLE_CHEMICAL_CELL_64K;
            case _256K -> PORTABLE_CHEMICAL_CELL_256K;
        };
    }

    public enum Tier {
        _1K,
        _4K,
        _16K,
        _64K,
        _256K
    }
}

