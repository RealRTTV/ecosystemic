package ca.rttv.ecosystemic.entity;

import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.stream.Stream;

public class AnimalEntityHelper {
    public static void writeVisitedSpaces(NbtCompound nbt, Map<BlockPos, Long> visitedSpaces) {
        NbtList list = new NbtList();
        visitedSpaces.entrySet()
                     .stream()
                     .flatMap(entry -> Stream.of(NbtLong.of(entry.getKey().asLong()), NbtLong.of(entry.getValue())))
                     .forEach(list::add);
        nbt.put("VisitedSpaces", list);
    }

    public static void readVisitedSpaces(NbtCompound nbt, Map<BlockPos, Long> visitedSpaces) {
        visitedSpaces.clear();
        long[] list = nbt.getList("VisitedSpaces", 4)
                         .stream()
                         .mapToLong(element -> ((NbtLong) element).longValue())
                         .toArray();
        for (int i = 0; i < list.length; ) {
            visitedSpaces.put(BlockPos.fromLong(list[i++]), list[i++]);
        }
        // gross, a stream into a for loop
        // if you can make it into just a stream, we'll
    }

    public static void cacheVisitedSpace(World world, Map<BlockPos, Long> visitedSpaces, BlockPos pos) {
        if (!world.isClient) {
            visitedSpaces.put(pos, world.getTime()); // this is why it is a map
            // if these lists work well, and I understand then, I can cut runtime by stopping once I hit one that is too recent to be discarded
            visitedSpaces.entrySet().removeIf(entry -> entry.getValue() <= world.getTime());
        }
    }

    public static void addShelterGoals(GoalSelector goalSelector, PathAwareEntity mob) {
        goalSelector.add(-1, new EscapeRainGoal(mob)); // this works somehow
        goalSelector.add(-1, new AvoidRainGoal(mob)); // this works somehow
    }
}
