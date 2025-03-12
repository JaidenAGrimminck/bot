package me.autobot.lib.tools.suppliers;

import java.util.function.Supplier;

public interface StringSupplier extends Supplier<String> {
    /**
     * Gets a string.
     * @return The string.
     * */
    String get();
}
