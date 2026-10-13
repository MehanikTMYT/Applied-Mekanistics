package me.ramidzkh.mekae2;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import me.ramidzkh.mekae2.ae2.*;
import appeng.api.config.Actionable;
import appeng.api.stacks.*;
import appeng.api.storage.StorageCells;
import appeng.api.networking.security.IActionSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
@EventBusSubscriber(modid="appmek")
public class PortSmoke {
 static int ticks;
 static boolean done;
 static void check(boolean x,String label){if(!x)throw new AssertionError(label);System.out.println("APPMEK PASS: "+label);}
 @SubscribeEvent public static void start(ServerTickEvent.Post event) {
  if(!Boolean.getBoolean("appmek.smoke")||done||++ticks<5)return;
  done=true;
  try {
   var level=event.getServer().overworld();
   var pos=new BlockPos(0,150,0);level.setBlockAndUpdate(pos,mekanism.common.registries.MekanismBlocks.ULTIMATE_CHEMICAL_TANK.get().defaultBlockState());
   var tankTile=(mekanism.common.tile.TileEntityChemicalTank)level.getBlockEntity(pos);
   for(var side:mekanism.api.RelativeSide.values())tankTile.getConfig().getConfig(mekanism.common.lib.transmitter.TransmissionType.CHEMICAL).setDataType(mekanism.common.tile.component.config.DataType.INPUT_OUTPUT,side);
   var handler=level.getCapability(MekCapabilities.CHEMICAL.block(),pos,Direction.NORTH);check(handler!=null,"native chemical capability connects");
   var source=IActionSource.empty();int extras=0;
   for(var chemical:mekanism.api.MekanismAPI.CHEMICAL_REGISTRY) {
    var id=mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getKey(chemical);
    if(!id.getNamespace().equals("mekanism_extras"))continue;
    var resource=mekanism.api.chemical.ChemicalResource.of(chemical);var key=MekanismKey.of(resource.toStack(1));extras++;
    var ops=event.getServer().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
    var encoded=MekanismKey.CODEC.encodeStart(ops,key).getOrThrow();check(key.equals(MekanismKey.CODEC.parse(ops,encoded).getOrThrow()),"Extras chemical key codec: "+id);
    var pattern=appeng.api.crafting.PatternDetailsHelper.encodeProcessingPattern(java.util.List.of(new GenericStack(key,250)),java.util.List.of(new GenericStack(key,125)));
    var decoded=appeng.api.crafting.PatternDetailsHelper.decodePattern(pattern,level);check(decoded!=null&&decoded.getPrimaryOutput().what().equals(key),"Extras chemical processing pattern: "+id);
    if(chemical.isRadioactive())continue;
    var cellStack=new ItemStack(AMItems.CHEMICAL_CELL_64K.get());var cell=StorageCells.getCellInventory(cellStack,null);check(cell!=null,"chemical storage cell handler");
    check(cell.insert(key,1000,Actionable.SIMULATE,source)==1000,"cell accepts Extras chemical simulation");check(cell.extract(key,1000,Actionable.SIMULATE,source)==0,"simulation preserves cell");
    check(cell.insert(key,1000,Actionable.MODULATE,source)==1000,"cell stores Extras chemical");cell.persist();
    var reopened=StorageCells.getCellInventory(cellStack.copy(),null);check(reopened.extract(key,1000,Actionable.MODULATE,source)==1000,"cell reload preserves Extras chemical");
    var facade=new me.ramidzkh.mekae2.ae2.stack.ChemicalHandlerFacade(handler,false,()->{});
    check(facade.insert(key,250,Actionable.SIMULATE,source)==250,"native tank insert simulation");check(handler.getAmountAsLong(0)==0,"native simulation rolls back");
    check(facade.insert(key,250,Actionable.MODULATE,source)==250,"native tank insert commits");
    check(facade.extract(key,250,Actionable.MODULATE,source)==250,"native tank extract conserves chemical");
   }
   check(extras>0,"tested all Extras chemical registry entries: "+extras);
   var itemStack=new ItemStack(mekanism.common.registries.MekanismBlocks.ULTIMATE_CHEMICAL_TANK.asItem());
   var itemStrategy=new ChemicalContainerItemStrategy();
   var oxygen=MekanismKey.of(mekanism.api.chemical.ChemicalResource.of(mekanism.common.registries.MekanismChemicals.OXYGEN).toStack(1));
   check(itemStrategy.insert(itemStack,oxygen,500,Actionable.SIMULATE)==500,"item tank insertion preview accepts native chemical");
   check(itemStrategy.getContainedStack(itemStack)==null,"item tank insertion preview leaves item unchanged");
   check(itemStrategy.insert(itemStack,oxygen,500,Actionable.MODULATE)==500,"item tank native insertion commits");
   check(itemStrategy.extract(itemStack,oxygen,500,Actionable.SIMULATE)==500,"item tank extraction simulation succeeds");
   check(itemStrategy.getContainedStack(itemStack).amount()==500,"item tank extraction simulation preserves amount");
   check(itemStrategy.extract(itemStack,oxygen,500,Actionable.MODULATE)==500,"item tank extraction commits exactly");
   check(AMItems.CHEMICAL_P2P_TUNNEL.get().createPart().getExposedApi()!=null,"chemical P2P initializes transactional handler");
   System.out.println("APPMEK FUNCTIONAL COMPLETE");
  }catch(Throwable t){System.out.println("APPMEK FUNCTIONAL FAILED");t.printStackTrace();}finally{event.getServer().halt(false);}
 }
}

