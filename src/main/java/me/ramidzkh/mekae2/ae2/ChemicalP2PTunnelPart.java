package me.ramidzkh.mekae2.ae2;
import appeng.api.parts.IPartItem;
import appeng.parts.p2p.ResourceHandlerP2PTunnelPart;
import mekanism.api.chemical.ChemicalResource;
import me.ramidzkh.mekae2.MekCapabilities;
public class ChemicalP2PTunnelPart extends ResourceHandlerP2PTunnelPart<ChemicalP2PTunnelPart,ChemicalResource> {
 public ChemicalP2PTunnelPart(IPartItem<?> item) { super(item,MekCapabilities.CHEMICAL.block(),ChemicalResource.EMPTY,MekanismKeyType.TYPE); }
}
