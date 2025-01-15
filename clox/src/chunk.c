#include <stdlib.h>
#include <stdio.h>

#include "chunk.h"
#include "memory.h"

void initChunk(Chunk* chunk) {
    chunk->capacity = 0;
    chunk->count = 0;
    chunk->code = NULL;
    chunk->lines = NULL;
    initValueArray(&chunk->contants);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
    if (chunk->capacity < chunk->count + 1) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        // printf("Capacity: %d\n", chunk->capacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
        chunk->lines = GROW_ARRAY(int, chunk->lines, oldCapacity, chunk->capacity);
    }

    chunk->code[chunk->count] = byte;
    chunk->lines[chunk->count] = line;
    chunk->count++;
}

int writeConstant(Chunk* chunk, Value value) {
    writeValueArray(&chunk->contants, value);
    return chunk->contants.count-1;
}

void freeChunk(Chunk* chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    FREE_ARRAY(int, chunk->lines, chunk->capacity);
    freeValueArray(&chunk->contants);
    initChunk(chunk);
}