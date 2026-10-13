package me.ramidzkh.mekae2.ae2;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;


import appeng.client.api.AEKeyRenderer;
import appeng.client.gui.style.FluidBlitter;
import appeng.util.Platform;

public class AMChemicalStackRenderer implements AEKeyRenderer<MekanismKey, AMChemicalStackRenderer.RenderState> {
    public static void initialize(net.neoforged.bus.api.IEventBus bus) {
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event)->event.enqueueWork(()->appeng.client.api.AEKeyRendering.register(MekanismKeyType.TYPE,MekanismKey.class,new AMChemicalStackRenderer())));
    }
    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphicsExtractor graphics, int x,int y,MekanismKey what) {
        var state=createState();extract(state,what,null,0);
        appeng.client.gui.style.Blitter.sprite(state.sprite).colorRgb(state.color).blending(false).dest(x,y,16,16).blit(graphics);
    }

    @Override
    public Class<RenderState> stateClass() {
        return RenderState.class;
    }

    @Override
    public RenderState createState() {
        return new RenderState();
    }

    @Override
    public void extract(RenderState state, MekanismKey what, @Nullable Level level, int seed) {
        state.color=what.getStack().getChemical().getTint();
        state.sprite=Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getSprite(what.getStack().getChemical().getIcon());
    }

    @Override
    public void submit(PoseStack poseStack, RenderState state, SubmitNodeCollector nodes, int lightCoords) {

        poseStack.pushPose();
        // Push it out of the block face a bit to avoid z-fighting
        poseStack.translate(0, 0, 0.01f);

        // y is flipped here
        var x0 = -1 / 2f;
        var y0 = 1 / 2f;
        var x1 = 1 / 2f;
        var y1 = -1 / 2f;

        var color = ARGB.opaque(state.color);
        var sprite = state.sprite;

        // We have to use "solid" here because we don't actually want to alpha blend
        // the semi-transparent fluid textures onto a fake screen.
        nodes.submitCustomGeometry(poseStack, RenderTypes.entitySolid(state.sprite.atlasLocation()), (pose, buffer) -> {
            buffer.addVertex(pose, x0, y1, 0)
                    .setColor(color)
                    .setUv(sprite.getU0(), sprite.getV1())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightCoords)
                    .setNormal(0, 0, 1);
            buffer.addVertex(pose, x1, y1, 0)
                    .setColor(color)
                    .setUv(sprite.getU1(), sprite.getV1())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightCoords)
                    .setNormal(0, 0, 1);
            buffer.addVertex(pose, x1, y0, 0)
                    .setColor(color)
                    .setUv(sprite.getU1(), sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightCoords)
                    .setNormal(0, 0, 1);
            buffer.addVertex(pose, x0, y0, 0)
                    .setColor(color)
                    .setUv(sprite.getU0(), sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightCoords)
                    .setNormal(0, 0, 1);
        });

        poseStack.popPose();
    }

    @Override
    public List<Component> getTooltip(MekanismKey stack) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(stack.getDisplayName());

        // Heuristic: If the last line doesn't include the modname, add it ourselves
        var modName = Platform.formatModName(stack.getModId());
        if (!tooltip.getLast().getString().equals(modName)) {
            tooltip.add(Component.literal(modName));
        }

        return tooltip;
    }

    public static final class RenderState {
        public int color;
        public TextureAtlasSprite sprite;
    }
}
