package me.autobot.lib.tools.suppliers;

import java.util.function.Supplier;

public interface FloatSupplier extends Supplier<Float> {
    @Override
    Float get();
}
