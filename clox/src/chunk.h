#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum {
    OP_CONSTANT,
    OP_NEGATE,
    OP_ADD,
    OP_SUBTRACT,
    OP_MULTIPLY,
    OP_DIVIDE,
    OP_RETURN
} OpCode;

typedef struct {
    int count;
    int capacity;
    int* lines;
    uint8_t* code;
    ValueArray contants;
} Chunk;

void initChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
int writeConstant(Chunk* chunk, Value value);
void freeChunk(Chunk* chunk);

#endif