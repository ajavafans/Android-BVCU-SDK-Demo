package com.example.myapplication1;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.smarteye.sdk.BVCU;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderUtils {
    private static final String TAG = "RecorderUtils";
    private static final String RECORDER_DIA_NAME = "Recorder";
    private static final String RECORDER_FILE_PATH = Environment.getExternalStorageDirectory() + "/" +RECORDER_DIA_NAME;
    // 音频获取源
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 8000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private boolean isRecording = false;

    private AudioRecord audioRecord;

    private String recorderFolderPath;

    public static int bufferSize = 0;
    public RecorderUtils(){
        this(RECORDER_FILE_PATH);
    }

    public RecorderUtils(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            file.mkdirs();
        }
        recorderFolderPath = filePath;
    }

    public boolean isRecording(){
        return isRecording;
    }

    public void startRecorder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                bufferSize = 0;
                bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
                audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                        channelConfig, audioFormat, bufferSize);
                byte [] tempBufferSize = new byte[bufferSize];
                /*
                FileOutputStream outputStream = null;
                String fileName = recorderFolderPath+"/"+getCurrentTime();
                File file = new File(fileName+".pcm");
                File outFile = new File(fileName + ".wav");

                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                */
                audioRecord.startRecording();
                isRecording = true;
                Log.d(TAG,"isRecording:"+isRecording);
                while(isRecording){
                    audioRecord.read(tempBufferSize,0,bufferSize);
                    BVCU.getData().inputAudioData(tempBufferSize,bufferSize,System.currentTimeMillis() * 1000);
                    /*
                    try {
                        outputStream.write(tempBufferSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */
                }
                /*
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pcmToWave(file.getAbsolutePath(), outFile.getAbsolutePath());
                */
            }
        }).start();

    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        return sdf.format(date);
    }


    public void stopRecorder(){
        isRecording = false;
        if(audioRecord != null){
            if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                audioRecord.stop();
            }
            audioRecord.release();
        }
    }

    public void pcmToWave(String inFileName, String outFileName){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudiolen = 0;
        long longSamplRate = 11025;
        long totalDataLen = totalAudiolen+36;//由于不包括RIFF和WAV
        int channels = 2;
        long byteRate = 16*longSamplRate*channels/8;
        byte[] data = new byte[1024];
        try {
            in = new FileInputStream(inFileName);
            out = new FileOutputStream(outFileName);
            totalAudiolen = in.getChannel().size();
            totalDataLen = totalAudiolen+36;
            writeWaveFileHeader(out, totalAudiolen, totalDataLen, longSamplRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                    int channels, long byteRate) {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        try {
            out.write(header, 0, 44);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
