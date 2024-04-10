package com.GunterPro7.utils;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TimeUtils {
    public static final Map<Long, List<Runnable>> TIME_JOB_MAP = new ConcurrentHashMap<>();

    public static void addJob(int duration, Runnable runnable) {
        addJob(System.currentTimeMillis() + duration, runnable);
    }

    public static void addJob(long time, Runnable runnable) {
        List<Runnable> runnables = TIME_JOB_MAP.containsKey(time) ? TIME_JOB_MAP.get(time) : new ArrayList<>();
        runnables.add(runnable);

        TIME_JOB_MAP.put(time, runnables);
    }

    private static void checkJobs() {
        List<Long> toDelete = new ArrayList<>();
        for (long time : TIME_JOB_MAP.keySet()) {
            if (System.currentTimeMillis() >= time) {
                List<Runnable> runnables = TIME_JOB_MAP.get(time);
                runnables.forEach(Runnable::run);
                toDelete.add(time);
            }
        }

        toDelete.forEach(TIME_JOB_MAP::remove);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        checkJobs();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        checkJobs();
    }
}
