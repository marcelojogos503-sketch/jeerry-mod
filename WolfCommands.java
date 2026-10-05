package com.shadowhound;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lobos domesticados entendem o que o jogador "fala" no chat (ou por voz, se um mod
 * de voz transformar a fala em texto no chat). Ex.: "Jerry, senta", "Jerry, roll over",
 * "Jerry, busca", "vem", "pula", "gira", "bom garoto".
 * Se o texto tiver o nome do lobo, so ele obedece; sem nome, todos os seus lobos obedecem.
 * O lobo inclina a cabeca enquanto "escuta".
 */
@Mod.EventBusSubscriber(modid = ShadowHoundMod.MODID)
public class WolfCommands {

    private static final String K_LISTEN = "sh_listen";
    private static final String K_ROLL = "sh_roll";
    private static final String K_JUMP = "sh_jump";
    private static final String K_FETCH = "sh_fetch";
    private static final String K_FETCH_T = "sh_fetch_t";
    private static final String K_CARRY = "sh_carry";

    private static final String[] LISTEN = {"escuta", "ouve", "presta atencao", "listen"};
    private static final String[] SIT = {"senta", "sit", "fica", "stay", "quieto", "fique"};
    private static final String[] COME = {"vem", "venha", "come", "levanta", "segue", "follow", "stand"};
    private static final String[] ROLL = {"rola", "roll", "rolar"};
    private static final String[] JUMP = {"pula", "jump"};
    private static final String[] SPIN = {"gira", "spin", "volta"};
    private static final String[] FETCH = {"busca", "pega", "traz", "fetch", "bring"};
    private static final String[] PRAISE = {"bom garoto", "bom menino", "bom cachorro", "bom cao", "good boy", "good dog", "parabens"};

