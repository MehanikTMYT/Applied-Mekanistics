package me.ramidzkh.mekae2.ae2;
import appeng.api.config.Actionable;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
public final class ChemicalTransfer {
 public static int insert(ResourceHandler<ChemicalResource> handler,MekanismKey key,long requested,Actionable mode) {
  if(requested<=0)return 0;
  try(var tx=Transaction.openRoot()) { int moved=handler.insert(ChemicalResource.of(key.getStack()),(int)Math.min(Integer.MAX_VALUE,requested),tx);if(mode==Actionable.MODULATE)tx.commit();return moved; }
 }
 public static int extract(ResourceHandler<ChemicalResource> handler,MekanismKey key,long requested,Actionable mode) {
  if(requested<=0)return 0;
  try(var tx=Transaction.openRoot()) { int moved=handler.extract(ChemicalResource.of(key.getStack()),(int)Math.min(Integer.MAX_VALUE,requested),tx);if(mode==Actionable.MODULATE)tx.commit();return moved; }
 }
}
