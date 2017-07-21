//
// Created by lijialin on 2017/4/6.
//

//#include <opencv2/core/types.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>

using namespace cv;
using namespace std;

#define halfPi ((float)(CV_PI*0.5))
#define Pi     ((float)CV_PI)
#define a0  0 /*-4.172325e-7f*/   /*(-(float)0x7)/((float)0x1000000); */
#define a1 1.000025f        /*((float)0x1922253)/((float)0x1000000)*2/Pi; */
#define a2 -2.652905e-4f    /*(-(float)0x2ae6)/((float)0x1000000)*4/(Pi*Pi); */
#define a3 -0.165624f       /*(-(float)0xa45511)/((float)0x1000000)*8/(Pi*Pi*Pi); */
#define a4 -1.964532e-3f    /*(-(float)0x30fd3)/((float)0x1000000)*16/(Pi*Pi*Pi*Pi); */
#define a5 1.02575e-2f      /*((float)0x191cac)/((float)0x1000000)*32/(Pi*Pi*Pi*Pi*Pi); */
#define a6 -9.580378e-4f    /*(-(float)0x3af27)/((float)0x1000000)*64/(Pi*Pi*Pi*Pi*Pi*Pi); */

#define _sin(x) ((((((a6*(x) + a5)*(x) + a4)*(x) + a3)*(x) + a2)*(x) + a1)*(x) + a0)
#define _cos(x) _sin(halfPi - (x))

/****************************************************************************************\
*                              Probabilistic Hough Transform                             *
\****************************************************************************************/

