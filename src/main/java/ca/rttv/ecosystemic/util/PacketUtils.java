package ca.rttv.ecosystemic.util;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class PacketUtils {
    /**
     * A method in which a packet will be sent to every {@link PlayerEntity} which has the specified {@link BlockPos} loaded
     * @param world the world in which the players exist
     * @param pos the position to filter the players that have it loaded
     * @param packet the packet to send to the clients
     */
    public static void sendPacketToPlayers(ServerWorld world, BlockPos pos, Packet<ClientPlayPacketListener> packet) {
        world.getPlayers(player -> player.getBlockPos().isWithinDistance(pos, 128)).forEach(player -> player.networkHandler.sendPacket(packet));
    }

    /**
     * A method in which a packet will be sent to every {@link PlayerEntity} which has the specified {@link BlockPos} loaded
     * @param world the world in which the players exist
     * @param pos the position to filter the players that have it loaded
     * @param channel the quilt networking string for the packet channel id
     * @param packet the packet to send
     */
    public static void sendPacketToPlayers(ServerWorld world, BlockPos pos, Identifier channel, Packet<ClientPlayPacketListener> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(world.getPlayers(player -> player.getBlockPos().isWithinDistance(pos, 128)), channel, buf);
    }
}
