#include <com_videotest_VideoItem.h>

#include <android/log.h>
#include <android/bitmap.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avio.h>
#include <libavformat/avformat.h>
#include <libavutil/imgutils.h>
#include <libavutil/samplefmt.h>
#include <libavutil/timestamp.h>
#include <libswscale/swscale.h>

#include <string.h>
#include <stdint.h>

#define  LOG_TAG    "VideoTest"
#define  LOG_I(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOG_E(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *dummy) {
	return JNI_VERSION_1_6;
}

static int open_codec_context(int *stream_idx, const char *video_link_str,
		AVFormatContext *fmt_ctx, enum AVMediaType type)
{
	int ret;
	AVStream *st;
	AVCodecContext *dec_ctx = NULL;
	AVCodec *dec = NULL;
	ret = av_find_best_stream(fmt_ctx, type, -1, -1, NULL, 0);
	if (ret < 0) {
		LOG_E("Could not find %s stream in input file '%s'\n",
				av_get_media_type_string(type), video_link_str);
		return ret;
	} else {
		*stream_idx = ret;
		st = fmt_ctx->streams[*stream_idx];
		/* find decoder for the stream */
		dec_ctx = st->codec;
		dec = avcodec_find_decoder(dec_ctx->codec_id);
		if (!dec) {
			LOG_E("Failed to find %s codec\n",
					av_get_media_type_string(type));
			return ret;
		}
		if ((ret = avcodec_open2(dec_ctx, dec, NULL)) < 0) {
			LOG_E("Failed to open %s codec\n",
					av_get_media_type_string(type));
			return ret;
		}
	}
	return 0;
}

static void copy_frame_to_pixels(AVFrame *p_frame, void *pixels, AndroidBitmapInfo *info) {
    uint8_t *p_frame_line;

    for (int line = 0; line < info->height; line++) {
        uint8_t*  pxs = (uint8_t*)pixels;
        p_frame_line = (uint8_t *)p_frame->data[0] + (line*p_frame->linesize[0]);

        for (int offset = 0; offset < info->width; offset++) {
            int out_offset = offset * 4;
            int in_offset = offset * 3;

            pxs[out_offset]   = p_frame_line[in_offset];
            pxs[out_offset+1] = p_frame_line[in_offset+1];
            pxs[out_offset+2] = p_frame_line[in_offset+2];
            pxs[out_offset+3] = 0;
        }
        pixels = (char*)pixels + info->stride;
    }
}

#define ERR_FMT "(error: %s)\n"

static void println_av_error(char *msg, int err) {
	char buff[100] = {0};
	av_strerror(err, buff, sizeof(buff)/sizeof(buff[0]));
	unsigned fmt_size = strlen(msg)+strlen(ERR_FMT)+1;
	char fmt[fmt_size];
	memset(fmt, 0, fmt_size);
	strncpy(fmt, msg, fmt_size);
	strncat(fmt, ERR_FMT, fmt_size - strlen(fmt) - strlen(ERR_FMT) - 1);
	LOG_E("%s",fmt);
}

#undef ERR_FMT

JNIEXPORT jint JNICALL Java_com_videotest_VideoItem_getLinkPreview
(JNIEnv *env, jobject obj, jstring jstr, jint width, jint height, jobject jbitmap) {

	AVFormatContext *p_format_ctx = NULL;
	const char *video_link_str = (*env)->GetStringUTFChars(env, jstr, NULL);
	int err = 0;

	av_register_all();

	if (0 > (err = avformat_open_input(&p_format_ctx, video_link_str, NULL, NULL))) {
		println_av_error("Error at opening input video file!", err);
		goto exit_level_1;
	}

	if (0 > (err = avformat_find_stream_info(p_format_ctx, NULL))) {
		println_av_error("Error at retrieving stream info!", err);
		goto exit_level_2;
	}

	int video_stream_idx = -1;
	if (0 > (err = open_codec_context(&video_stream_idx, video_link_str, p_format_ctx, AVMEDIA_TYPE_VIDEO))) {
		println_av_error("Error at opening codec context!", err);
		goto exit_level_2;
	}

	AVStream *p_video_stream = p_format_ctx->streams[video_stream_idx];
	AVCodecContext *p_video_dec_ctx = p_video_stream->codec;

	uint8_t *video_dst_data[4] = {NULL};
	int video_dst_linesize[4];

	if (0 > (err = av_image_alloc(video_dst_data, video_dst_linesize,
			p_video_dec_ctx->width, p_video_dec_ctx->height, p_video_dec_ctx->pix_fmt, 1))) {
		println_av_error("Error at allocating output image!", err);
		goto exit_level_2;
	}
	int video_dst_bufsize = err;

	av_dump_format(p_format_ctx, 0, video_link_str, 0);

	AVFrame *p_frame = NULL, *p_transformed_frame = NULL;
	if (NULL == (p_frame = avcodec_alloc_frame())) {
		LOG_E("Cannot allocate av frame!");
		goto exit_level_3;
	}

	if (NULL == (p_transformed_frame = avcodec_alloc_frame())) {
		LOG_E("Cannot allocate second av frame!");
		goto exit_level_4;
	}

	AVPacket pkt;
	av_init_packet(&pkt);
	pkt.data = NULL;
	pkt.size = 0;

	int frame_number = 0;
	while (0 < av_read_frame(p_format_ctx, &pkt)) {
		if (pkt.stream_index == video_stream_idx) {
			int got_picture = 0;
			if (0 > (err = avcodec_decode_video2(p_video_dec_ctx, p_frame,  &got_picture, &pkt))) {
				println_av_error("Cannot decode frame!", err);
				break;
			}
			if (got_picture) {
				struct SwsContext *p_sws_context = sws_getContext(
						p_video_dec_ctx->width,	p_video_dec_ctx->height,
						p_video_dec_ctx->pix_fmt,
						width, height, PIX_FMT_RGB,
						SWS_BICUBIC, NULL, NULL, NULL);
				if (p_sws_context == NULL) {
					println_av_error("Cannot allocate context for image transformation!", 0);
					break;
				}
				if (0 >= sws_scale(p_sws_context, (const uint8_t * const *) p_frame->data, p_frame->linesize, 0,
						p_video_dec_ctx->height, p_transformed_frame->data, p_transformed_frame->linesize)) {
					sws_freeContext(p_sws_context);
					break;
				}

				AndroidBitmapInfo info;
				void *pixels = NULL;
				AndroidBitmap_getInfo(env, jbitmap, &info);
				AndroidBitmap_lockPixels(env, jbitmap, pixels);
				copy_frame_to_pixels(p_transformed_frame, pixels, &info);
				AndroidBitmap_unlockPixels(env, jbitmap);
				LOG_I("Picture created (frame %d)", frame_number);
				sws_freeContext(p_sws_context);
				if (++frame_number > 10) {
					break;
				}
			}
		}
		av_free_packet(&pkt);
	}

	LOG_I("Finished normally.");
	err = 0;
exit_level_5:
	avcodec_free_frame(&p_transformed_frame);

exit_level_4:
	avcodec_free_frame(&p_frame);

exit_level_3:
	av_freep(&video_dst_data[0]);

exit_level_2:
	avformat_close_input(&p_format_ctx);

exit_level_1:
	(*env)->ReleaseStringUTFChars(env, jstr, video_link_str);

	return 0;
}
