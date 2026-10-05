package com.shadowhound;

import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowHoundMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.HOUND.get(), HoundRenderer::new);
    }

    public static class HoundRenderer extends MobRenderer<HoundEntity, WolfModel<HoundEntity>> {
        // Texturas proprias do mod (assets/shadowhound/textures/entity/).
        private static final ResourceLocation CALM = new ResourceLocation(ShadowHoundMod.MODID, "textures/entity/hound.png");
        private static final ResourceLocation ANGRY = new ResourceLocation(ShadowHoundMod.MODID, "textures/entity/hound_angry.png");

        public HoundRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new WolfModel<>(ctx.bakeLayer(ModelLayers.WOLF)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(HoundEntity e) {
            return e.getStage() >= 2 ? ANGRY : CALM;
        }

        @Override
        protected float getBob(HoundEntity e, float partialTicks) {
            return e.getTailAngle();
        }
    }
}
