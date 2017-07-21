//
// Created by lijialin on 2017/4/6.
//

#include "HeaderFiles/Line.h"
#include <opencv2/opencv.hpp>
#include <math.h>

#define PI 3.1415926
#define MAXNUM 100000

using namespace cv;
using namespace std;

Line::Line(Vec4i l) {
    line = l;

    // 设定端点
    start = Point(l[0], l[1]);
    end = Point(l[2], l[3]);

    // 计算直线的斜率
    if(end.x - start.x != 0) {
        k = (double)(start.y - end.y) / (start.x - end.x);
    }else {
        k = MAXNUM;
    }

    //计算直线与水平方向的夹角
    theta = atan(k) * 180 / PI;
}

Line::Line(void) {
}

Line::~Line(void){
}
