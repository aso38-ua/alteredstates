package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.ibm.icu.impl.ICUCache;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CigarItem extends CigaretteItem implements ICurable {

    private final CigarType type;

    public CigarItem(Item.Properties properties, CigarType type) {
        this(properties, type, SmokeVisuals.CIGARETTE);
    }

    public CigarItem(Item.Properties properties, CigarType type, SmokeVisuals visuals) {
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1)
                .component(ModDataComponentTypes.CIGAR_TYPE.get(), type), visuals);
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

    @Override
    protected void applyHeadEffects(LivingEntity entity, ItemStack stack) {
        if (!entity.level().isClientSide) {
            int quality = getQuality(stack);

            int duration = 40;
            int amplifier = 0;
            if(quality == 4){ amplifier = 1; }

            switch (type){
                case TORITO -> entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier));
                case LANCERO -> entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier));
                case ESPLENDIDO -> {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WATER_BREATHING, duration, amplifier));
                }
                case DON_JAVIER -> {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.LUCK, duration, amplifier));
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.GLOWING, duration, amplifier));
                }
            }
        }
    }
}
