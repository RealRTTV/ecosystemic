package ca.rttv.ecosystemic.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.function.Consumer;

public class PacketUtils {
    /**
     * A method in which a packet will be sent to every {@link PlayerEntity} which has the specified {@link BlockPos} loaded
     * @param world the world in which the players exist
     * @param pos the position to filter the players that have it loaded
     * @return a consumer to generate the packet which every player will receive
     */
    public static Consumer<Packet<?>> sendPacketToPlayers(ServerWorld world, BlockPos pos) {
        return packet -> world.getPlayers(player -> player.getWorld().isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()))).forEach(player -> player.networkHandler.sendPacket(packet));
    }

    /**
     * A method in which a packet will be sent to every {@link PlayerEntity} which has the specified {@link BlockPos} loaded
     * @param world the world in which the players exist
     * @param pos the position to filter the players that have it loaded
     * @param channel the quilt networking string for the packet channel id
     * @return a consumer to generate the packet which every player will receive
     */
    public static Consumer<PacketByteBuf> sendPacketToPlayers(ServerWorld world, BlockPos pos, Identifier channel) {
        return packet -> ServerPlayNetworking.send(world.getPlayers(player -> player.getWorld().isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()))), channel, packet);
    }
}
