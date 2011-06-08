#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>

#include "android/log.h"
static const char *TAG="NativeUSBHostEnabler";


JNIEXPORT void JNICALL Java_de_unierlangen_like_usb_FreerunnerUSB_enableHost(JNIEnv *env, jobject thiz){
	__android_log_write(ANDROID_LOG_INFO,  TAG, "Native host enabler started");

	//enable logical USB host mode
	int fd = open("/sys/devices/platform/s3c-ohci/usb_mode", O_RDWR | O_DIRECT | O_SYNC);
	if (fd == -1){
		__android_log_write(ANDROID_LOG_ERROR,  TAG, "Cannot open file");
		/* TODO: throw an exception */
		return;
	}
	fputs("host", fd);
	close(fd);
	//enable 5V
	fd = fopen("/sys/class/i2c-adapter/i2c-0/0-0073/neo1973-pm-host.0/hostmode", "w");
	if (fd == -1){
		__android_log_write(ANDROID_LOG_ERROR,  TAG, "Cannot open file");
		/* TODO: throw an exception */
		return;
	}
	fputs("1", fd);
	close(fd);
}

JNIEXPORT void JNICALL Java_de_unierlangen_like_usb_FreerunnerUSB_disableHost(JNIEnv *env, jobject thiz){
	__android_log_write(ANDROID_LOG_INFO,  TAG, "Native host disabler started");

	//disable logical USB host mode
	int fd = open("/sys/devices/platform/s3c-ohci/usb_mode", O_RDWR | O_DIRECT | O_SYNC);//XXX
	if (fd == -1){
		__android_log_write(ANDROID_LOG_ERROR,  TAG, "Cannot open file");
		/* TODO: throw an exception */
		return;
	}
	fputs("device", fd);
	close(fd);

	//disable 5V at USB!!
	fd = fopen("/sys/class/i2c-adapter/i2c-0/0-0073/neo1973-pm-host.0/hostmode", "w");
	if (fd == -1){
		__android_log_write(ANDROID_LOG_ERROR,  TAG, "Cannot open file");
		/* TODO: throw an exception */
		return;
	}
	fputs("0", fd);
	close(fd);
}
