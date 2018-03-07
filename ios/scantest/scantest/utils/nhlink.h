//
//  nhlink.h
//  scantest
//
//  Created by lakshmana on 05/03/18.
//  Copyright © 2018 nearhop. All rights reserved.
//

#ifndef nhlink_h
#define nhlink_h

#include <stdio.h>
struct Node {
    int data;
    struct Node* next;
};

struct BBSTNode {
    int data;
    struct BBSTNode* left;
    struct BBSTNode* right;
};


void testList();
struct Node* getRoot();
int getLength();
int getDataAt(int pos, int* data);
struct Node* addDataAt(int pos, int inData);
struct Node* deleteNodeAt(int pos);
struct Node* reverseList();
struct Node* reverseListRecursive(struct Node* first);


struct Node* root = NULL;
struct BBSTNode* rootBBST = NULL;

void addNodeBBST(struct BBSTNode** cNode, int inData);
struct BBSTNode* findDataInBBST(struct BBSTNode* cNode,  int inData);
struct BBSTNode* removeNodeBBST(struct BBSTNode* cNode, int inData);
struct BBSTNode* getminValue(struct BBSTNode* cNode);
#endif /* nhlink_h */
