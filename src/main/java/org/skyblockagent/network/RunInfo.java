package org.skyblockagent.network;

public record RunInfo(String id, int promptTokens, int completionTokens, int totalTokens, double totalTime, double tps) { }