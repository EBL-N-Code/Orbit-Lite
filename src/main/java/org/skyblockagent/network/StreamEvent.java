package org.skyblockagent.network;

public sealed interface StreamEvent permits StreamEvent.Chunk, StreamEvent.Finish {
    record Chunk(String text) implements StreamEvent {}
    record Finish() implements StreamEvent {}
}