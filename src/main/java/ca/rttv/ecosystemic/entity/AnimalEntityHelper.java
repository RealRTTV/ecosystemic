package ca.rttv.ecosystemic.entity;

import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.util.Map;
import java.util.stream.IntStream;

public class AnimalEntityHelper {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void writeVisitedSpaces(NbtCompound nbt, Map<BlockPos, Long> visitedSpaces, World world, long ticksMoved) {
        nbt.putLong("TicksMoved", ticksMoved);
        nbt.putIntArray("VisitedSpaces", visitedSpaces.entrySet()
                                                      .stream()
                                                      .flatMapToInt(entry -> IntStream.of(
                                                                    (int) ((entry.getKey().asLong() & 0xFFFFFFFF00000000L) >> 32),
                                                                    (int) (entry.getKey().asLong() & 0x00000000FFFFFFFFL),
                                                                    (int) (ticksMoved - entry.getValue()))
                                                      )
                                                      .toArray());
    }

    public static void readVisitedSpaces(NbtCompound nbt, Map<BlockPos, Long> visitedSpaces, long ticksMoved) {
        visitedSpaces.clear();
        // normally an nbt list cannot hold multiple types so split the BlockPos into 2 ints with 1 remaining for the time visited since 3 ints is better than 2 longs (96 bits vs 128 bits)
        int[] list = nbt.getIntArray("VisitedSpaces");
        if (list.length % 3 != 0) {
            LOGGER.error("VisitedSpaces length must be divisible by 3 to work properly, ignoring remaining " + list.length % 2 + " ints");
        }
        for (int i = 0; i < list.length / 3 * 3; ) {
            visitedSpaces.put(BlockPos.fromLong((long) list[i++] << 32 | list[i++]), list[i++] + ticksMoved);
        }
        // gross, a stream into a for loop
        // if you can make it into just a stream, I'd gladly accept it
    }

    public static void cacheVisitedSpace(World world, Map<BlockPos, Long> visitedSpaces, BlockPos pos, long ticksMoved) {
        if (!world.isClient) {
            visitedSpaces.put(pos, ticksMoved); // this is why it is a map
            visitedSpaces.entrySet().removeIf(entry -> entry.getValue() <= ticksMoved - 100);
        }
    }

    public static void addShelterGoals(GoalSelector goalSelector, PathAwareEntity mob) {
        goalSelector.add(-1, new EscapeRainGoal(mob)); // this works somehow
        goalSelector.add(-1, new AvoidRainGoal(mob)); // this works somehow
    }
}
