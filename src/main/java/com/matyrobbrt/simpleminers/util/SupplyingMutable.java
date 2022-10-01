package com.matyrobbrt.simpleminers.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SupplyingMutable<T> implements Supplier<T>, Consumer<Supplier<T>> {
    private Supplier<T> sup;
    private T value;

    @Override
    public T get() {
        if (value == null) value = sup.get();
        return value;
    }

    @Override
    public void accept(Supplier<T> tSupplier) {
        this.sup = tSupplier;
    }
}
