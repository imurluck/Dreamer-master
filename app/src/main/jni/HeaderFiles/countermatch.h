//
// Created by lijialin on 2017/4/6.
//

#ifndef DREAMERAJNI_COUNTERMATCH_H
#define DREAMERAJNI_COUNTERMATCH_H

#endif DREAMERAJNI_COUNTERMATCH_H

#include<opencv2/opencv.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc/imgproc.hpp>
#include <iostream>

using namespace cv;
using namespace std;

void myCanny( InputArray _src, OutputArray _dst,
              double low_thresh, double high_thresh,
              int aperture_size);

void myHoughLinesP(InputArray _image, OutputArray _lines,
                   double rho, double theta, int threshold,
                   double minLineLength, double maxGap);

double match(vector<Vec4i> lines1, vector<Vec4i> lines2, InputArray m1, InputArray m2);