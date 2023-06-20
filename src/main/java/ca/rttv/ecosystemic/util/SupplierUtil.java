package ca.rttv.ecosystemic.util;

import ca.rttv.ecosystemic.duck.WaterDesireDuck;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.fluid.Fluids;

import java.util.function.IntSupplier;
import java.util.stream.Stream;

public class SupplierUtil {
    public static IntSupplier memoize(IntSupplier inner) {
        return new MemoizedIntSupplier(inner);
    }

    public static IntSupplier drinkableWaterBlocks(WaterDesireDuck duck, AnimalEntity entity) {
        return SupplierUtil.memoize(() ->
                (int) duck.ecosystemic$pen()
                        .stream()
                        .flatMap(space -> Stream.of(
                                space.add(1, -1, 0),
                                space.add(-1, -1, 0),
                                space.add(0, -1, 1),
                                space.add(0, -1, -1)
                        ))
                        .distinct()
                        .filter(pos -> entity.getWorld().getFluidState(pos).isOf(Fluids.WATER) && entity.getWorld().getFluidState(pos.up()).isEmpty() && entity.getWorld().getBlockState(pos.up()).getCollisionShape(entity.getWorld(), pos).isEmpty())
                        .count()
        );
    }

    static class MemoizedIntSupplier implements IntSupplier {
        MemoizedIntSupplier(IntSupplier inner) {
            this.inner = inner;
        }

        boolean memoized;
        int value;
        IntSupplier inner;
        @Override
        public int getAsInt() {
            if (memoized) return value;
            memoized = true;
            return value = inner.getAsInt();
        }
    }
}
