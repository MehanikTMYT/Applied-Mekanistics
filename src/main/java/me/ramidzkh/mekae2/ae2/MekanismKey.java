package me.ramidzkh.mekae2.ae2;

import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.radiation.IRadiationManager;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.core.AELog;

public class MekanismKey extends AEKey {

    public static final MapCodec<MekanismKey> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.holderByNameCodec().fieldOf("id").forGetter(key -> key.getStack().typeHolder()))
            .apply(instance, chemical -> MekanismKey.of(new ChemicalStack(chemical, 1))));
    public static final Codec<MekanismKey> CODEC = MAP_CODEC.codec();

    private final ChemicalStack stack;

    private MekanismKey(ChemicalStack stack) {
        this.stack = stack;
    }

    @Nullable
    public static MekanismKey of(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return new MekanismKey(stack.copyWithAmount(1));
    }

    public ChemicalStack getStack() {
        return stack.copy();
    }

    public ChemicalStack withAmount(long amount) {
        return stack.copyWithAmount((int)Math.min(Integer.MAX_VALUE, Math.max(0,amount)));
    }

    @Override
    public AEKeyType getType() {
        return MekanismKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Nullable
    public static MekanismKey fromTag(net.minecraft.world.level.storage.ValueInput input) {
        return input.read(MAP_CODEC).orElse(null);
    }
    @Override
    public void toTag(net.minecraft.world.level.storage.ValueOutput output) {
        output.store(MAP_CODEC, this);
    }

    @Override
    public Object getPrimaryKey() {
        return stack.getChemical();
    }

    @Override
    public Identifier getId() {
        return stack.typeHolder().getKey().identifier();
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        IRadiationManager.INSTANCE.dumpRadiation(level, pos, mekanism.api.chemical.ChemicalResource.of(stack), amount);
    }

    @Override
    protected Component computeDisplayName() {
        return stack.getChemical().getTextComponent();
    }

    @Override
    public boolean isTagged(TagKey<?> tag) {
        // This will just return false for incorrectly cast tags
        return stack.is((TagKey<Chemical>) tag);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<T> type) {
        return null;
    }

    @Override
    public boolean hasComponents() {
        return false;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        ChemicalStack.STREAM_CODEC.encode(data, stack);
    }

    public static MekanismKey fromPacket(RegistryFriendlyByteBuf data) {
        return java.util.Objects.requireNonNull(of(ChemicalStack.STREAM_CODEC.decode(data)), "Empty chemical key");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var that = (MekanismKey) o;
        return Objects.equals(stack.getChemical(), that.stack.getChemical());
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack.getChemical());
    }

    @Override
    public String toString() {
        return "MekanismKey{" +
                "stack=" + stack.getChemical() +
                '}';
    }
}
