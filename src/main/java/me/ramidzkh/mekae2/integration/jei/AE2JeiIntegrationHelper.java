package me.ramidzkh.mekae2.integration.jei;

import appeng.client.api.integrations.jei.IngredientConverters;

public class AE2JeiIntegrationHelper {
    public static void register() {
        IngredientConverters.register(new ChemicalIngredientConverter());
    }
}
