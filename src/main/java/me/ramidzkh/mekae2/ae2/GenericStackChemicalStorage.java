package me.ramidzkh.mekae2.ae2;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
public record GenericStackChemicalStorage(GenericInternalInventory inv) implements ResourceHandler<ChemicalResource> {
 public int size(){return inv.size();}
 public ChemicalResource getResource(int slot){return inv.getKey(slot) instanceof MekanismKey k?ChemicalResource.of(k.getStack()):ChemicalResource.EMPTY;}
 public long getAmountAsLong(int slot){return inv.getAmount(slot);}
 public long getCapacityAsLong(int slot,ChemicalResource resource){return inv.getCapacity(MekanismKeyType.TYPE);}
 public boolean isValid(int slot,ChemicalResource r){return !r.isEmpty() && inv.isAllowedIn(slot,MekanismKey.of(r.toStack(1)));}
 public int insert(int slot,ChemicalResource r,int amount,TransactionContext tx){if(r.isEmpty()||amount<=0)return 0;inv.updateSnapshots(tx);return (int)inv.insert(slot,MekanismKey.of(r.toStack(1)),amount,Actionable.MODULATE);}
 public int extract(int slot,ChemicalResource r,int amount,TransactionContext tx){if(r.isEmpty()||amount<=0||!r.equals(getResource(slot)))return 0;inv.updateSnapshots(tx);return (int)inv.extract(slot,MekanismKey.of(r.toStack(1)),amount,Actionable.MODULATE);}
}
