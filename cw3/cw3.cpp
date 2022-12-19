#include <vector>
#include <iostream>
#include <string>
#include "Experiment.h"
#include <tbb/task_arena.h>
#include "Profiler.h"

int main(int argc, char* argv[]) {

    Profiler p;
    tbb::task_arena arena(4);
    arena.execute([&] { run(); });

//    test();

    return 0;
}