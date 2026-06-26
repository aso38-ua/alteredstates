package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.ibm.icu.impl.ICUCache;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CigarItem extends CigaretteItem implements ICurable {

    private final CigarType type;

    public CigarItem(Properties properties, CigarType type) {
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1).component(ModDataComponentTypes.CIGAR_TYPE.get(), type));
        this.type = type;
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        // Pendiente: efectos de nicotina/relajación según mecánicas del mod
        if (entity.level().isClientSide) return;

        // Obtenemos los datos de forma limpia a través de la interfaz
        int quality = getQuality(stack);

        if (entity instanceof Player player) {
            player.hurt(player.damageSources().starve(), 2.0F); // 1.0F = medio corazón
        }

        int duration = 500 * quality;
        int amplifier = 2;
        if(quality == 4){ amplifier = 3; }

        switch (type){
            case TORITO -> entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier));
            case LANCERO -> entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier));
            case ESPLENDIDO -> {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.GLOWING, duration, amplifier));
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WATER_BREATHING, duration, amplifier));
            }
            case DON_JAVIER -> entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.REGENERATION, duration, amplifier));
        }
    }
}
