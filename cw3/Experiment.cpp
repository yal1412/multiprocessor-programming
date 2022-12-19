#include "Experiment.h"
#include "Profiler.h"
#include <iostream>
#include <tbb/parallel_for.h>
#include <vector>
#include <tbb/task_arena.h>
#include <stdlib.h>
#include <time.h>

void run()
{
    srand(time(NULL));

    Node r = Node(4);

    Tree t = Tree(&r);

    int num, a;

    tbb::parallel_for(tbb::blocked_range<int>(0, 100000), [&](tbb::blocked_range<int> r)
        {
            for (auto i = r.begin(); i != r.end(); i++)
            {
                num = 1 + rand() % 10000;
                a = rand() % 3;
                switch (a)
                {
                case 0: 
                    t.insert_node(num);
                    break;
                case 1:
                    t.search_node(num);
                    break;
                case 2:
                    t.delete_node(num);
                    break;
                default:
                    break;
                }
            }
        });

    std::cout << "check " << t.check(t.root) << std::endl;
}

void test() {
    Node* r = new Node(4);

    Tree t = Tree(r);

    t.insert_node(5);
    t.delete_node(5);
    t.delete_node(3);
    t.delete_node(4);
    t.search_node(3);
    t.insert_node(7);

    std::cout << "check " << t.check(t.root) << std::endl;
}
