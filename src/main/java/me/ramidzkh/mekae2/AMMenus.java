package me.ramidzkh.mekae2;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.InitScreens;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;

public class AMMenus {

    public static final MenuType<MEStorageMenu> PORTABLE_CHEMICAL_CELL_TYPE = MenuTypeBuilder
            .create(MEStorageMenu::new, IPortableTerminal.class)
            .build(AppliedMekanistics.id("portable_chemical_cell"));

    @SuppressWarnings("RedundantTypeArguments")
    public static void initialize(IEventBus bus) {
        bus.addListener((RegisterMenuScreensEvent event) -> {
            InitScreens.<MEStorageMenu, MEStorageScreen<MEStorageMenu>>register(event, PORTABLE_CHEMICAL_CELL_TYPE,
                    MEStorageScreen::new, "/screens/terminals/portable_chemical_cell.json");
        });
    }
}
