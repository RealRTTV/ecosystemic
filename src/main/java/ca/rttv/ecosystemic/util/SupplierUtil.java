package ca.rttv.ecosystemic.util;

import java.util.function.IntSupplier;

public class SupplierUtil {
    public static IntSupplier memoize(IntSupplier inner) {
        return new IntSupplier() {
            boolean memoized;
            int value;

            @Override
            public int getAsInt() {
                if (memoized) {
                    return value;
                }
                memoized = true;
                value = inner.getAsInt();
                return value;
            }
        };
    }
}