static void
icvHoughLinesProbabilistic( CvMat* image,
                            float rho, float theta, int threshold,
                            int lineLength, int lineGap,
                            CvSeq *lines, int linesMax ) {

    //rho:单位像素精度，常取1，因此irho常为1
    //theta 单位弧度
    //累加平面可以看做由rho像素和theta弧度组成的二维直方图
    //linesMax表示支持所返回的直线的点的数量

    Mat accum, mask;       // accum 计数用数组 mask为源码矩阵
    vector<float> trigtab; //用于存储事先计算好的正弦和余弦
    MemStorage storage(cvCreateMemStorage(0)); //开辟一段内存空间

    CvSeq* seq;            //用于存储边缘像素
    CvSeqWriter writer;
    int width, height;
    int numangle, numrho;  //rho的离散数量，theta的离散数量（theta轴上最大值）
    float ang;
    int r, n, count;
    CvPoint pt;
    float irho = 1 / rho;  //rho:单位像素精度，常取1，因此irho常为1
    CvRNG rng = cvRNG(-1); //随机数
    const float* ttab;     //向量trigtab的地址指针
    uchar* mdata0;         //矩阵mask的地址指针

    CV_Assert( CV_IS_MAT(image) && CV_MAT_TYPE(image->type) == CV_8UC1 );

    width = image->cols;   //提取输入图像的列数（宽度）
    height = image->rows;  //提取输入图像的行数（高度）

    //根据精度计算出角度的最大值
    numangle = cvRound(CV_PI / theta);
    //根据精度计算出r的最大值
    numrho = cvRound(((width + height) * 2 + 1) / rho); //为什么这么算?

    accum.create( numangle, numrho, CV_32SC1 ); // 创建累加器矩阵，即Hough空间
    mask.create( height, width, CV_8UC1 );      //创建掩码矩阵

    //定义trigtab的大小，因为要存储sin和cos，所以长度为角度离散数的2倍
    trigtab.resize(numangle*2);
    accum = cv::Scalar(0); //累加器矩阵清零

    //为避免重复计算，事先计算好所需的所有正弦和余弦值
    for( ang = 0, n = 0; n < numangle; ang += theta, n++ ) {
        trigtab[n*2] = (float)(cos(ang) * irho);
        trigtab[n*2+1] = (float)(sin(ang) * irho);
    }
    ttab = &trigtab[0]; //赋值首地址
    mdata0 = mask.data;

    //开始写入序列，开启写状态
    cvStartWriteSeq( CV_32SC2, sizeof(CvSeq), sizeof(CvPoint), storage, &writer );

    // stage 1. collect non-zero image points
    // 收集图像中的所有非零点，因为输入图像是边缘图像，所以非零点就是边缘点
    for( pt.y = 0, count = 0; pt.y < height; pt.y++ ) {
        //提取输入图像和掩码矩阵的每行地址指针
        //CvMat内部有一个共用体,叫data，data里有一个成员ptr，表示地址
        //CvMat里有一个成员step,表示行数据长度（字节）
        const uchar* data = image->data.ptr + pt.y * image->step;
        uchar* mdata = mdata0 + pt.y * width;

        for( pt.x = 0; pt.x < width; pt.x++ ) {
            if( data[pt.x] ){ //是边缘点
                mdata[pt.x] = (uchar)1; //掩码矩阵相应位置设置为1
                CV_WRITE_SEQ_ELEM( pt, writer ); //把坐标写入序列
            }
            else //不是边缘点
                mdata[pt.x] = 0; //掩码矩阵相应位置设置为0
        }
    }

    //写操作只有在执行cvEndWriteSeq函数后，才真正写到序列中，之前都是在缓冲中
    seq = cvEndWriteSeq( &writer ); //终止写序列
    count = seq->total; //得到边缘点的数量

    // stage 2. process all the points in random order
    // 随机处理所有的边缘点
    for( ; count > 0; count-- ) {
        // choose random point out of the remaining ones
        // 步骤1.在剩下的边缘点中随机选一个点,idx为不大于count的随机数
        int idx = cvRandInt(&rng) % count;
        int max_val = threshold-1, max_n = 0; //max_val为累加器的最大值,max_n为最大值所对应的角度
        //cvGetSeqElem 方法可以随机访问序列
        CvPoint* point = (CvPoint*)cvGetSeqElem( seq, idx ); //由随机数idx在序列中提取出来的坐标点
//        CvPoint line_end[2] = {{0,0}, {0,0}}; //定义直线的两个端点
        CvPoint line_end[2] = {CvPoint(0,0), CvPoint(0,0)}; //定义直线的两个端点
        float a, b;
        int* adata = (int*)accum.data; //累加器的地址，也就是Hough空间的地址指针
        int i, j, k, x0, y0, dx0, dy0, xflag;
        int good_line;
        const int shift = 16;

        i = point->y; //提出坐标点的横纵坐标
        j = point->x;

        // "remove" it by overriding it with the last element
        // 用序列中的最后一个元素覆盖掉刚才提出的随机坐标点
        *point = *(CvPoint*)cvGetSeqElem( seq, count-1 );

        // check if it has been excluded already (i.e. belongs to some other line)
        // 检验这个坐标点是否已经计算过，也就是它已经属于其他直线
        // 计算过的坐标点，会在掩码矩阵中的对应位置清零
        if( !mdata0[i*width + j] ) //该坐标点已经被处理过
            continue;

        // update accumulator, find the most probable line
        // 步骤2.更新累加器矩阵，找到最有可能的直线
        for( n = 0; n < numangle; n++, adata += numrho ) {
            // 由角度计算距离
            // ρ = xcosθ + ysinθ
            r = cvRound( j * ttab[n*2] + i * ttab[n*2+1] );
            r += (numrho - 1) / 2;
            int val = ++adata[r]; //在累加器矩阵的相应位置上加1，并赋值给val
            if( max_val < val ) { //更新最大值，并得到它的角度
                max_val = val;
                max_n = n;
            }
        }

        // if it is too "weak" candidate, continue with another point
        // 步骤3. 如果上面步骤得到的最大值小于阈值，则放弃该点，继续计算
        if( max_val < threshold )
            continue;

        // from the current point walk in each direction
        // along the found line and extract the line segment
        // 步骤4. 从当前点出发，沿着它所在直线的方向前进，直到到达端点为止
        a = -ttab[max_n*2+1]; // a = -sinθ
        b = ttab[max_n*2]; // b = cosθ
        x0 = j; //当前点的横纵坐标值
        y0 = i;

        //确定当前点所在直线的角度是在45°-135°之间，还是在0°-45°或135°-180°之间
        if( fabs(a) > fabs(b) ) {//在45°-135°之间
            xflag = 1; //置标志位，标识直线的粗略方向
            dx0 = a > 0 ? 1 : -1; //确定横纵坐标的位移量
            dy0 = cvRound( b*(1 << shift)/fabs(a) );
            y0 = (y0 << shift) + (1 << (shift-1)); //确定纵坐标
        }else {//在0°-45°或135°-180°之间
            xflag = 0;
            dy0 = b > 0 ? 1 : -1; //确定横纵坐标的位移量
            dx0 = cvRound( a*(1 << shift)/fabs(b) );
            x0 = (x0 << shift) + (1 << (shift-1)); //确定横坐标
        }

        //搜索直线的两个端点
        for( k = 0; k < 2; k++ ) {
            //gap表示两条直线的间隙, x和y为搜索位置，dx和dy为位移量
            int gap = 0, x = x0, y = y0, dx = dx0, dy = dy0;

            if( k > 0 ) //搜索第二个端点的时候，反方向位移
                dx = -dx, dy = -dy;

            // walk along the line using fixed-point arithmetics,
            // stop at the image border or in case of too big gap
            //沿着直线的方向位移，直到到达图像的边界或大的间隙为止
            for( ;; x += dx, y += dy ) {
                uchar* mdata;
                int i1, j1;

                if( xflag ) {//确定新的位移后的坐标位置
                    j1 = x;
                    i1 = y >> shift;
                }
                else {
                    j1 = x >> shift;
                    i1 = y;
                }

                //如果到达了图像的边界，停止位移，退出循环
                if( j1 < 0 || j1 >= width || i1 < 0 || i1 >= height )
                    break;

                mdata = mdata0 + i1*width + j1;//定位位移后掩码矩阵位置

                // for each non-zero point:
                //    update line end,
                //    clear the mask element
                //    reset the gap
                if( *mdata ) { //该掩码不为0，说明该点可能是在直线上
                    gap = 0; //设置间隙为0
                    line_end[k].y = i1;  //更新直线的端点位置
                    line_end[k].x = j1;
                }
                    //掩码为0，说明不是直线，但仍继续位移，直到间隙大于所设置的阈值为止
                else if( ++gap > lineGap )  //间隙加1
                    break;
            }
        }

        //步骤5，由检测到的直线的两个端点粗略计算直线的长度
        //当直线长度大于所设置的阈值时，good_line为1，否则为0

        good_line = abs(line_end[1].x - line_end[0].x) >= lineLength ||
                    abs(line_end[1].y - line_end[0].y) >= lineLength;

        //再次搜索端点，目的是更新累加器矩阵和更新掩码矩阵，以备下一次循环使用
        for( k = 0; k < 2; k++ ) {

            int x = x0, y = y0, dx = dx0, dy = dy0;

            if( k > 0 )
                dx = -dx, dy = -dy;

            // walk along the line using fixed-point arithmetics,
            // stop at the image border or in case of too big gap
            for( ;; x += dx, y += dy ) {
                uchar* mdata;
                int i1, j1;

                if( xflag ) {
                    j1 = x;
                    i1 = y >> shift;
                }
                else {
                    j1 = x >> shift;
                    i1 = y;
                }

                mdata = mdata0 + i1*width + j1;

                // for each non-zero point:
                //    update line end,
                //    clear the mask element
                //    reset the gap
                if( *mdata ) {
                    //if语句的作用是清除那些已经判定是好的直线上的点对应的累加器的值，避免再次利用这些累加值
                    if( good_line ) {
                        adata = (int*)accum.data; //得到累加器矩阵地址指针
                        for( n = 0; n < numangle; n++, adata += numrho ) {
                            r = cvRound( j1 * ttab[n*2] + i1 * ttab[n*2+1] );
                            r += (numrho - 1) / 2;
                            adata[r]--; //相应的累加器减1
                        }
                    }
                    //搜索过的位置，不管是好的直线，还是坏的直线，掩码相应位置都清0，
                    //这样下次就不会再重复搜索这些位置了，从而达到减小计算边缘点的目的
                    *mdata = 0;
                }
                //如果已经到达了直线的端点，则退出循环
                if( i1 == line_end[k].y && j1 == line_end[k].x )
                    break;
            }
        }

        if( good_line ) {//如果是好的直线
//            CvRect lr = { line_end[0].x, line_end[0].y, line_end[1].x, line_end[1].y };
            CvRect lr = CvRect(line_end[0].x, line_end[0].y, line_end[1].x, line_end[1].y);
            cvSeqPush( lines, &lr ); //把两个端点压入序列中
            if( lines->total >= linesMax )//如果检测到的直线数量大于阈值，则退出该函数
                return;
        }
    }
}

