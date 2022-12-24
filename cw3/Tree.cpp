#include "Tree.h"

typedef tbb::spin_mutex NodeMutexType;
NodeMutexType NodeMutex;

bool Tree::insert_node(int var)
{
	Node* pred_node = nullptr;
	Node* curr_node = root;

	while (true)
	{
		if (root == nullptr)
		{
			tbb::spin_mutex lock(NodeMutexType);
			if (root == nullptr) {
				root = new Node(var);
				return true;
			}
			else {
				curr_node = root;
			}
		}
		{
			tbb::spin_mutex lock(NodeMutexType);

			if (curr_node->is_leaf && !curr_node->is_removed)
			{


				if (curr_node->key == var)
				{
					return false;
				}
				if (pred_node == nullptr || !pred_node->is_leaf)
				{
					Node* new_left_child, * new_right_child;
					if (curr_node->key > var)
					{
						new_left_child = new Node(var);
						new_right_child = new Node(curr_node->key);
					}
					else
					{
						new_left_child = new Node(curr_node->key);
						new_right_child = new Node(var);
						curr_node->key = var;
					}
					curr_node->left_child = new_left_child;
					curr_node->right_child = new_right_child;
					curr_node->is_leaf = false;
				}

				return true;
			}
		}

		pred_node = curr_node;
		if (curr_node->key > var)
		{
			curr_node = curr_node->left_child;
		}
		else
		{
			curr_node = curr_node->right_child;
		}
	}
}

bool Tree::search_node(int var)
{
	if (root == nullptr)
	{
		return false;
	}

	Node* curr_node = root;
	while (true)
	{
		if (curr_node->is_leaf == true)
		{
			if (curr_node->key == var && !curr_node->is_removed)
			{
				return true;
			}
			return false;
		}
		if (curr_node->key > var)
		{
			curr_node = curr_node->left_child;
		}
		else
		{
			curr_node = curr_node->right_child;
		}
	}

	return false;
}

bool Tree::delete_node(int var)
{
	if (root == nullptr)
	{
		return false;
	}

	{
		NodeMutexType::scoped_lock lock(NodeMutex);
		Node* gprev_node = nullptr;
		Node* prev_node = nullptr;
		Node* curr_node = root;

		if (curr_node->is_leaf == true)
		{
			if (curr_node->key != var)
			{
				return false;      // не удалось удалить
			}
			delete root;
			root = nullptr;
			return true;
		}

		while (true)
		{
			gprev_node = prev_node;
			prev_node = curr_node;

			if (curr_node->key > var)
			{
				curr_node = curr_node->left_child;
			}
			else
			{
				curr_node = curr_node->right_child;
			}
			if (curr_node->is_leaf == true)
			{
				if (curr_node->key != var)
				{
					return false;      // не удалось удалить
				}

				if (prev_node == nullptr)
				{
					delete root;
					root = nullptr;
				}
				else
				{
					if (prev_node->left_child == curr_node)
					{
						prev_node->key = prev_node->right_child->key;
					}
					else
					{
						prev_node->key = prev_node->left_child->key;
					}

					delete prev_node->left_child;
					prev_node->left_child = nullptr;
					delete prev_node->right_child;
					prev_node->right_child = nullptr;
					prev_node->is_leaf = true;
				}
				return true;
			}
		}
	}
}

bool Tree::check(Node* root)
{
	if (root == nullptr)
	{
		return true;
	}

	Node* curr_node = root;
	if (!curr_node->is_leaf && (curr_node->left_child == nullptr || curr_node->right_child == nullptr))
		return false;

	if (curr_node->is_leaf && (curr_node->left_child == nullptr && curr_node->right_child == nullptr))
		return true;

	if (curr_node->left_child->key >= curr_node->key)
		return false;
	if (curr_node->right_child->key < curr_node->key)
		return false;

	if (check(curr_node->left_child) && check(curr_node->right_child))
		return true;
}
