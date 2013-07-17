LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)

include includeOpenCV.mk
#ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
	#try to load OpenCV.mk from default install location
	#include $(TOOLCHAIN_PREBUILT_ROOT)/user/share/OpenCV/OpenCV.mk
#else
	include ~/workspace/Android+OpenCV/OpenCV-2.3.1/share/OpenCV/OpenCV.mk#$(OPENCV_MK_PATH)
#endif

LOCAL_MODULE    := opencv



LOCAL_SRC_FILES := cvjni.cpp
LOCAL_LDLIBS +=  -llog -ldl

#LOCAL_STATIC_LIBRARIES := cxcore cv

include $(BUILD_SHARED_LIBRARY)
