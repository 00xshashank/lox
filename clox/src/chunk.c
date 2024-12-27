#include <stdlib.h>
#include "chunk.h"
#include "memory.h"

#include <stdio.h>

void initChunk(Chunk *chunk) {
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;
    chunk->lines = NULL;
    initValueArray(&chunk->constants);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {    
    if (chunk->capacity == 0) {
        chunk->capacity = GROW_CAPACITY(0);  
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, 0, chunk->capacity);
        chunk->lines = GROW_ARRAY(int, chunk->lines, 0, chunk->capacity);
    } else if (chunk->capacity <= chunk->count) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
        chunk->lines = GROW_ARRAY(int, chunk->lines, oldCapacity, chunk->capacity);
    }

    int count = chunk->count;
    chunk->code[count] = byte;
    chunk->lines[count] = line;
    chunk->count++;
}

void freeChunk(Chunk *chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    FREE_ARRAY(int, chunk->lines, chunk->capacity);
    initChunk(chunk);
}

int addConstant(Chunk *chunk, Value value) {
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1;
}
