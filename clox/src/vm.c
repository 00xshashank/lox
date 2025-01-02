#include "vm.h"
#include "common.h"

VM vm;

void initVM() {}

static InterpretResult run() {  
    #define READ_BYTE() (*vm.ip++)

    for (;;) {
        uint8_t instruction;
        switch(instruction = READ_BYTE()) {
            case OP_RETURN: {
                return INTERPRET_OK;
            }
        } 
    }
    #undef READ_BYTE
}

InterpretResult interpret(Chunk* chunk) {
    vm.chunk = chunk;
    vm.ip = vm.chunk->code;
    return run();
}

void freeVM() {}