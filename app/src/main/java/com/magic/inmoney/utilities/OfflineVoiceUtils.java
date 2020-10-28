package com.magic.inmoney.utilities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.magic.inmoney.model.KeyStockModel;
import com.magic.inmoney.orm.LitePalDBase;

public class OfflineVoiceUtils {

    // 语音合成对象
    public static SpeechSynthesizer mTts;
    // 默认本地发音人
    public static String voicerLocal = "xiaoyan";
    Context context;
    private static OfflineVoiceUtils instance = null;
    private static boolean voiceStatus = false;


    // 获取发音人资源路径
    public String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        // 合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        // 发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + OfflineVoiceUtils.voicerLocal + ".jet"));
        return tempBuffer.toString();
    }

    public void setParam() {

        if (mTts != null) {
            mTts.setParameter(SpeechConstant.PARAMS, null);
            // 设置使用本地引擎
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置发音人资源路径
            mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
            // 设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicerLocal);
            // 设置语速
            mTts.setParameter(SpeechConstant.SPEED, "1");
            // 设置音调
            mTts.setParameter(SpeechConstant.PITCH, "30");
            // 设置音量
            mTts.setParameter(SpeechConstant.VOLUME, "100");
            // 设置播放器音频流类型
            mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        }
    }

    public static OfflineVoiceUtils getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineVoiceUtils();
        }
        instance.setContext(context);
        mTts = SpeechSynthesizer.createSynthesizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                System.out.println("----------->" + "InitListener init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(context, "初始化失败,错误码：" + code, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static void promptSpeak(Context context, KeyStockModel model) {

        if (voiceStatus) return;

        OfflineVoiceUtils.getInstance(context).setParam();

        String voiceText = model.getStockName() + "即将接近5日均线，可考虑在目标价" + model.getBuyTargetPrice() + "挂单";

        OfflineVoiceUtils.mTts.startSpeaking(voiceText, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {

            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {

            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {

            }

            @Override
            public void onCompleted(SpeechError speechError) {
                voiceStatus = false;
                LitePalDBase.INSTANCE.updateKeyStockForPrompt(model.getStockCode());
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }

    public static void soldSpeak(Context context, KeyStockModel model, int profitRate) {

        if (voiceStatus) return;

        OfflineVoiceUtils.getInstance(context).setParam();

        System.out.println("----------->ProfitRate : " + profitRate);

        String voiceText = "哦也，" + model.getStockName() + "超过目标盈利了，已经盈利百分之" + profitRate + "以上，赶紧卖，赶紧卖";

        OfflineVoiceUtils.mTts.startSpeaking(voiceText, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {

            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {

            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {

            }

            @Override
            public void onCompleted(SpeechError speechError) {
                voiceStatus = false;
                LitePalDBase.INSTANCE.updateKeyStockForPrompt(model.getStockCode());
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }
}
