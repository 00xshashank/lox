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
    uint8_t* code;
    // Change line information storage to be more efficient
    int* lines;
    ValueArray constants;
} Chunk;

void initChunk(Chunk* chunk);
int addConstant(Chunk* chunk, Value value);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
int getLine(Chunk* chunk, uint8_t byte);
void freeChunk(Chunk* chunk);

#endif