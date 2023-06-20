package ca.rttv.ecosystemic.network.packet.s2c.play;

import ca.rttv.ecosystemic.Nibble;
import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

import java.util.UUID;

public record ResetConsumingTimerS2CPacket(UUID uuid, int ticks, Nibble type) implements Packet<ClientPlayPacketListener> {
    public ResetConsumingTimerS2CPacket(PacketByteBuf buf) { this(buf.readUuid(), buf.readVarInt(), buf.readEnumConstant(Nibble.class)); }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeVarInt(ticks);
        buf.writeEnumConstant(type);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        ((EcosystemicClientPlayPacketListener) listener).ecosystemic$onResetConsumingTimer(this);
    }
}
