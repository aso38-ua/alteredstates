package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModEffects;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GroundMysticMushroomItem extends Item implements IRollable, IBongable {

    public GroundMysticMushroomItem(Properties properties) {
        super(properties);
    }

    // ─── UTILIDADES ────────────────────────────────────────────────────────

    public int getQuality(ItemStack stack) {
        // Lee la calidad, por defecto 1 si no tiene
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
    }

    // ─── INTERFAZ IROLLABLE ────────────────────────────────────────────────

    @Override
    public Item getRollResult(ItemStack stack) {
        // La bandeja devuelve siempre nuestro porro genérico
        return ModItems.ROLLED_JOINT.get();
    }

    @Override
    public String getContentType(ItemStack stack) {
        // ¡El chivato clave! Esto hace que el porro sepa que tiene setas dentro
        return "mystic_mushroom";
    }

    // ─── INTERFAZ IBONGABLE (Si te lo fumas en bong o pipa) ────────────────

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        if (!(entity instanceof Player player)) return;

        int quality = getQuality(stack);
        int amplifier = quality >= 3 ? 1 : 0;
        int duration = 200 * quality; // Viaje psicodélico de 10s por nivel

        // 🍄 Efecto principal de la seta mística
        player.addEffect(new MobEffectInstance(ModEffects.PSYCHODELIA, duration, amplifier));

        // 🤢 Las setas a veces caen mal al estómago (10% de probabilidad de náuseas leves)
        if (player.getRandom().nextFloat() < 0.10f) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }
    }
}