/* Wrapper function for standard hough transform */
CV_IMPL CvSeq* cvMyHoughLines2( CvArr* src_image, void* lineStorage, int method,
               double rho, double theta, int threshold,
               double param1, double param2 ) {
    CvSeq* result = 0;

    CvMat stub, *img = (CvMat*)src_image; //源图像
    CvMat* mat = 0;
    CvSeq* lines = 0;
    CvSeq lines_header;
    CvSeqBlock lines_block;
    int lineType, elemSize;
    int linesMax = INT_MAX; //输出最多直线的数量，设为无穷多
    int iparam1, iparam2;

    img = cvGetMat( img, &stub );

    if(!CV_IS_MASK_ARR(img)) //确保输入图像是8位单通道
        CV_Error( CV_StsBadArg, "The source image must be 8-bit, single-channel" );

    if(!lineStorage)
        CV_Error( CV_StsNullPtr, "NULL destination" );

    if(rho <= 0 || theta <= 0 || threshold <= 0)
        CV_Error( CV_StsOutOfRange, "rho, theta and threshold must be positive" );

    lineType = CV_32SC4;
    elemSize = sizeof(int)*4;


    if( CV_IS_STORAGE( lineStorage )) {
        lines = cvCreateSeq( lineType, sizeof(CvSeq), elemSize, (CvMemStorage*)lineStorage );
    } else if( CV_IS_MAT( lineStorage )) {
        mat = (CvMat*)lineStorage;

        if( !CV_IS_MAT_CONT( mat->type ) || (mat->rows != 1 && mat->cols != 1) )
            CV_Error( CV_StsBadArg,
            "The destination matrix should be continuous and have a single row or a single column" );
        if( CV_MAT_TYPE( mat->type ) != lineType )
            CV_Error( CV_StsBadArg,
            "The destination matrix data type is inappropriate, see the manual" );

        lines = cvMakeSeqHeaderForArray( lineType, sizeof(CvSeq), elemSize, mat->data.ptr,
                                     mat->rows + mat->cols - 1, &lines_header, &lines_block );
        linesMax = lines->total;
        cvClearSeq( lines );
    }else
        CV_Error( CV_StsBadArg, "Destination is not CvMemStorage* nor CvMat*" );

        iparam1 = cvRound(param1);
        iparam2 = cvRound(param2);

        icvHoughLinesProbabilistic( img, (float)rho, (float)theta,
            threshold, iparam1, iparam2, lines, linesMax );

        if(mat){
            if(mat->cols > mat->rows)
                mat->cols = lines->total;
            else
                mat->rows = lines->total;
        }
        else
            result = lines;

    return result;
}


namespace cv
{

    const int STORAGE_SIZE = 1 << 12;

    static void seqToMat(const CvSeq* seq, OutputArray _arr)
    {
        if( seq && seq->total > 0 )
        {
            _arr.create(1, seq->total, seq->flags, -1, true);
            Mat arr = _arr.getMat();
            cvCvtSeqToArray(seq, arr.data);
        }
        else
            _arr.release();
    }

}

/*
 概率Hough变换，不直接扫描所有的点，而是随机选择一些点，
 当确定一条直线后，将在直线上的未扫描点直接从扫描列表中
 去掉
*/
void myHoughLinesP( InputArray _image, OutputArray _lines,
                    double rho, double theta, int threshold,
                    double minLineLength, double maxGap )
{
    Ptr<CvMemStorage> storage = cvCreateMemStorage(STORAGE_SIZE);//创建内存存储器
    Mat image = _image.getMat();
    CvMat c_image = image;
    CvSeq* seq = cvMyHoughLines2( &c_image, storage, CV_HOUGH_PROBABILISTIC,
                                rho, theta, threshold, minLineLength, maxGap );
    seqToMat(seq, _lines); //序列转化为Mat
}


/* End of file. */
