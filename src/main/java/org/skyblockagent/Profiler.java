package org.skyblockagent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Profiler {
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final int SAMPLE_SIZE = 100;
    private static final Map<String, Profiler> PROFILERS = new HashMap<>();

    private final String name;
    private final Deque<Double> samples = new ArrayDeque<>(SAMPLE_SIZE);
    private double totalTime = 0;
    private double maxTime = Double.MIN_VALUE;
    private double minTime = Double.MAX_VALUE;
    private long startTime;

    private Profiler(String name) {
        this.name = name;
    }

    public static void start(String name) {
        get(name).startTiming();
    }

    public static void stop(String name) {
        get(name).stopTiming();
    }

    private static Profiler get(String name) {
        return PROFILERS.computeIfAbsent(name, Profiler::new);
    }

    private void startTiming() {
        startTime = System.nanoTime();
    }

    private void stopTiming() {
        double timeTakenMs = (System.nanoTime() - startTime) / 1_000_000.0;

        if (samples.size() == SAMPLE_SIZE) {
            double removed = samples.removeFirst();
            totalTime -= removed;
        }

        samples.addLast(timeTakenMs);
        totalTime += timeTakenMs;

        maxTime = Math.max(maxTime, timeTakenMs);
        minTime = Math.min(minTime, timeTakenMs);
    }

    public static void renderHud(DrawContext context) {
        context.drawText(textRenderer, "Fps: " + MinecraftClient.getInstance().getCurrentFps(), 10, 10, 0xFFFFFFFF, true);
        int offsetY = 10;
        for (Profiler profiler : PROFILERS.values()) {
            String line = String.format("%s: avg %.2f ms | max %.2f | min %.2f", profiler.name, profiler.getAverageTime(), profiler.getMaxTime(), profiler.getMinTime());
            context.drawText(textRenderer, line, 10, 10 + offsetY, 0xFFFFFFFF, true);
            offsetY += 10;
        }
    }

    public double getAverageTime() {
        return samples.isEmpty() ? 0 : totalTime / samples.size();
    }

    public double getMaxTime() {
        return maxTime;
    }

    public double getMinTime() {
        return minTime;
    }

    public static void resetAll() {
        for (Profiler profiler : PROFILERS.values()) {
            profiler.reset();
        }
    }

    private void reset() {
        samples.clear();
        totalTime = 0;
        maxTime = Double.MIN_VALUE;
        minTime = Double.MAX_VALUE;
    }
}
