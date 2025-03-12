package me.autobot.lib.tools.suppliers;

import java.util.function.Supplier;

public interface ShortSupplier extends Supplier<Short> {
    @Override
    Short get();
}
