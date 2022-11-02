// Copyright (C) 2022 Antonio Fermiano
//
// This file is part of ecmgui.
//
// ecmgui is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ecmgui is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ecmgui.  If not, see <http://www.gnu.org/licenses/>.

#include "com_afermiano_ecmgui_control_bridge_NativeBridge.h"

#include "ecm.h"

#define MAX_STEP_IN_BYTES (5 * 1024 * 1024)

static Progress g_progress;
static jmethodID g_set_failure_method;
static jmethodID g_set_encoding_complete_method;
static jmethodID g_set_decoding_complete_method;
static jmethodID g_set_percentage_method;

static int g_last_analyse_percentage;
static int g_last_encoding_or_decoding_percentage;

static int cache_java_methods(JNIEnv *env, jobject obj){
    const jclass java_class = (*env)->GetObjectClass(env, obj);

    g_set_failure_method = (*env)->GetMethodID(env, java_class, "setFailure", "(I)V");
    g_set_encoding_complete_method = (*env)->GetMethodID(env, java_class, "setEncodingComplete", "(JJJJJJ)V");
    g_set_decoding_complete_method = (*env)->GetMethodID(env, java_class, "setDecodingComplete", "(JJ)V");
    g_set_percentage_method = (*env)->GetMethodID(env, java_class, "setPercentage", "(II)V");

    if(!g_set_failure_method || !g_set_encoding_complete_method || !g_set_decoding_complete_method || !g_set_percentage_method){
        return 1;
    }

    return 0;
}

JNIEXPORT void JNICALL Java_com_afermiano_ecmgui_control_bridge_NativeBridge_encode
(JNIEnv *env, jobject obj){
    encode(&g_progress);

    if(g_progress.state == FAILURE){
        (*env)->CallVoidMethod(env, obj, g_set_failure_method, g_progress.failure_reason);
        return;
    }

    if(g_progress.state == COMPLETED){
        (*env)->CallVoidMethod(env, obj, g_set_encoding_complete_method,
                               g_progress.literal_bytes,
                               g_progress.mode_1_sectors,
                               g_progress.mode_2_form_1_sectors,
                               g_progress.mode_2_form_2_sectors,
                               g_progress.bytes_before_processing,
                               g_progress.bytes_after_processing);
        return;
    }

    if(g_progress.analyze_percentage != g_last_analyse_percentage || g_progress.encoding_or_decoding_percentage != g_last_encoding_or_decoding_percentage){
        (*env)->CallVoidMethod(env, obj, g_set_percentage_method, g_progress.analyze_percentage, g_progress.encoding_or_decoding_percentage);

        g_last_analyse_percentage = g_progress.analyze_percentage;
        g_last_encoding_or_decoding_percentage = g_progress.encoding_or_decoding_percentage;
    }
}

JNIEXPORT void JNICALL Java_com_afermiano_ecmgui_control_bridge_NativeBridge_decode
(JNIEnv *env, jobject obj){
    decode(&g_progress);

    if(g_progress.state == FAILURE){
        (*env)->CallVoidMethod(env, obj, g_set_failure_method, g_progress.failure_reason);
        return;
    }

    if(g_progress.state == COMPLETED){
        (*env)->CallVoidMethod(env, obj, g_set_decoding_complete_method,
                               g_progress.bytes_before_processing,
                               g_progress.bytes_after_processing);
    }

    if(g_progress.encoding_or_decoding_percentage != g_last_encoding_or_decoding_percentage){
        (*env)->CallVoidMethod(env, obj, g_set_percentage_method, g_progress.analyze_percentage, g_progress.encoding_or_decoding_percentage);

        g_last_encoding_or_decoding_percentage = g_progress.encoding_or_decoding_percentage;
    }
}

static jint generic_prepare(JNIEnv *env, jobject obj, jstring input_file_name, jstring output_file_name, FailureReason (*prepare_function)(char *, char *, int, Progress *)){
    if(cache_java_methods(env, obj)){
        return OUT_OF_MEMORY;
    }

    g_last_analyse_percentage = -1;
    g_last_encoding_or_decoding_percentage = -1;

    const char *input_file_chars = (*env)->GetStringUTFChars(env, input_file_name, NULL);
    const char *output_file_chars = (*env)->GetStringUTFChars(env, output_file_name, NULL);

    if(!input_file_chars || !output_file_chars){
        return OUT_OF_MEMORY;
    }

    const FailureReason ret = (*prepare_function)((char *)input_file_chars, (char *)output_file_chars, MAX_STEP_IN_BYTES, &g_progress);

    (*env)->ReleaseStringUTFChars(env, input_file_name, input_file_chars);
    (*env)->ReleaseStringUTFChars(env, output_file_name, output_file_chars);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_afermiano_ecmgui_control_bridge_NativeBridge_nativePrepareEncoding
(JNIEnv *env, jobject obj, jstring input_file_name, jstring output_file_name){

   return generic_prepare(env, obj, input_file_name, output_file_name, prepare_encoding);
}

JNIEXPORT jint JNICALL Java_com_afermiano_ecmgui_control_bridge_NativeBridge_nativePrepareDecoding
(JNIEnv *env, jobject obj, jstring input_file_name, jstring output_file_name){
    return generic_prepare(env, obj, input_file_name, output_file_name, prepare_decoding);
}


