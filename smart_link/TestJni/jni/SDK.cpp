#include <jni.h>
#include "com_smartlink_SDK.h"
#include <android/log.h>
#define LOG_TAG "clog"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)


JNIEXPORT jint JNICALL Java_com_smartlink_SDK_SlaveCrc8(JNIEnv *env, jclass cla,
		jbyteArray pinbuf, jint len, jbyteArray outbuf) {

	char *pBuf = (char *) env->GetByteArrayElements(pinbuf, 0);
	int i;
	char crc = 0;
	while (len--) {
		crc = (crc ^ (*pBuf)) & 0x00ff;
		LOGD("pBuf================%@",*pBuf);
		for (i = 8; i > 0; i--) {
			if (crc & 0x0080) {
				crc = ((crc << 1) ^ 0x0031) & 0x00ff;
			} else {
				crc = (crc << 1) & 0x00ff;
			}
		}
		pBuf++;
	}
	env->ReleaseByteArrayElements(pinbuf, (jbyte *) pBuf, 0);
	env->SetByteArrayRegion(outbuf, 0, 1, (jbyte *) &crc);
	LOGD("校验后的CRC================%d",crc);
	LOGI("校验后的CRC================%d",crc);
	return crc;
}
