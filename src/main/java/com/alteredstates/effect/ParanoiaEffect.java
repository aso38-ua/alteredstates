package com.alteredstates.effect;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.RandomSource;

public class ParanoiaEffect extends MobEffect {

    public ParanoiaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    // Le dice a Minecraft que este efecto quiere ejecutar código en cada tick del juego
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            RandomSource random = player.getRandom();

            // 🔊 Cada 10 segundos aprox (200 ticks), hay un 15% de probabilidad de escuchar un fantasma
            if (player.tickCount % 40 == 0 && random.nextFloat() < 0.30f) {

                // Elegimos un sonido aleatorio de "peligro" para asustar al jugador
                net.minecraft.sounds.SoundEvent fakeSound = switch (random.nextInt(6)) {
                    case 0 -> SoundEvents.CREEPER_PRIMED; // El "Sssss" de un creeper
                    case 1 -> SoundEvents.ZOMBIE_AMBIENT; // Un gemido al lado
                    case 2 -> SoundEvents.ARROW_HIT;      // El sonido de que te han clavado una flecha
                    case 3 -> SoundEvents.ZOMBIE_HURT;
                    case 4 -> SoundEvents.SKELETON_AMBIENT;
                    default -> SoundEvents.CAVE_VINES_STEP;  // El sonido tétrico de las cuevas
                };

                // Spawneamos el sonido justo detrás de la espalda del jugador
                double angle = Math.toRadians(player.getYRot() + 180);
                double x = player.getX() + Math.sin(angle) * 2;
                double z = player.getZ() - Math.cos(angle) * 2;

                // Como estamos en el servidor, enviamos el sonido SOLO a la conexión de ese jugador
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(fakeSound),
                            SoundSource.AMBIENT,
                            x, player.getY(), z,
                            0.6F, // Volumen
                            0.9F + random.nextFloat() * 0.2F, // Pitch
                            player.getRandom().nextLong() // Semilla de aleatoriedad
                    ));
                }
            }
        }
        return true;
    }
}