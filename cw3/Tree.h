#pragma once

#include "Node.h"
#include <tbb/spin_mutex.h>
#include <vector>

class Tree
{
public:
	Node* root;

	Tree() 
	{
		root = nullptr;
	}

	Tree(Node* root_ptr)
	{
		root = root_ptr;
	}

	bool insert_node(int var);
	bool search_node(int var);
	bool delete_node(int var);
	bool check(Node * root);
};

