package com.GunterPro7.utils;

import com.GunterPro7.Main;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TimeUtils {
    public static final Map<Long, List<TimeTask>> TIME_JOB_MAP = new ConcurrentHashMap<>();

    public static void addJob(long id, int duration, Consumer<Long> runnable) {
        addJob(id, System.currentTimeMillis() + duration, runnable);
    }

    public static void addJob(long id, long time, Consumer<Long> runnable) {
        List<TimeTask> runnables = TIME_JOB_MAP.containsKey(time) ? TIME_JOB_MAP.get(time) : new ArrayList<>();
        runnables.add(new TimeTask(id, runnable));
        TIME_JOB_MAP.put(time, runnables);
    }

    private static void checkJobs() {
        List<Long> toDelete = new ArrayList<>();
        for (long time : TIME_JOB_MAP.keySet()) {
            if (System.currentTimeMillis() >= time) {
                List<TimeTask> runnables = TIME_JOB_MAP.get(time);
                if (runnables != null) {
                    runnables.forEach(TimeTask::run);
                    toDelete.add(time);
                }
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
        if (Main.minecraftServer != null && !Minecraft.getInstance().hasSingleplayerServer()) {
            checkJobs();
        }
    }
}
