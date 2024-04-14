package com.GunterPro7.utils;

import java.util.function.Consumer;

public record TimeTask(long id, Consumer<Long> consumer) {

    public void run() {
        consumer.accept(id);
    }
}
