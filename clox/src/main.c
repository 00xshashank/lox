#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

static void repl() {
    char line[1024];
    for(;;) {
        printf(">> ");

        if(!fgets(line, sizeof(line), stdin)) {
            printf("\n");
            break;
        }

        interpret(line);
    }
}

static char* readFile(const char* path) {
    FILE* file = fopen(path, "rb");
    if (!file) {
        fprintf(stderr, "Could not open file \"%s\".", path);
        exit(74);
    }

    fseek(file, 0L, SEEK_END);
    size_t fileSize = ftell(file);
    rewind(file);

    if (fileSize == (size_t)-1) {
        perror("Error determining file size");
        fclose(file);
        return NULL;
    }

    char* buffer = (char*)malloc(fileSize + 1);
    if (!buffer) {
        fprintf(stderr, "Notenough memory to read file \"%s\".", path);
        fclose(file);
        exit(74);
    }

    size_t bytesRead = fread(buffer, 1, fileSize, file);
    if (bytesRead != fileSize) {
        perror("Error reading file");
        free(buffer);
        fclose(file);
        return NULL;
    }

    buffer[bytesRead] = '\0';

    fclose(file);
    return buffer;
}


static void runFile(const char* path) {
    char* source = readFile(path);
    InterpretResult result = interpret(source);
    free(source);

    if (result == INTERPRET_COMPILE_ERROR) { exit(65); }
    if (result == INTERPRET_RUNTIME_ERROR) { exit(70); }
}

int main(int argc, char* argv[]) {
    initVM();

    if (argc == 1) {
        repl();
    } else if (argc == 2) {
        runFile(argv[1]);
    } else {
        fprintf(stderr, "Usage: clox [script]\n");
        exit(64);   
    }
    
    freeVM();
    return 0;
}