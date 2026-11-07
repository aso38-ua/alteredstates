package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;

/** Gasolina cruda: fluido real (cubo, bloque, tuberías). Usa las texturas del agua teñidas de marrón oscuro. */
public class ModGasoline {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, AlteredStates.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, AlteredStates.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AlteredStates.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AlteredStates.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> GASOLINE_TYPE = FLUID_TYPES.register("gasoline",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.alteredstates.gasoline")
                    .density(800)
                    .viscosity(1500)
                    .temperature(300)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override public ResourceLocation getStillTexture() { return ResourceLocation.withDefaultNamespace("block/water_still"); }
                        @Override public ResourceLocation getFlowingTexture() { return ResourceLocation.withDefaultNamespace("block/water_flow"); }
                        @Override public ResourceLocation getOverlayTexture() { return ResourceLocation.withDefaultNamespace("block/water_overlay"); }
                        @Override public int getTintColor() { return 0xFF2B2012; }
                    });
                }
            });

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> SOURCE =
            FLUIDS.register("gasoline", () -> new BaseFlowingFluid.Source(props()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING =
            FLUIDS.register("flowing_gasoline", () -> new BaseFlowingFluid.Flowing(props()));

    public static final DeferredBlock<LiquidBlock> GASOLINE_BLOCK = BLOCKS.register("gasoline",
            () -> new LiquidBlock(SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                    .mapColor(MapColor.COLOR_BLACK)
                    .noLootTable()));

    public static final DeferredItem<Item> GASOLINE_BUCKET = ITEMS.register("gasoline_bucket",
            () -> new BucketItem(SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(GASOLINE_TYPE, SOURCE, FLOWING)
                .block(GASOLINE_BLOCK)
                .bucket(GASOLINE_BUCKET)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30)
                .explosionResistance(100.0F);
    }

    public static void register(IEventBus bus) {
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
