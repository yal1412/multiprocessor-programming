#pragma once

#include <tbb/spin_mutex.h>

class Node
{
public:
	int key;
	Node* right_child;
	Node* left_child;
	Node* parent;
	bool is_leaf;
	bool is_removed;

	Node(int var = 0) 
	{
		key = var;
		right_child = nullptr;
		left_child = nullptr;
		parent = nullptr;
		is_leaf = true;
		is_removed = false;
	}
};



