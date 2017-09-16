//
// Created by yourgod on 2017/8/26.
//
#include "HeaderFiles/com_example_utils_OpenCVCanny.h"
#include <stdio.h>
#include <stdlib.h>
extern "C" {
JNIEXPORT jintArray JNICALL JNICALL Java_com_example_utils_PictureTransformUtil_ImgToGrey(
        JNIEnv* env, jobject obj, jintArray buf, int w, int h);
}
;
JNIEXPORT jintArray JNICALL JNICALL Java_com_example_utils_PictureTransformUtil_ImgToGrey(
        JNIEnv* env, jobject obj, jintArray buf, int w, int h) {
    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, false);
    if (cbuf == NULL) {
        return 0; /* exception occurred */
    }
    int alpha = 0xFF << 24;
    for (int i = 0; i < h; i++) {
        for (int j = 0; j < w; j++) {
            // 获得像素的颜色
            int color = cbuf[w * i + j];
            int red = ((color & 0x00FF0000) >> 16);
            int green = ((color & 0x0000FF00) >> 8);
            int blue = color & 0x000000FF;
            color = (red + green + blue) / 3;
            color = alpha | (color << 16) | (color << 8) | color;
            cbuf[w * i + j] = color;
        }
    }
    int size=w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, cbuf);
    env->ReleaseIntArrayElements(buf, cbuf, 0);
    return result;
}
