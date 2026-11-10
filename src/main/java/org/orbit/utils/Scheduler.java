package org.orbit.utils;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class for scheduling tasks.
 * Allows for synchronous, asynchronous, and cyclic task scheduling.
 */
public class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private static final Scheduler INSTANCE = new Scheduler();
    private final ConcurrentHashMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private int currentTick = 0;
    private int taskIdCounter = 0;

    public static Scheduler getInstance() {
        return INSTANCE;
    }

    public int schedule(Runnable task, int delay) {
        return addTask(task, delay, 0, false, false);
    }

    public int scheduleAsync(Runnable task, int delay) {
        return addTask(task, delay, 0, false, true);
    }

    public int scheduleCyclic(Runnable task, int delay, int intervalTicks) {
        return addTask(task, delay, intervalTicks, true, false);
    }

    public int scheduleCyclicAsync(Runnable task, int delay, int intervalTicks) {
        return addTask(task, delay, intervalTicks, true, true);
    }

    private int addTask(Runnable task, int delay, int intervalTicks, boolean cyclic, boolean async) {
        int taskId = taskIdCounter++;
        ScheduledTask scheduledTask = new ScheduledTask(task, intervalTicks, cyclic, async, currentTick + delay);
        tasks.put(taskId, scheduledTask);
        return taskId;
    }

    public void resetTask(int taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task != null) {
            tasks.put(taskId, task.next(currentTick));
        }
    }

    public void tick(MinecraftClient ignoredClient) {
        currentTick++;

        Iterator<Map.Entry<Integer, ScheduledTask>> iterator = tasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ScheduledTask> entry = iterator.next();
            ScheduledTask task = entry.getValue();

            if (currentTick >= task.nextExecutionTick) {
                try {
                    if (task.async) {
                        CompletableFuture.runAsync(task.runnable);
                    } else {
                        task.runnable.run();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error Occurred while running a scheduled task: {}", e.getMessage());
                }

                if (task.cyclic) {
                    tasks.put(entry.getKey(), task.next(currentTick));
                } else {
                    iterator.remove();
                }
            }
        }
    }

    public record ScheduledTask(Runnable runnable, int intervalTicks, boolean cyclic, boolean async, int nextExecutionTick) {
        public ScheduledTask next(int currentTick) {
            return new ScheduledTask(runnable, intervalTicks, cyclic, async, currentTick + intervalTicks);
        }
    }
}