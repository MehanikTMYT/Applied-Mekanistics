package me.ramidzkh.mekae2;
import net.neoforged.fml.ModList;
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
   var hasExtras=ModList.get().isLoaded("mekanism_extras");
   var testNamespace=hasExtras?"mekanism_extras":"mekanism";
   if(!hasExtras)System.out.println("APPMEK NOTE: mekanism_extras not loaded (fleet queue stage 5) - exercising base mekanism chemicals instead");
   for(var chemical:mekanism.api.MekanismAPI.CHEMICAL_REGISTRY) {
    var id=mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getKey(chemical);
    if(!id.getNamespace().equals(testNamespace))continue;
    var resource=mekanism.api.chemical.ChemicalResource.of(chemical);if(resource.isEmpty())continue;var key=MekanismKey.of(resource.toStack(1));extras++;
    var ops=event.getServer().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
    var encoded=MekanismKey.CODEC.encodeStart(ops,key).getOrThrow();check(key.equals(MekanismKey.CODEC.parse(ops,encoded).getOrThrow()),"Extras chemical key codec: "+id);
    var pattern=appeng.api.crafting.PatternDetailsHelper.encodeProcessingPattern(java.util.List.of(new GenericStack(key,250)),java.util.List.of(new GenericStack(key,125)));
    var decoded=appeng.api.crafting.PatternDetailsHelper.decodePattern(pattern,level);check(decoded!=null&&decoded.getPrimaryOutput().what().equals(key),"Extras chemical processing pattern: "+id);
    if(chemical.isRadioactive()) {
     for(var tier:AMItems.Tier.values()) {
      for(var item:java.util.List.of(AMItems.get(tier).get(),AMItems.getPortableCell(tier).get())) {
       var radioactiveCell=StorageCells.getCellInventory(new ItemStack(item),null);
       check(radioactiveCell.insert(key,1000,Actionable.SIMULATE,source)==0,"radioactive simulation blocked: "+item+" "+id);
       check(radioactiveCell.insert(key,1000,Actionable.MODULATE,source)==0,"radioactive insertion blocked: "+item+" "+id);
       check(radioactiveCell.extract(key,1000,Actionable.SIMULATE,source)==0,"radioactive cell remains empty");
       var radiation=mekanism.common.config.MekanismConfig.general.radiationEnabled;
       boolean enabled=radiation.get();
       try {
        radiation.set(false);
        check(radioactiveCell.insert(key,1000,Actionable.MODULATE,source)==0,"radioactive cell protection survives disabled radiation");
       } finally {radiation.set(enabled);}

      }
     }
     continue;
    }
    var cellStack=new ItemStack(AMItems.CHEMICAL_CELL_64K.get());var cell=StorageCells.getCellInventory(cellStack,null);check(cell!=null,"chemical storage cell handler");
    check(cell.insert(key,1000,Actionable.SIMULATE,source)==1000,"cell accepts Extras chemical simulation");check(cell.extract(key,1000,Actionable.SIMULATE,source)==0,"simulation preserves cell");
    check(cell.insert(key,1000,Actionable.MODULATE,source)==1000,"cell stores Extras chemical");cell.persist();
    var reopened=StorageCells.getCellInventory(cellStack.copy(),null);check(reopened.extract(key,1000,Actionable.MODULATE,source)==1000,"cell reload preserves Extras chemical");
    var facade=new me.ramidzkh.mekae2.ae2.stack.ChemicalHandlerFacade(handler,false,()->{});
    check(facade.insert(key,250,Actionable.SIMULATE,source)==250,"native tank insert simulation");check(handler.getAmountAsLong(0)==0,"native simulation rolls back");
    check(facade.insert(key,250,Actionable.MODULATE,source)==250,"native tank insert commits");
    check(facade.extract(key,250,Actionable.MODULATE,source)==250,"native tank extract conserves chemical");
   }
   check(extras>0,"tested all "+testNamespace+" chemical registry entries: "+extras);
   var itemStack=new ItemStack(mekanism.common.registries.MekanismBlocks.ULTIMATE_CHEMICAL_TANK.asItem());
   var itemStrategy=new ChemicalContainerItemStrategy();
   var itemHandler=net.neoforged.neoforge.transfer.access.ItemAccess.forStack(itemStack).getCapability(MekCapabilities.CHEMICAL.item());
   var oxygen=MekanismKey.of(mekanism.api.chemical.ChemicalResource.of(mekanism.common.registries.MekanismChemicals.OXYGEN).toStack(1));
   check(itemStrategy.insert(itemHandler,oxygen,500,Actionable.SIMULATE)==500,"item tank insertion preview accepts native chemical");
   check(itemStrategy.getContainedStack(itemStack)==null,"item tank insertion preview leaves item unchanged");
   check(itemStrategy.insert(itemHandler,oxygen,500,Actionable.MODULATE)==500,"item tank native insertion commits");
   check(itemStrategy.extract(itemHandler,oxygen,500,Actionable.SIMULATE)==500,"item tank extraction simulation succeeds");
   check(itemStrategy.getContainedStack(itemStack).amount()==500,"item tank extraction simulation preserves amount");
   check(itemStrategy.extract(itemHandler,oxygen,500,Actionable.MODULATE)==500,"item tank extraction commits exactly");
   check(AMItems.CHEMICAL_P2P_TUNNEL.get().createPart().getExposedApi()!=null,"chemical P2P initializes transactional handler");
   transactionalChecks(handler,oxygen);
   p2pChecks();
   qioChecks(level);
   busChecks(level,pos,handler,oxygen);
   java.nio.file.Files.writeString(java.nio.file.Path.of("appmek-smoke-result.txt"), "PASS\n");
   System.out.println("APPMEK FUNCTIONAL COMPLETE");
  }catch(Throwable t){System.out.println("APPMEK FUNCTIONAL FAILED");t.printStackTrace();}finally{event.getServer().halt(false);}
 }

 static void transactionalChecks(net.neoforged.neoforge.transfer.ResourceHandler<mekanism.api.chemical.ChemicalResource> tank, MekanismKey key) {
  var chemical=mekanism.api.chemical.ChemicalResource.of(key.getStack());
  check(ChemicalTransfer.insert(tank,key,-1,Actionable.MODULATE)==0,"negative insert rejected");
  check(ChemicalTransfer.extract(tank,key,Long.MIN_VALUE,Actionable.MODULATE)==0,"negative extract rejected");
  check(ChemicalTransfer.limit(Long.MAX_VALUE)==Integer.MAX_VALUE,"long requests saturate without overflow");
  try(var tx=ChemicalTransfer.open()) {
   check(ChemicalTransfer.insert(tank,key,100,Actionable.MODULATE)==100,"nested insertion succeeds");
  }
  check(tank.getAmountAsLong(0)==0,"outer abort undoes nested commit");
  check(ChemicalTransfer.exchange(tank,key,500,true,n->Math.min(n,125),n->Math.min(n,125))==125,"export short ME extraction");
  check(tank.getAmountAsLong(0)==125,"export commits only extracted quantity");
  check(ChemicalTransfer.exchange(tank,key,500,false,n->Math.min(n,25),n->Math.min(n,25))==25,"import short ME acceptance");
  check(tank.getAmountAsLong(0)==100,"import rolls back unaccepted remainder");
  check(ChemicalTransfer.exchange(tank,key,500,false,n->0,n->0)==0,"full network refuses import");
  check(tank.getAmountAsLong(0)==100,"full network preserves source");
  check(ChemicalTransfer.extract(tank,key,Long.MAX_VALUE,Actionable.MODULATE)==100,"oversized extraction cannot become negative");
  var notifications=new java.util.concurrent.atomic.AtomicInteger();
  var inv=new appeng.helpers.externalstorage.GenericStackInv(notifications::incrementAndGet,2);
  inv.setCapacity(MekanismKeyType.TYPE,1000);
  var generic=new GenericStackChemicalStorage(inv);
  try(var tx=ChemicalTransfer.open()) {check(generic.insert(chemical,500,tx)==500,"interface stages chemical");}
  check(inv.getAmount(0)==0&&notifications.get()==0,"interface abort restores state without notifications");
  try(var tx=ChemicalTransfer.open()) {generic.insert(chemical,500,tx);tx.commit();}
  check(inv.getAmount(0)==500&&notifications.get()==1,"interface commit notifies once");
  try(var outer=ChemicalTransfer.open()) {
   generic.extract(chemical,200,outer);
   try(var inner=net.neoforged.neoforge.transfer.transaction.Transaction.open(outer)){generic.extract(chemical,200,inner);}
   check(inv.getAmount(0)==300,"inner abort preserves outer state");
  }
  check(inv.getAmount(0)==500&&notifications.get()==1,"interface nested abort preserves count and notifications");
  var destinationInv=new appeng.helpers.externalstorage.GenericStackInv(null,1);
  destinationInv.setCapacity(MekanismKeyType.TYPE,50);
  var destination=new me.ramidzkh.mekae2.ae2.stack.ChemicalHandlerFacade(new GenericStackChemicalStorage(destinationInv),false,()->{});
  ChemicalTransfer.insert(tank,key,100,Actionable.MODULATE);
  var source=IActionSource.empty();
  check(ChemicalTransfer.exchange(tank,key,100,false,
    n->(int)destination.insert(key,n,Actionable.SIMULATE,source),
    n->(int)destination.insert(key,n,Actionable.MODULATE,source))==50,"import into partially full transactional storage bus");
  check(tank.getAmountAsLong(0)==50&&destinationInv.getAmount(0)==50,"two transactional endpoints conserve total");
  ChemicalTransfer.extract(tank,key,50,Actionable.MODULATE);
  var oneWay=new net.neoforged.neoforge.transfer.DelegatingResourceHandler<mekanism.api.chemical.ChemicalResource>(tank) {
   @Override public int extract(mekanism.api.chemical.ChemicalResource r,int n,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return 0;}
   @Override public int extract(int slot,mekanism.api.chemical.ChemicalResource r,int n,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return 0;}
  };
  check(ChemicalTransfer.exchange(oneWay,key,100,true,n->n,n->25)==25,"short extraction rolls back insertion-only target");
  check(tank.getAmountAsLong(0)==25,"one-way target retains exact accepted amount");
  ChemicalTransfer.extract(tank,key,25,Actionable.MODULATE);

 }

 static void qioChecks(net.minecraft.server.level.ServerLevel level) {
  var drivePos=new BlockPos(4,150,0);
  level.setBlockAndUpdate(drivePos,mekanism.common.registries.MekanismBlocks.QIO_DRIVE_ARRAY.get().defaultBlockState());
  var drives=(mekanism.common.tile.qio.TileEntityQIODriveArray)level.getBlockEntity(drivePos);
  var frequency=new mekanism.common.content.qio.QIOFrequency("appmek-smoke",java.util.UUID.randomUUID(),mekanism.api.security.SecurityMode.PUBLIC);
  frequency.update(level,drives);
  var drive=net.neoforged.neoforge.transfer.item.ItemResource.of(mekanism.common.registries.MekanismItems.SUPERMASSIVE_QIO_DRIVE.get());
  drives.getDriveSlots().get(0).setContents(drive,1,null);
  frequency.addDrive(new mekanism.common.content.qio.QIODriveData.QIODriveKey(drives,0),drive);
  var dashboardPos=new BlockPos(6,150,0);
  level.setBlockAndUpdate(dashboardPos,mekanism.common.registries.MekanismBlocks.QIO_DASHBOARD.get().defaultBlockState());
  var dashboard=(mekanism.common.tile.qio.TileEntityQIODashboard)level.getBlockEntity(dashboardPos);
  var adapter=new me.ramidzkh.mekae2.qio.QioStorageAdapter<>(dashboard,Direction.SOUTH,null) {
   @Override public mekanism.api.inventory.qio.IQIOFrequency getFrequency(){return frequency;}
  };
  var item=AEItemKey.of(net.minecraft.world.item.Items.IRON_INGOT);
  var source=IActionSource.empty();
  long count=(long)Integer.MAX_VALUE+42;
  check(adapter.insert(item,count,Actionable.SIMULATE,source)==count,"QIO supports long insertion simulation");
  check(frequency.getStored(item.toResource())==0,"QIO simulation rolls back native drive");
  check(adapter.insert(item,count,Actionable.MODULATE,source)==count,"QIO inserts beyond integer range");
  check(adapter.extract(item,count,Actionable.SIMULATE,source)==count,"QIO extraction simulation");
  check(frequency.getStored(item.toResource())==count,"QIO extraction preview preserves count");
  try(var tx=ChemicalTransfer.open()){adapter.extract(item,42,Actionable.MODULATE,source);}
  check(frequency.getStored(item.toResource())==count,"QIO nested rollback preserves native drive");
  check(adapter.extract(item,Long.MAX_VALUE,Actionable.MODULATE,source)==count,"QIO extraction clamps to actual count");
  check(adapter.insert(item,-1,Actionable.MODULATE,source)==0,"QIO negative insertion rejected");
  check(frequency.getStored(item.toResource())==0,"QIO conserved exact long count");
  var unavailable=new me.ramidzkh.mekae2.qio.QioStorageAdapter<>(dashboard,Direction.NORTH,null);
  check(unavailable.insert(item,1,Actionable.MODULATE,source)==0,"QIO denies wrong-side or absent frequency");
  var secureDashboard=new mekanism.common.tile.qio.TileEntityQIODashboard(dashboardPos,dashboard.getBlockState()) {
   @Override public mekanism.common.content.qio.QIOFrequency getQIOFrequency(){return frequency;}
  };
  secureDashboard.setLevel(level);
  frequency.setValid(true);
  var owner=java.util.UUID.randomUUID();
  secureDashboard.getSecurity().setOwnerUUID(owner);
  secureDashboard.getSecurity().setMode(mekanism.api.security.SecurityMode.PRIVATE);
  var side=dashboard.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING).getOpposite();
  var ownerAccess=new me.ramidzkh.mekae2.qio.QioStorageAdapter<>(secureDashboard,side,owner);
  var strangerAccess=new me.ramidzkh.mekae2.qio.QioStorageAdapter<>(secureDashboard,side,java.util.UUID.randomUUID());
  check(ownerAccess.getFrequency()==frequency,"QIO private dashboard allows owner");
  check(strangerAccess.getFrequency()==null,"QIO private dashboard denies stranger");
  check(new me.ramidzkh.mekae2.qio.QioStorageAdapter<>(secureDashboard,side.getOpposite(),owner).getFrequency()==null,"QIO checks dashboard side");
  frequency.setValid(false);
  check(ownerAccess.getFrequency()==null,"QIO invalid frequency denied");

 }

 static void busChecks(net.minecraft.server.level.ServerLevel level,BlockPos pos,
   net.neoforged.neoforge.transfer.ResourceHandler<mekanism.api.chemical.ChemicalResource> handler,MekanismKey key) {
  var cell=StorageCells.getCellInventory(new ItemStack(AMItems.CHEMICAL_CELL_64K.get()),null);
  var source=IActionSource.empty();
  cell.insert(key,1000,Actionable.MODULATE,source);
  var service=(appeng.api.networking.storage.IStorageService)java.lang.reflect.Proxy.newProxyInstance(PortSmoke.class.getClassLoader(),
   new Class<?>[]{appeng.api.networking.storage.IStorageService.class},(proxy,method,args)-> {
    if(method.getName().equals("getInventory"))return cell;
    throw new UnsupportedOperationException(method.getName());
   });
  var operations=new java.util.concurrent.atomic.AtomicInteger(10);
  appeng.api.networking.energy.IEnergySource energy=(amount,mode,multiplier)->amount;
  var context=(appeng.api.behaviors.StackTransferContext)java.lang.reflect.Proxy.newProxyInstance(PortSmoke.class.getClassLoader(),
   new Class<?>[]{appeng.api.behaviors.StackTransferContext.class},(proxy,method,args)->switch(method.getName()) {
    case "getInternalStorage" -> service;
    case "getActionSource" -> source;
    case "getEnergySource" -> energy;
    case "getOperationsRemaining" -> operations.get();
    case "reduceOperationsRemaining" -> {operations.addAndGet(-((Long)args[0]).intValue());yield null;}
    case "isKeyTypeEnabled", "isInFilter" -> true;
    case "isInverted" -> false;
    default -> throw new UnsupportedOperationException(method.getName());
   });
  var exporter=new me.ramidzkh.mekae2.ae2.stack.MekanismStackExportStrategy(level,pos,Direction.NORTH);
  check(exporter.transfer(context,key,600)==600,"export bus moves real cell to native tank");
  check(handler.getAmountAsLong(0)==600&&cell.extract(key,1000,Actionable.SIMULATE,source)==400,"export bus conserves total");
  var importer=new me.ramidzkh.mekae2.ae2.stack.MekanismStackImportStrategy(level,pos,Direction.NORTH);
  importer.transfer(context);
  check(handler.getAmountAsLong(0)==0&&cell.extract(key,1000,Actionable.SIMULATE,source)==1000,"import bus conserves tank plus cell total");
 }

 static void p2pChecks() throws Exception {
  var split=ChemicalP2PTunnelPart.class.getDeclaredMethod("distribute",int.class,int.class,java.util.function.IntBinaryOperator.class);
  split.setAccessible(true);
  for(int outputs:new int[]{1,2,3,6,17})for(int amount:new int[]{0,1,5,10,1000,Integer.MAX_VALUE}) {
   var delivered=new java.util.concurrent.atomic.AtomicLong();
   java.util.function.IntBinaryOperator receiver=(index,offered)->{check(offered>=0,"P2P nonnegative offer");delivered.addAndGet(offered);return offered;};
   int result=(int)split.invoke(null,amount,outputs,receiver);
   check(result==amount&&delivered.get()==amount,"P2P conserves "+amount+" across "+outputs+" outputs");
  }
  var delivered=new java.util.concurrent.atomic.AtomicInteger();
  java.util.function.IntBinaryOperator receiver=(index,offered)->{int accepted=index<2?0:offered;delivered.addAndGet(accepted);return accepted;};
  check((int)split.invoke(null,10,6,receiver)==10&&delivered.get()==10,"P2P carries full-output remainder without loss");
 }
}

