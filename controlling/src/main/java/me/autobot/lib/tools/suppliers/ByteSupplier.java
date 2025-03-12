package me.autobot.lib.tools.suppliers;

import java.util.function.Supplier;

public interface ByteSupplier extends Supplier<Byte[]> {
    @Override
    Byte[] get();
}
