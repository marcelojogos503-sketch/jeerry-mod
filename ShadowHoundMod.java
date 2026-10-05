package com.shadowhound;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ShadowHoundMod.MODID)
public class ShadowHoundMod {

    public static final String MODID = "shadowhound";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ShadowHoundMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITIES.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onEntityAttributes);
        modBus.addListener(this::onSpawnPlacements);

        LOGGER.info("Shadow Hound carregado!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // setup comum
    }

    private void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.HOUND.get(), HoundEntity.createHoundAttributes().build());
    }

    private void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.HOUND.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HoundEntity::checkAnimalSpawnRules
        );
    }
}
