#pragma once

class Node
{
public:
	int key;
	Node* right_child;
	Node* left_child;
	bool is_leaf;

	Node() 
	{
		key = 0;
		right_child = nullptr;
		left_child = nullptr;
		is_leaf = true;
	}

	Node(int var)
	{
		key = var;
		right_child = nullptr;
		left_child = nullptr;
		is_leaf = true;
	}
};



