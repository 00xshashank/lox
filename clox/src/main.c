#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, char* argv[]) {
    initVM();

    Chunk chunk;
    initChunk(&chunk);
    
    int constant = writeConstant(&chunk, 1.2);
    writeChunk(&chunk, OP_CONSTANT, 1);
    writeChunk(&chunk, constant, 1);

    constant = writeConstant(&chunk, 3.4);
    writeChunk(&chunk, OP_CONSTANT, 2);
    writeChunk(&chunk, constant, 2);

    writeChunk(&chunk, OP_ADD, 2);

    constant = writeConstant(&chunk, 5.6);
    writeChunk(&chunk, OP_CONSTANT, 2);
    writeChunk(&chunk, constant, 2);

    writeChunk(&chunk, OP_DIVIDE, 3);

    writeChunk(&chunk, OP_NEGATE, 4);
    writeChunk(&chunk, OP_RETURN, 5);

    disassembleChunk(&chunk, "Test Chunk");
    interpret(&chunk);
    
    freeVM();
    freeChunk(&chunk);

    return 0;
}