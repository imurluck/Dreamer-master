//
// Created by lijialin on 2017/4/6.
//

#ifndef DREAMERAJNI_LINE_H
#define DREAMERAJNI_LINE_H

#endif DREAMERAJNI_LINE_H

#include <opencv2/opencv.hpp>
#include <vector>

using namespace std;
using namespace cv;

class Line {
public:
    double width;  //所在图像的宽
    double height; //所在图像的高
    Vec4i line;  //直线本身
    Point start; //直线的起点
    Point end;   //直线的终点
    double length; //直线的长度
    double len; //直线的相对长度（相对图像的高）
    double k; //直线的斜率
    double theta; //直线与水平方向的夹角
    vector<double> angles; //夹角向量
    vector<Line> parallels; //平行线集合
    vector<Line> verticals; //垂直线集合

    Line(Vec4i line);
    Line(void);
    ~Line(void);
};