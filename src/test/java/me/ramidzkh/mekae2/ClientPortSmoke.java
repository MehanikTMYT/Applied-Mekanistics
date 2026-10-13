package me.ramidzkh.mekae2;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import me.ramidzkh.mekae2.integration.jei.AMJeiPlugin;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import appeng.api.stacks.GenericStack;
@EventBusSubscriber(modid="appmek",value=Dist.CLIENT)
public class ClientPortSmoke {
 static boolean done;
 static void check(boolean x,String s){if(!x)throw new AssertionError(s);System.out.println("APPMEK CLIENT PASS: "+s);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
  if(!Boolean.getBoolean("appmek.clientSmoke")||done)return;
  var mc=Minecraft.getInstance();if(mc.level==null||AMJeiPlugin.runtime==null)return;
  done=true;
  try {
   var converter=new me.ramidzkh.mekae2.integration.jei.ChemicalIngredientConverter();
   check(appeng.client.api.integrations.jei.IngredientConverters.getConverter(converter.getIngredientType())!=null,"JEI registers chemical converter in AE2");
   int chemicals=0;
   for(var chem:mekanism.api.MekanismAPI.CHEMICAL_REGISTRY) {
    if(!mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getKey(chem).getNamespace().equals("mekanism_extras"))continue;
    var stack=mekanism.api.chemical.ChemicalResource.of(chem).toStack(250);var generic=converter.getStackFromIngredient(stack);check(generic!=null&&generic.amount()==250,"JEI maps Extras chemical to exact AE pattern amount");
    check(converter.getIngredientFromStack(generic).equals(stack),"Extras chemical roundtrip through JEI converter");
    var renderer=appeng.client.api.AEKeyRendering.getOrThrow(generic.what());check(renderer!=null,"Extras chemical renderer available");chemicals++;
   }
   int fluids=0;var fluidConverter=appeng.client.api.integrations.jei.IngredientConverters.getConverter(mezz.jei.api.neoforge.NeoForgeTypes.FLUID_STACK);check(fluidConverter!=null,"AE2 native fluid converter loaded");
   for(var fluid:net.minecraft.core.registries.BuiltInRegistries.FLUID) {
    var id=net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid);if(!id.getNamespace().equals("mekanism_extras")||id.getPath().startsWith("flowing_"))continue;
    var stack=new net.neoforged.neoforge.fluids.FluidStack(fluid,250);var generic=fluidConverter.getStackFromIngredient(stack);check(generic!=null&&generic.amount()==250,"JEI maps Extras fluid to AE2 pattern: "+id);fluids++;
   }
   check(chemicals==15&&fluids>0,"Extras chemicals and fluids are supported");
   check(mc.getResourceManager().getResource(net.minecraft.resources.Identifier.parse("appmek:items/chemical_storage_cell_1k.json")).isPresent(),"modern item models present");
   System.out.println("APPMEK CLIENT FUNCTIONAL COMPLETE");
  }catch(Throwable t){System.out.println("APPMEK CLIENT FUNCTIONAL FAILED");t.printStackTrace();}finally{mc.stop();}
 }
}
