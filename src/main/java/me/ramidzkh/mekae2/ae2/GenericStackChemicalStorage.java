package me.ramidzkh.mekae2.ae2;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.helpers.ResourceConversion;
import appeng.helpers.externalstorage.GenericStackInvHandler;
import mekanism.api.chemical.ChemicalResource;

/** Uses AE's snapshot journal and commit-only inventory notifications, including slot filters. */
public final class GenericStackChemicalStorage extends GenericStackInvHandler<ChemicalResource> {
    private static final ResourceConversion<ChemicalResource> CONVERSION = new ResourceConversion<>() {
        public AEKeyType getKeyType() {
            return MekanismKeyType.TYPE;
        }

        public ChemicalResource getVariant(AEKey key) {
            return key instanceof MekanismKey chemical ? ChemicalResource.of(chemical.getStack()) : ChemicalResource.EMPTY;
        }

        public AEKey getKey(ChemicalResource resource) {
            return resource.isEmpty() ? null : MekanismKey.of(resource.toStack(1));
        }

        public long getBaseSlotSize(ChemicalResource resource) {
            return 4L * MekanismKeyType.TYPE.getAmountPerUnit();
        }
    };

    public GenericStackChemicalStorage(GenericInternalInventory inv) {
        super(CONVERSION, MekanismKeyType.TYPE, inv);
    }
}
