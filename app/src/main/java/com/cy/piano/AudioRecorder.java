package com.cy.piano;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 44100;
    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int mBufferSizeInBytes = 0;
    //录音对象
    private AudioRecord mAudioRecord;
    //录音状态
    private volatile Status mStatus = Status.STATUS_NO_READY;
    //线程池
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();

    //录音监听
    private RecordStreamListener mRecordStreamListener;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private Context mContext;

    public AudioRecorder(Context context) {
        mContext = context;
    }

    /**
     * 创建默认的录音对象
     */
    public void createDefaultAudio() {
        createAudio(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
    }

    /**
     * 创建录音对象
     *
     * @param audioSource
     * @param sampleRateInHz
     * @param channelConfig
     * @param audioFormat
     */
    public void createAudio(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获得缓冲区字节大小
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mBufferSizeInBytes);
        int state = mAudioRecord.getState();
        Log.i(TAG, "createAudio state:" + state + ", initialized:" + (state == AudioRecord.STATE_INITIALIZED));
        mStatus = Status.STATUS_READY;
    }

    /**
     * 开始录音
     */
    public void startRecord() {
        if (mStatus == Status.STATUS_NO_READY || mAudioRecord == null) {
            createDefaultAudio();
        }
        if (mStatus == Status.STATUS_START) {
            return;
        }
        Log.d(TAG, "===startRecord===");
        mAudioRecord.startRecording();

        //将录音状态设置成正在录音状态
        mStatus = Status.STATUS_START;

        //使用线程池管理线程
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                writeDataToFile();
            }
        });
    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        Log.d(TAG, "===pauseRecord===");
        if (mStatus != Status.STATUS_START) {
            return;
        } else {
            mAudioRecord.stop();
            mStatus = Status.STATUS_PAUSE;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        Log.d(TAG, "===stopRecord===");
        if (mStatus == Status.STATUS_NO_READY || mStatus == Status.STATUS_READY) {
            return;
        } else {
            mAudioRecord.stop();
            mStatus = Status.STATUS_STOP;
            if (mAudioRecord != null) {
                mAudioRecord.release();
                mAudioRecord = null;
            }
            mStatus = Status.STATUS_NO_READY;
        }
    }

    /**
     * 取消录音
     */
    public void canel() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }

        mStatus = Status.STATUS_NO_READY;
    }


    /**
     * 将音频信息写入文件
     */
    private void writeDataToFile() {
        short[] audioData = new short[mBufferSizeInBytes];
        int readSize;
        while (mStatus == Status.STATUS_START) {
            readSize = mAudioRecord.read(audioData, 0, mBufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                if (mRecordStreamListener != null) {
                    mRecordStreamListener.onRecording(audioData, 0, audioData.length);
                }
            }
        }
        if (mRecordStreamListener != null) {
            mRecordStreamListener.finishRecord();
        }
    }


    public void setRecordStreamListener(RecordStreamListener recordStreamListener) {
        this.mRecordStreamListener = recordStreamListener;
    }

    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }

    public interface RecordStreamListener {
        /**
         * 录音过程中
         *
         * @param bytes
         * @param offset
         * @param length
         */
        void onRecording(short[] bytes, int offset, int length);

        /**
         * 录音完成
         */
        void finishRecord();
    }
}

