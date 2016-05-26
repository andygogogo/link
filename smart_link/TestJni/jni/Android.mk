LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := SlaveCrc8
LOCAL_SRC_FILES := SDK.cpp
LOCAL_LDLIBS += -llog 

include $(BUILD_SHARED_LIBRARY)
#C:\Users\Andy\Desktop\work\code\smart_link\TestJni