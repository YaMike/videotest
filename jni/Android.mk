LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := com_videotest_VideoItem
LOCAL_SRC_FILES := com_videotest_VideoItem.c
LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil

include $(BUILD_SHARED_LIBRARY)
$(call import-module,ffmpeg-2.1.2/android/arm)
