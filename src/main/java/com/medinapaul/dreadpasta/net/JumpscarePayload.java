package com.medinapaul.dreadpasta.net;

import com.medinapaul.dreadpasta.DreadpastaMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record JumpscarePayload(Integer ticks) implements CustomPayload {
    public static final CustomPayload.Id<JumpscarePayload> ID = new CustomPayload.Id<>(DreadpastaMod.id("jumpscare"));
    public static final PacketCodec<PacketByteBuf, JumpscarePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            JumpscarePayload::ticks,
            JumpscarePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
