//
// Created by lijialin on 2017/4/6.
//

#include "HeaderFiles/MinHeap.h"
#include <iostream>
#include <opencv2/opencv.hpp>
#include <math.h>

using namespace std;
using namespace cv;

MinHeap::MinHeap(int k) {
    maxsize = k;
}

MinHeap::~MinHeap() {
    minheap.clear();
    //delete (*minheap); //释放空间
}

// 创建小顶堆
void MinHeap::createMinHeap(vector<Vec4i> a) {
    for(int i = 0; i < maxsize; i++) {
        minheap.push_back(a[i]);
    }
}

//插入元素
void MinHeap::insert(Vec4i line) {
    if(getLength(line) > getLength(getTop())) {
        minheap[0] = line;
        filterDown(0);
    }
}

//向下调整
void MinHeap::filterDown(int current) {
    int end = minheap.size()-1;
    int child = current * 2 + 1; //当前节点的左孩子
    Vec4i line = minheap[current]; //保存当前节点

    while(child <= end) {
        // 选出两个孩子中的较小孩子
        if(child < end && getLength(minheap[child+1]) < getLength(minheap[child]))
            child++;
        if(getLength(line) < getLength(minheap[child])) break;
        else {
            minheap[current] = minheap[child];//孩子节点覆盖当前节点
            current = child;
            child = child * 2 + 1;
        }
    }
    minheap[current] = line;
}

//获取堆顶元素
Vec4i MinHeap::getTop() {
    if(minheap.size() != 0)
        return minheap[0];
    return NULL;
}

//计算距离
double MinHeap::getLength(Vec4i line) {
    double x = line[0]-line[2];
    double y = line[1]-line[3];
    return sqrt(pow(x,2) + pow(y,2));
}

// 获取堆中的全部元素
vector<Vec4i> MinHeap::getHeap() {
    vector<Vec4i> heap;
    for(int i = 0; i < minheap.size(); i++)
        heap.push_back(minheap[i]);
    return heap;
}