    private static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).trim();
    }

    private static boolean has(String text, String[] words) {
        for (String w : words) {
            if (text.contains(w)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String text = normalize(event.getMessage().getString());
        MinecraftServer server = player.getServer();
        if (server == null) return;
        server.execute(() -> handle(player, text));
    }

    private static void handle(ServerPlayer player, String text) {
        Level level = player.level();

        // O "cachorro sombrio" nao obedece: rosna quando voce fala por perto
        for (HoundEntity h : level.getEntitiesOfClass(HoundEntity.class, player.getBoundingBox().inflate(16.0D))) {
            if (h.getStage() >= 2) {
                h.playSound(net.minecraft.sounds.SoundEvents.WOLF_GROWL, 1.0F, 0.6F);
            }
        }

        List<Wolf> owned = level.getEntitiesOfClass(Wolf.class, player.getBoundingBox().inflate(32.0D),
                w -> !(w instanceof HoundEntity) && w.isTame() && w.isOwnedBy(player));
        if (owned.isEmpty()) return;

        // quem foi chamado pelo nome?
        List<Wolf> targets = new ArrayList<>();
        boolean named = false;
        for (Wolf w : owned) {
            if (w.hasCustomName()) {
                String n = normalize(w.getCustomName().getString());
                if (!n.isEmpty() && text.contains(n)) {
                    targets.add(w);
                    named = true;
                }
            }
        }
        if (targets.isEmpty()) targets = owned;

        boolean listen = has(text, LISTEN);
        boolean sit = has(text, SIT);
        boolean come = has(text, COME);
        boolean roll = has(text, ROLL);
        boolean jump = has(text, JUMP);
        boolean spin = has(text, SPIN);
        boolean fetch = has(text, FETCH);
        boolean praise = has(text, PRAISE);
        boolean any = listen || sit || come || roll || jump || spin || fetch || praise;
        if (!any && !named) return;

        for (Wolf w : targets) {
            CompoundTag d = w.getPersistentData();
            d.putInt(K_LISTEN, 60); // inclina a cabeca escutando por 3 segundos

            if (sit) {
                w.setOrderedToSit(true);
                w.setInSittingPose(true);
                w.getNavigation().stop();
            } else if (come) {
                w.setOrderedToSit(false);
                w.setInSittingPose(false);
                w.getNavigation().moveTo(player, 1.3D);
            }
            if (roll) {
                w.setOrderedToSit(false);
                w.setInSittingPose(false);
                d.putInt(K_ROLL, 16);
                w.setDeltaMovement(w.getDeltaMovement().x, 0.42D, w.getDeltaMovement().z);
            }
            if (spin) {
                w.setOrderedToSit(false);
                w.setInSittingPose(false);
                d.putInt(K_ROLL, 16);
            }
            if (jump) {
                w.setOrderedToSit(false);
                w.setInSittingPose(false);
                d.putInt(K_JUMP, 3);
            }
            if (fetch) {
                w.setOrderedToSit(false);
                w.setInSittingPose(false);
                ItemEntity best = null;
                double bd = 1.0E9D;
                for (ItemEntity it : level.getEntitiesOfClass(ItemEntity.class, w.getBoundingBox().inflate(16.0D))) {
                    double dd = it.distanceToSqr(w);
                    if (dd < bd) {
                        bd = dd;
                        best = it;
                    }
                }
                if (best != null) {
                    d.putInt(K_FETCH, best.getId());
                    d.putInt(K_FETCH_T, 400);
                    d.putInt(K_CARRY, 0);
                }
            }
            if (praise && level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 0.8D, w.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.02D);
            }
        }
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Wolf)) return;
        Wolf wolf = (Wolf) event.getEntity();
        if (wolf instanceof HoundEntity || wolf.level().isClientSide) return;
        CompoundTag d = wolf.getPersistentData();

        // escutando: inclina a cabeca
        int listen = d.getInt(K_LISTEN);
        if (listen > 0) {
            d.putInt(K_LISTEN, listen - 1);
            wolf.setIsInterested(true);
            if (listen == 1) wolf.setIsInterested(false);
        }

        // girar / rolar
        int roll = d.getInt(K_ROLL);
        if (roll > 0) {
            d.putInt(K_ROLL, roll - 1);
            float yaw = wolf.getYRot() + 22.5F;
            wolf.setYRot(yaw);
            wolf.setYBodyRot(yaw);
            wolf.setYHeadRot(yaw);
        }

        // pular
        int jump = d.getInt(K_JUMP);
        if (jump > 0 && wolf.onGround()) {
            d.putInt(K_JUMP, jump - 1);
            wolf.setDeltaMovement(wolf.getDeltaMovement().x, 0.45D, wolf.getDeltaMovement().z);
        }

        // buscar item e trazer ate o dono
        int fid = d.getInt(K_FETCH);
        if (fid != 0) {
            int t = d.getInt(K_FETCH_T) - 1;
            d.putInt(K_FETCH_T, t);
            Entity e = wolf.level().getEntity(fid);
            ItemEntity item = (e instanceof ItemEntity) ? (ItemEntity) e : null;
            LivingEntity owner = wolf.getOwner();
            if (t <= 0 || item == null || !item.isAlive() || owner == null) {
                d.putInt(K_FETCH, 0);
                d.putInt(K_CARRY, 0);
            } else if (d.getInt(K_CARRY) == 0) {
                wolf.getNavigation().moveTo(item, 1.3D);
                if (wolf.distanceTo(item) < 2.0F) d.putInt(K_CARRY, 1);
            } else {
                item.setPos(wolf.getX(), wolf.getY() + 0.5D, wolf.getZ());
                item.setDeltaMovement(0.0D, 0.0D, 0.0D);
                wolf.getNavigation().moveTo(owner, 1.3D);
                if (wolf.distanceTo(owner) < 2.5F) {
                    item.setPos(owner.getX(), owner.getY() + 0.3D, owner.getZ());
                    d.putInt(K_FETCH, 0);
                    d.putInt(K_CARRY, 0);
                }
            }
        }
    }
}
