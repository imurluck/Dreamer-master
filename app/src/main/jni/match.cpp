//
// Created by lijialin on 2017/4/7.
//

#include <opencv2/opencv.hpp>
#include <opencv2/core/internal.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <string.h>
#include <iostream>
#include "countermatch.h"
#include "Line.h"
#include <math.h>

#define MAX 1.7976931348623158e+308
#define MAXK 100000

using namespace cv;
using namespace std;


//构造Line
vector<Line> createLine(vector<Vec4i> lines) {
    vector<Line> LineSet;
    for(int i = 0; i < lines.size(); i++) {
        Line *line = new Line(lines[i]);
        LineSet.push_back(*line);
    }
    return LineSet;
}


//计算中点
Point getMidPoint(Line line) {
    double mid_x = (line.start.x + line.end.x) / 2;
    double mid_y = (line.start.y + line.end.y) / 2;
    Point *mid = new Point(mid_x, mid_y);
    return *mid;
}


//计算两个点的距离
double positionDiff(Point p1, Point p2) {
    return abs(p1.x - p2.x) + abs(p1.y - p2.y);
}


//计算平均斜率（用于计算TK）
double averageK(vector<Line> LineSet) {
    double avg = 0;
    int count = 0;
    for(int i = 0; i < LineSet.size(); i++) {
        if(LineSet[i].k != MAXK) {
            avg += LineSet[i].k;
            count++;
        }
    }
    avg /= count;
    return avg;
}


//计算TP
double getTP(InputArray m1, InputArray m2) {
    return (m1.getMat().rows + m2.getMat().rows) / 6;
}


//计算两条直线夹角
double getAngle(double k1, double k2) {
    return atan( abs(k2 - k1) / (1 + k1 * k2) );
}


/*
 计算两个矩阵的相似度
 matlab中的corr2()函数，好麻烦
*/
double calculateMean(vector<vector<double>> m) {

    vector<double> *mean = new vector<double>();
    int p = 0;
    for(int j = m[p].size()-1; j>=0; j--) {
        double count = 0;
        for(int i = 0, k = j; i <= m[p].size()-1; k--, i++) {
            count += m[i][k];
        }
        count /= m.size();
        (*mean).push_back(count);
        p++;
    }

    double count = 0;
    for(int i = 0; i < (*mean).size(); i++) {
        count += (*mean)[i];
    }
    count /= ((*mean).size()+1);
    return count;
}

double calculateCorr2(vector<vector<double>> m1,
                      vector<vector<double>> m2) {

    double mean1 = calculateMean(m1);
    double mean2 = calculateMean(m2);

    //计算分子
    double numerator = 0;
    for(int i = 0; i < m1.size(); i++) {
        for(int j = 0; j < m1[i].size(); j++) {
            numerator += (m1[i][j] - mean1) * (m2[i][j] - mean2);
        }
        for(int j = m1[i].size(); j <= m1.size(); j++) {
            numerator += mean1 * mean2;
        }
    }

    //计算分母 sqrt(pow(x,2) + pow(y,2));
    double d1 = 0;
    double d2 = 0;
    for(int i = 0; i < m1.size(); i++) {
        for(int j = 0; j < m1[i].size(); j++) {
            d1 += pow((m1[i][j] - mean1),2);
            d2 += pow((m2[i][j] - mean2),2);
        }
        for(int j = m1[i].size(); j <= m1.size(); j++) {
            d1 += pow(mean1,2);
            d2 += pow(mean2,2);
        }
    }
    double denominator = sqrt(d1) * sqrt(d2);

    if(numerator == 0) return 0.0;
    return numerator/denominator;
}



/*
  计算两组直线的匹配度
  算法步骤如下：
  1.计算每组直线的斜率，计算斜率阈值TK、距离阈值TP
  2.根据斜率、距离的差值是否满足阈值，找到最佳匹配直线对
  3.计算每组中的直线与本组中的其他直线之间的夹角
  4.计算夹角矩阵之间的相似度，并把这个相似度，作为直线的匹配度，返回
*/
double match(vector<Vec4i> lines1, vector<Vec4i> lines2, InputArray m1, InputArray m2) {

    // step1. 对每一条直线计算斜率
    vector<Line> lineSet1 = createLine(lines1);
    vector<Line> lineSet2 = createLine(lines2);

    //计算平均k
    double t1 = averageK(lineSet1);
    double t2 = averageK(lineSet2);
    double TK = t1 == t2 ? abs(t1) : abs(t1 - t2);
    double TP = getTP(m1, m2);
    if(TK <= 0) return 0.0;

    // step2. 根据斜率、距离之间的差值，配对
    vector<vector<Line>> *pairSet = new vector<vector<Line>>();

    for(int i = 0; i < lineSet1.size(); i++) {
        Line line1 = lineSet1[i];
        double min_diff = MAX;
        int index = 0;
        Line *min_line = new Line();

        for(int j = 0; j < lineSet2.size(); j++) {
            Line line2 = lineSet2[j];
            if(abs(line1.k - line2.k) < TK) { //判断1. 斜率差值在TK内
                Point mid1 = getMidPoint(line1);
                Point mid2 = getMidPoint(line2);
                double diff = positionDiff(mid1, mid2);
                if(diff < min_diff) {
                    min_diff = diff;
                    index = j;
                    *min_line = line2;
                }
            }
        }

        if(min_diff < TP) { //判断2. 距离差值在TP内
            vector<Line> *v = new vector<Line>();
            (*v).push_back(line1);
            (*v).push_back(*min_line);
            (*pairSet).push_back(*v);
        }
        delete min_line;

    }

    //画出图像，便于分析
    /*
    Mat src1(m1.getMat().rows,m1.getMat().cols, CV_8UC3, Scalar(255,255,255));
    Mat src2(m2.getMat().rows,m2.getMat().cols, CV_8UC3, Scalar(255,255,255));
    for(int i = 0; i < (*pairSet).size(); i++) {
        vector<Line> v = (*pairSet)[i];

        line( src1, v[0].start, v[0].end, Scalar(255-i*10,255-i*10,i*10), 3, CV_AA);
        line( src2, v[1].start, v[1].end, Scalar(255-i*10,255-i*10,i*10), 3, CV_AA);
        //cout<<"("<<v[0].start<<","<<v[0].end<<") ("<<v[1].start<<","<<v[1].end<<")"<<" "<<255-i*10<<" "<<i*10<<endl;
    }
    imshow("1", src1);
    imshow("2", src2);
     */

    //计算直线之间的误差O(n2)
    //计算直线与本张图像中的其他直线的夹角
    vector<vector<double>> *angles_list1 = new vector<vector<double>>();
    vector<vector<double>> *angles_list2 = new vector<vector<double>>();

    for(int i = 0; i < (*pairSet).size(); i++) {
        vector<Line> v1 = (*pairSet)[i];
        vector<double> *angles1 = new vector<double>();
        vector<double> *angles2 = new vector<double>();

        for(int j = i+1; j < (*pairSet).size(); j++) {
            vector<Line> v2 = (*pairSet)[j];
            (*angles1).push_back(getAngle(v1[0].k, v2[0].k));
            (*angles2).push_back(getAngle(v1[1].k, v2[1].k));
        }
        (*angles_list1).push_back(*angles1);
        (*angles_list2).push_back(*angles2);
        delete angles1;
        delete angles2;
    }

    // 计算夹角矩阵的相似度
    double rate = calculateCorr2((*angles_list1),(*angles_list2));
    rate *= (double)(*pairSet).size() / lineSet1.size();
    return rate;
}