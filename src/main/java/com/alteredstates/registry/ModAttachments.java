package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.data.attachment.PlayerSubstanceData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AlteredStates.MOD_ID);

    /**
     * Attachment principal del jugador.
     * Guarda: sustancia activa, fase, ticks, tolerancias, últimas tomas.
     * Se serializa con Codec → persiste entre sesiones y dimensiones.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerSubstanceData>> SUBSTANCE_DATA =
            ATTACHMENT_TYPES.register("substance_data", () ->
                    AttachmentType.builder(PlayerSubstanceData::new)
                            .serialize(PlayerSubstanceData.CODEC)
                            .build()
            );
}