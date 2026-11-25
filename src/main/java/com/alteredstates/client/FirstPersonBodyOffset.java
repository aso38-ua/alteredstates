package com.alteredstates.client;

import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

public class FirstPersonBodyOffset extends AbstractModifier {
    private final float offset;

    public FirstPersonBodyOffset(float offset) {
        this.offset = offset;
    }

    @Override
    public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
        PlayerAnimBone result = super.get3DTransform(bone);   // primero la animación, luego tu ajuste
        if (FirstPersonMode.isFirstPersonPass() && "body".equals(result.getName())) {
            result.setPosY(result.getPosY() + offset);
        }
        return result;
    }
}