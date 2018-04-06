//
//  nhlink.c
//  scantest
//
//  Created by lakshmana on 05/03/18.
//  Copyright © 2018 nearhop. All rights reserved.
//
#include <stdlib.h>
#include <string.h>
#include "nhlink.h"




void testList(){
    struct Node* myNode1 = addDataAt(0, 3);
    struct Node* myNode2 = addDataAt(1, 2);
    struct Node* myNode3 = addDataAt(2, 5);
    struct Node* myNode4 = addDataAt(3, 9);
    struct Node* myNode5 = addDataAt(4, 6);
    struct Node* myNode6 = addDataAt(5, 11);
    struct Node* myNode7 = addDataAt(6, 19);
    struct Node* myNode8 = addDataAt(7, 4);
    struct Node* myNode9 = addDataAt(3, 8);

    int linkLen = 0;
    struct Node* temp = root;
    while(temp->next != NULL){
        linkLen += 1;
        temp = temp->next;
    }
    mergeSort(root, 0, linkLen);
//    int c;
//    int a = getDataAt(2, &c);
//    struct Node* myNode6 = deleteNodeAt(3);
//    struct Node* myNode7 = addDataAt(3, 3);
//    int b = getDataAt(3, &c);
//    struct Node* myNode8 = reverseList();
//    struct Node* myNode9 = reverseListRecursive(myNode8);

    

//    addNodeBBST(&rootBBST,4);
//    addNodeBBST(&rootBBST,5);
//    addNodeBBST(&rootBBST,6);
//    addNodeBBST(&rootBBST,7);
//    addNodeBBST(&rootBBST,1);
//    struct BBSTNode* myNode1 = findDataInBBST(rootBBST, 7);
//    addNodeBBST(&rootBBST,2);
//    addNodeBBST(&rootBBST,3);
//    struct BBSTNode* myNode2 = removeNodeBBST(rootBBST,6);
//    struct BBSTNode* myNode3 = findDataInBBST(rootBBST, 3);
}

struct BBSTNode* getRootBBST(){
    return rootBBST;
}

void addNodeBBST(struct BBSTNode** cNode, int inData){
    if(*cNode == NULL){
        struct BBSTNode* node = malloc(1 * sizeof(struct BBSTNode));
        memset(node,0x00,sizeof( struct BBSTNode));
        node->data = inData;
        *cNode = node;
    }else{
        if(inData < (*cNode)->data){
            addNodeBBST(&((*cNode)->left), inData);
        }else if(inData > (*cNode)->data){
            addNodeBBST(&((*cNode)->right), inData);
        }
    }
}

struct BBSTNode* findDataInBBST(struct BBSTNode* cNode,  int inData){
    struct BBSTNode* findNode = NULL;
    if(cNode == NULL || cNode->data == inData){
        return  cNode;
    }
    if(inData < cNode->data){
        findNode = findDataInBBST(cNode->left,inData);
    }else{
        findNode = findDataInBBST(cNode->right,inData);
    }
    return findNode;
}

struct BBSTNode* getminNode(struct BBSTNode* cNode) {
    if (cNode->left == NULL)
        return cNode;
    else
        return getminNode(cNode->left);
}


struct BBSTNode* getmaxNode(struct BBSTNode* cNode) {
    if (cNode->right == NULL)
        return cNode;
    else
        return getminNode(cNode->right);
}


struct BBSTNode* removeNodeBBST(struct BBSTNode* cNode, int inData){

    if(cNode == NULL){
        return cNode;
    }
    if(inData < cNode->data){
        if(cNode->left != NULL){
            cNode->left = removeNodeBBST(cNode->left,inData);
        }
    }else if(inData > cNode->data){
        if(cNode->right != NULL){
           cNode->right = removeNodeBBST(cNode->right,inData);
        }
    }else{
        if(cNode->left == NULL){
            struct BBSTNode* temp = cNode->right;
            free(cNode);
            return temp;
        }else if(cNode->right == NULL){
            struct BBSTNode* temp = cNode->left;
            free(cNode);
            return temp;
        }else{
            struct BBSTNode* temp  = getminNode(cNode->right);
            cNode->data = temp->data;
            cNode->right = removeNodeBBST(cNode->right, temp->data);
        }
    }
    return cNode;
}




struct Node* getRoot(){
    return root;
}

int getLength(){
    int counter = 0;
    struct Node* temp = root;
    while(temp != NULL){
        counter = counter + 1;
        temp =  temp->next;
    }
    return counter;
}
int getDataAt(int pos, int* myData){
    if(root == NULL){
        return 0;
    }
    struct Node* temp = root;
    while(temp != NULL && pos >0 ){
        pos = pos - 1;
        temp =  temp->next;
    }
    *myData = temp->data;
    return 1;
}
struct Node* addDataAt(int pos , int inData){
    if(root == NULL){
        struct Node* node =  malloc(1 * sizeof(struct Node));
        if(node != NULL){
            memset(node,0x00,sizeof(struct Node));
            node->next = NULL;
            node->data = inData;
            root = node;
        }
    }else{
        struct Node* temp = root;
        if(getLength() <= pos){
            struct Node* node =  malloc(1 * sizeof(struct Node));
            if(node != NULL){
                memset(node,0x00,sizeof(struct Node));
                node->data = inData;
                node->next = NULL;
                while(temp->next != NULL){
                    temp = temp->next;
                }
                temp->next = node;
                return root;
            }else{
                return root;
            }
        }else{
            struct Node* node =  malloc(1 * sizeof(struct Node));
            if(node != NULL){
                memset(node,0x00,sizeof(struct Node));
                node->data = inData;
                node->next = NULL;
                while(temp->next != NULL && pos > 1 ){
                    pos = pos - 1;
                    temp =  temp->next;
                }
                node->next = temp->next;
                temp->next = node;
                return root;
            }else{
                return root;
            }
        }
        return root;
    }
    return root;
}
struct Node* deleteNodeAt(int pos){
    struct Node* temp = root;
    while(temp->next != NULL && pos > 1 ){
        pos = pos -1;
        temp = temp->next;
    }
    if(pos > 1){
        return root;
    }
    if(temp != NULL){
        struct Node* delNode = temp->next;
        temp->next = temp->next->next;
        free(delNode);
    }
    return root;
    
}
struct Node* reverseList(){
    struct Node* currNode = root;
    struct Node* prevNode = NULL;
    struct Node* nextNode = NULL;
    while(currNode != NULL){
        nextNode = currNode->next;
        currNode->next = prevNode;
        prevNode = currNode;
        currNode = nextNode;
    }
    root = prevNode;
    return root;
}

struct Node* reverseListRecursive(struct Node* first){
    
    if(first == NULL) return NULL; // list does not exist.
    
    if(first->next == NULL) return first; // list with only one node.
    
    struct Node* rest = reverseListRecursive(first->next); // recursive call on rest.
    
    first->next->next = first; // make first; link to the last node in the reversed rest.
    
    first->next = NULL; // since first is the new last, make its link NULL.
    
    return rest; // rest
}

void  merge(struct Node* start1 , struct Node* start2){
    
}
struct Node*  mergeSort(struct Node* startNode, int low, int high){
    
    if(low < high){
    int mid =   low+(high-low)/2;
        // Sort first and second halves
        mergeSort(startNode, low, mid);
        mergeSort(startNode, mid+1, high);
        
        int a = 1;
    }
    
    return NULL;
}



