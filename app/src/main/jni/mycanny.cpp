//
// Created by lijialin on 2017/4/6.
//

#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>

#define CANNY_SHIFT 15

using namespace cv;
using namespace std;

/*
 * 卷积计算，求梯度
 */
void filter(Mat src, Mat dst, float k[9]) {
    Mat kernel = Mat(3, 3, CV_32FC1, k);
    filter2D(src, dst, CV_16S, kernel, Point(-1,-1), 0.0, 4);
}


void myCanny( InputArray _src, OutputArray _dst,
              double low_thresh, double high_thresh,
              int aperture_size) {

    Mat src = _src.getMat();
    CV_Assert( src.depth() == CV_8U );
    //扩大src的边界，利于后续5x5模板运算
    //copyMakeBorder(src, src, 2, 2, 2, 2, IPL_BORDER_REPLICATE);
    _dst.create(src.size(), CV_8U);
    Mat dst = _dst.getMat();

    if (low_thresh > high_thresh)
        std::swap(low_thresh, high_thresh);

    const int cn = src.channels();
    CV_Assert(cn == 1);

    //改进Scharr滤波器
    //添加45°、135°、180°、225°、270°、315°方向的滤波器
    float k_x[9] = {-3,0,3,-10,0,10,-3,0,3};   //0
    float k_y[9] = {-3,-10,-3,0,0,0,3,10,3};   //90
    float k_45[9] = {0,3,10,-3,0,3,-10,-3,0};  //45
    float k_135[9] = {10,3,0,3,0,-3,0,-3,-10}; //135
    float k_180[9] = {3,0,-3,10,0,-10,3,0,-3}; //180
    float k_225[9] = {0,-3,-10,3,0,-3,10,3,0}; //225
    float k_270[9] = {-3,-10,-3,0,0,0,3,10,3}; //270
    float k_315[9] = {-10,-3,0,-3,0,3,0,3,10}; //315

    Mat dx(src.rows, src.cols, CV_16SC(cn));
    Mat dy(src.rows, src.cols, CV_16SC(cn));
    Mat d_45(src.rows, src.cols, CV_16SC(cn));
    Mat d_135(src.rows, src.cols, CV_16SC(cn));
    Mat d_180(src.rows, src.cols, CV_16SC(cn));
    Mat d_225(src.rows, src.cols, CV_16SC(cn));
    Mat d_270(src.rows, src.cols, CV_16SC(cn));
    Mat d_315(src.rows, src.cols, CV_16SC(cn));

    filter(src, dx, k_x);
    filter(src, dy, k_y);
    filter(src, d_45, k_45);
    filter(src, d_135, k_135);
    filter(src, d_180, k_180);
    filter(src, d_225, k_225);
    filter(src, d_270, k_270);
    filter(src, d_315, k_315);


    int low = cvFloor(low_thresh);
    int high = cvFloor(high_thresh);

    ptrdiff_t mapstep = src.cols + 4;

    //由3改为5
    AutoBuffer<uchar> buffer((src.cols+4)*(src.rows+4) + cn * mapstep * 5 * sizeof(int));

    int* mag_buf[5];
    mag_buf[0] = (int*)(uchar*)buffer;
    mag_buf[1] = mag_buf[0] + mapstep*cn;
    mag_buf[2] = mag_buf[1] + mapstep*cn;
    mag_buf[3] = mag_buf[2] + mapstep*cn;
    mag_buf[4] = mag_buf[3] + mapstep*cn;

    memset(mag_buf[0], 0, /* cn* */mapstep*sizeof(int));

    uchar* map = (uchar*)(mag_buf[4] + mapstep*cn);   //2->4
    memset(map, 1, mapstep);
    memset(map + mapstep*(src.rows + 1), 1, mapstep);

    int maxsize = std::max(1 << 10, src.cols * src.rows / 10);
    std::vector<uchar*> stack(maxsize);
    uchar **stack_top = &stack[0];
    uchar **stack_bottom = &stack[0];

    /* sector numbers
       (Top-Left Origin)

        1   2   3
         *  *  *
          * * *
        0*******0
          * * *
         *  *  *
        3   2   1
    */

    #define CANNY_PUSH(d)    *(d) = uchar(2), *stack_top++ = (d)
    #define CANNY_POP(d)     (d) = *--stack_top

    // calculate magnitude and angle of gradient, perform non-maxima suppression.
    // fill the map with one of the following values:
    //   0 - the pixel might belong to an edge
    //   1 - the pixel can not belong to an edge
    //   2 - the pixel does belong to an edge
    for (int i = 0; i <= src.rows; i++){
        int* _norm = mag_buf[(i > 0) + 1] + 1;

        if (i < src.rows) {
            short* _dx = dx.ptr<short>(i);
            short* _dy = dy.ptr<short>(i);
            short* _d45 = d_45.ptr<short>(i);
            short* _d135 = d_135.ptr<short>(i);
            short* _d180 = d_180.ptr<short>(i);
            short* _d225 = d_225.ptr<short>(i);
            short* _d270 = d_270.ptr<short>(i);
            short* _d315 = d_315.ptr<short>(i);

            for (int j = 0; j < src.cols * cn; j++) {
                _norm[j] = (
                                   abs(int(_dx[j])) + abs(int(_dy[j])) +
                                   abs(int(_d45[j])) + abs(int(_d135[j])) +
                                   abs(int(_d180[j])) + abs(int(_d225[j])) +
                                   abs(int(_d270[j])) + abs(int(_d315[j]))
                           ) / 4;
            }

            _norm[-1] = _norm[src.cols] = 0;
        }
        else
            memset(_norm-1, 0, /* cn* */mapstep * sizeof(int));



        // at the very beginning we do not have a complete ring
        // buffer of 3 magnitude rows for non-maxima suppression
        if (i == 0 )
            continue;


        uchar* _map = map + mapstep*i + 1;
        _map[-1] = _map[src.cols] = 1;

        int* _mag = mag_buf[2] + 1; // take the central row 1改为2
        /*ptrdiff_t magstep1 = mag_buf[2] - mag_buf[1];
        ptrdiff_t magstep2 = mag_buf[0] - mag_buf[1];*/
        ptrdiff_t magstep0 = mag_buf[0] - mag_buf[2];
        ptrdiff_t magstep1 = mag_buf[1] - mag_buf[2];
        ptrdiff_t magstep3 = mag_buf[3] - mag_buf[2];
        ptrdiff_t magstep4 = mag_buf[4] - mag_buf[2];

        const short* _x = dx.ptr<short>(i-1);
        const short* _y = dy.ptr<short>(i-1);

        if ((stack_top - stack_bottom) + src.cols > maxsize) {//分配栈空间
            int sz = (int)(stack_top - stack_bottom);
            maxsize = maxsize * 3/2;
            stack.resize(maxsize);
            stack_bottom = &stack[0];
            stack_top = stack_bottom + sz;
        }

        int prev_flag = 0;


        //-----------------------------------------
        //  【1】 自动调整阈值
        //  当背景很弱，阈值适当降低
        //  当背景很强，阈值适当增加
        //-----------------------------------------
        int sum = 0;
        for(int p = 0; p < src.cols; p++) {
            sum += _mag[p+magstep0] + _mag[p+magstep1] + _mag[p] + _mag[p+magstep3] + _mag[magstep4];
        }
        int n = src.cols * 5;
        int average = sum / n;
        if(average < cvFloor(low_thresh)) {
            low = cvFloor(low_thresh) - average/5;
            high = cvFloor(high_thresh) - average/5;
        }else {
            low = cvFloor(low_thresh) + average/5;
            high = cvFloor(high_thresh) + average/5;
        }


        for (int j = 0; j < src.cols; j++) {

            const int TG22 = (int)(0.4142135623730950488016887242097*(1<<CANNY_SHIFT) + 0.5);
            int m = _mag[j];

            if (m > low) { // 如果大于低阈值
                int xs = _x[j];
                int ys = _y[j];
                int x = std::abs(xs);
                int y = std::abs(ys) << CANNY_SHIFT;
                int tg22x = x * TG22;

                if (y < tg22x){
                    //if (m > _mag[j-1] && m >= _mag[j+1]) goto __ocv_canny_push;
                    if (m >= _mag[j-1] && m >= _mag[j+1] &&
                        m >= _mag[j-2] && m >= _mag[j+2]) {
                        bool flag = true;
                        for(int p = -1; p <= 1 ; p+=2) {
                            int xp = std::abs(_x[j+p]);
                            int yp = (int)std::abs(_y[j+p]) << CANNY_SHIFT;
                            int tg22xp = xp * TG22;
                            if(yp >= tg22xp) { flag = false; break;}
                        }
                        if(flag) goto __ocv_canny_push;
                    }

                }else {
                    int tg67x = tg22x + (x << (CANNY_SHIFT+1));
                    if (y > tg67x) {
                        //if (m > _mag[j+magstep1] && m >= _mag[j+magstep3]) goto __ocv_canny_push;
                        if (m >= _mag[j+magstep1] && m >= _mag[j+magstep3] &&
                            m >= _mag[j+magstep0] && m >= _mag[j+magstep4]) {
                            bool flag = true;
                            for(int p = -2; p <= 1; p+=3) {
                                if(i + p > 0 && i + p < dx.rows && i + p < dy.rows) {
                                    short* x = dx.ptr<short>(i+p);
                                    short* y = dy.ptr<short>(i+p);
                                    int xp = std::abs(x[0]);
                                    int yp = std::abs(y[0]);
                                    int tg22xp = xp * TG22;
                                    int tg67xp = tg22xp + (xp << (CANNY_SHIFT+1));
                                    if(yp < tg67xp) { flag = false; break;}
                                }
                            }
                            if(flag) goto __ocv_canny_push;
                        }

                    }else {
                        int s = (xs ^ ys) < 0 ? -1 : 1;
                        //if (m > _mag[j+magstep1-s] && m > _mag[j+magstep3+s]) goto __ocv_canny_push;
                        if (m >= _mag[j+magstep1-s] && m >= _mag[j+magstep3+s] &&
                            m >= _mag[j+magstep0-2*s] && m >= _mag[j+magstep4+2*s]) {
                            bool flag = true;
                            for(int p = -2,q=-1; p <= 1; p+=3, q+=2) {
                                if(i + p > 0 && i + p < dx.rows && i + p < dy.rows) {
                                    short* x = dx.ptr<short>(i+p)+q*s;
                                    short* y = dy.ptr<short>(i+p)+q*s;
                                    int xp = std::abs(x[0]);
                                    int yp = std::abs(y[0]);
                                    int tg22xp = xp * TG22;
                                    int tg67xp = tg22xp + (xp << (CANNY_SHIFT+1));
                                    if(yp >= tg22xp && yp <= tg67xp) { flag = false; break;}
                                }
                            }
                            if(flag) {
                                goto __ocv_canny_push;
                            }
                        }
                    }
                }
            }
            prev_flag = 0;
            _map[j] = uchar(1);
            continue;

            __ocv_canny_push:
            if (!prev_flag && m > high && _map[j-mapstep] != 2) {
                CANNY_PUSH(_map + j);
                prev_flag = 1;
            }
            else
                _map[j] = 0;
        }

        // scroll the ring buffer
        _mag = mag_buf[0];
        mag_buf[0] = mag_buf[1];
        mag_buf[1] = mag_buf[2];
        //mag_buf[2] = _mag;
        mag_buf[2] = mag_buf[3];
        mag_buf[3] = mag_buf[4];
        mag_buf[4] = _mag;
    }

    // now track the edges (hysteresis thresholding)
    while (stack_top > stack_bottom) {
        uchar* m;
        if ((stack_top - stack_bottom) + 8 > maxsize) {
            int sz = (int)(stack_top - stack_bottom);
            maxsize = maxsize * 3/2;
            stack.resize(maxsize);
            stack_bottom = &stack[0];
            stack_top = stack_bottom + sz;
        }

        CANNY_POP(m);

        /*
        改一下边缘跟踪策略
        首先，定义一个3层前向浅层神经网络，用于计算5x5邻域内的边缘方向
        输入为x1,x2,x3,x4,x5,x6,x7,x8
        */
        ptrdiff_t twostep = mapstep + mapstep;
        int a[5][5] = {
                {m[-twostep-2], m[-twostep-1], m[-twostep], m[-twostep+1], m[-twostep+2]},
                {m[-mapstep-2], m[-mapstep-1], m[-mapstep], m[-mapstep+1], m[-mapstep+2]},
                {m[-2],         m[-1],         m[0],        m[1],          m[2]},
                {m[mapstep-2],  m[mapstep-1],  m[mapstep],  m[mapstep+1],  m[mapstep+2]},
                {m[twostep-2],  m[twostep-1],  m[twostep],  m[twostep+1],  m[twostep+2]}
        };

        //输入层
        int x1 = (3*(a[0][2] + a[1][2]) + a[0][1] + a[0][3] + a[1][1] + a[1][3]) / 6;
        int x2 = (3*(a[1][3] + a[0][4]) + 1*(a[0][3] + a[1][4])) / 4;
        int x3 = (3*(a[2][3] + a[2][4]) + a[1][3] + a[1][4] + a[3][3] + a[3][4]) / 6;
        int x4 = (3*(a[3][3] + a[4][4]) + 1*(a[3][4] + a[4][3])) / 4;
        int x5 = (3*(a[3][2] + a[4][2]) + a[3][1] + a[3][3] + a[4][1] + a[4][3]) / 6;
        int x6 = (3*(a[3][1] + a[4][0]) + 1*(a[3][0] + a[4][1])) / 4;
        int x7 = (3*(a[2][0] + a[2][1]) + a[1][0] + a[1][1] + a[3][0] + a[3][1]) / 6;
        int x8 = (3*(a[0][0] + a[1][1]) + 1*(a[0][1] + a[1][0])) / 4;

        //第一层：找最大点
        int b1 = x1 + x5;
        int b2 = x2 + x6;
        int b3 = x3 + x7;
        int b4 = x4 + x8;

        int b = max(max(b1,b2), max(b3,b4));

        if(b == b1) {
            if(!a[0][2]) CANNY_PUSH(m - mapstep - mapstep);
            if(!a[1][2]) CANNY_PUSH(m - mapstep);
            if(!a[3][2]) CANNY_PUSH(m + mapstep);
            if(!a[4][2]) CANNY_PUSH(m + mapstep + mapstep);

            if(!a[0][1]) CANNY_PUSH(m - mapstep - mapstep - 1);
            if(!a[0][3]) CANNY_PUSH(m - mapstep - mapstep + 1);
            if(!a[1][1]) CANNY_PUSH(m - mapstep - 1);
            if(!a[1][3]) CANNY_PUSH(m - mapstep + 1);
            if(!a[3][1]) CANNY_PUSH(m + mapstep - 1);
            if(!a[3][3]) CANNY_PUSH(m + mapstep + 1);
            if(!a[4][1]) CANNY_PUSH(m + mapstep + mapstep - 1);
            if(!a[4][3]) CANNY_PUSH(m + mapstep + mapstep + 1);

        }else if(b == b2) {
            if(!a[0][4]) CANNY_PUSH(m - mapstep - mapstep + 2);
            if(!a[1][3]) CANNY_PUSH(m - mapstep + 1);
            if(!a[3][1]) CANNY_PUSH(m + mapstep - 1);
            if(!a[4][0]) CANNY_PUSH(m + mapstep + mapstep -2 );

            if(!a[0][3]) CANNY_PUSH(m - mapstep - mapstep + 1);
            if(!a[1][2]) CANNY_PUSH(m - mapstep);
            if(!a[2][1]) CANNY_PUSH(m - 1);
            if(!a[3][0]) CANNY_PUSH(m + mapstep - 2);
            if(!a[1][4]) CANNY_PUSH(m - mapstep + 2);
            if(!a[2][3]) CANNY_PUSH(m + 1);
            if(!a[3][2]) CANNY_PUSH(m + mapstep);
            if(!a[4][1]) CANNY_PUSH(m + mapstep + mapstep - 1);

        }else if(b == b3) {
            if(!a[2][0]) CANNY_PUSH(m - 2);
            if(!a[2][1]) CANNY_PUSH(m - 1);
            if(!a[2][3]) CANNY_PUSH(m + 1);
            if(!a[2][4]) CANNY_PUSH(m + 2);

            if(!a[1][0]) CANNY_PUSH(m - mapstep - 2);
            if(!a[1][1]) CANNY_PUSH(m - mapstep - 1);
            if(!a[1][3]) CANNY_PUSH(m - mapstep + 1);
            if(!a[1][4]) CANNY_PUSH(m - mapstep + 2);
            if(!a[3][0]) CANNY_PUSH(m + mapstep - 2);
            if(!a[3][1]) CANNY_PUSH(m + mapstep - 1);
            if(!a[3][3]) CANNY_PUSH(m + mapstep + 1);
            if(!a[3][4]) CANNY_PUSH(m + mapstep + 2);

        }else if(b == b4) {
            if(!a[0][0]) CANNY_PUSH(m - mapstep - mapstep - 2);
            if(!a[1][1]) CANNY_PUSH(m - mapstep - 1);
            if(!a[3][3]) CANNY_PUSH(m + mapstep + 1);
            if(!a[4][4]) CANNY_PUSH(m + mapstep + mapstep + 2);

            if(!a[0][1]) CANNY_PUSH(m - mapstep - mapstep - 1);
            if(!a[1][2]) CANNY_PUSH(m - mapstep);
            if(!a[2][3]) CANNY_PUSH(m + 1);
            if(!a[3][4]) CANNY_PUSH(m + mapstep + 2);
            if(!a[1][0]) CANNY_PUSH(m - mapstep - 2);
            if(!a[2][1]) CANNY_PUSH(m - 1);
            if(!a[3][2]) CANNY_PUSH(m + mapstep);
            if(!a[4][3]) CANNY_PUSH(m + mapstep + mapstep + 1);
        }


    }

    // the final pass, form the final image
    const uchar* pmap = map + mapstep + 1;
    uchar* pdst = dst.ptr();
    for (int i = 0; i < src.rows; i++, pmap += mapstep, pdst += dst.step) {
        for (int j = 0; j < src.cols; j++) {
            pdst[j] = (uchar)-(pmap[j] >> 1);
        }
    }
}


/* End of file. */
