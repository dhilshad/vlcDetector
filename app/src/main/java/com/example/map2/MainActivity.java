package com.example.map2;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.os.SystemClock;
import android.os.Handler;



public class MainActivity extends AppCompatActivity {

    private static final int RECORDER_BPP = 8;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 48000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    public int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    String filename=null;
    int num_of_bytes;

    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    TextView timer,txt;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        timer=(TextView)findViewById(R.id.textView);
        txt=(TextView)findViewById(R.id.textView2);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 1);



       setButtonHandlers();
       enableButtons(false);

        bufferSize = AudioRecord.getMinBufferSize(48000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
       // txt.setText(String.valueOf(bufferSize));
    }

    private void setButtonHandlers() {
        ((Button)findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.button2)).setOnClickListener(btnClick);
    }

    private void enableButton(int id,boolean isEnable){
        ((Button)findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart,!isRecording);
        enableButton(R.id.btnStop,isRecording);
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }
        filename=file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_TEMP_FILE;
        return (filename);
    }
    String filepath;
    private String getTempFilename(){
        filepath = Environment.getExternalStorageDirectory().getPath();
        //txt.setText(filepath);
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

       if(tempFile.exists())
            tempFile.delete();


        return (file.getAbsolutePath() + "/" +
                "" + AUDIO_RECORDER_TEMP_FILE);
    }

    private void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        //txt.setText(" ");

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();

        isRecording = true;

        num_of_bytes=0;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;



        try {
            os = new FileOutputStream(filename);

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();

        }

        int read = 0;

        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);

                //byte[] hell = new byte[4];
                //byte[] rd = new byte[4];
                //rd[0]=

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);

                       // num_of_bytes=num_of_bytes+read;
                       // os.write(hell,num_of_bytes,read);
                      //  os.write();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording(){
        if(null != recorder){
            isRecording = false;

            int i = recorder.getState();
            if(i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(),getFilename());
       deleteTempFile();
    }

    private void deleteTempFile() {
       // File file = new File(getTempFilename());

        //file.delete();
    }
    //byte datastream[]=new byte[600];
    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;


        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
           /* totalAudioLen = in.getChannel().size();
            txt.setText(String.valueOf(totalAudioLen));*/


            //AppLog.logString("File size: " + totalDataLen);


            int count=0;
            int start=0;
            boolean begning=true;
            byte datastream[]=new byte[200];

            while(in.read(data) != -1){
              ////finding start bi////////////
                if(begning) {

                   // int low =0;
                    int high=0;
                    for (int i = 3; i < 3000; i += 2) {

                   if (data[i]<(byte)0){
                    high++;
                   // low=0;
                       //txt.setText(txt.getText()+"yes"+String.valueOf(i)+"  ");
                    }
                    else{
                        //low++;
                        high=0;
                      // txt.setText(txt.getText()+"no"+String.valueOf(high)+"  ");

                    }
                    if (high==24){
                        start=i+1;
                        txt.setText(txt.getText()+"strt:"+String.valueOf(start));
                        break;
                        /*txt.setText(txt.getText()+"say"+String.valueOf(i+1));
                        byte temp=0;
                        for (int j=2;j<24;j=j+2){
                            if (data[i+j]<(byte)0)   temp=0;
                            else temp++;
                        }
                        if (temp==12){

                            start=i+1;
                            txt.setText(txt.getText()+"strt:"+String.valueOf(start));
                            break;
                        }*/
                    }
                    }
                    /////////end//////////////////////
                    ///////////////////////
                    int ones;
                    int bitcount=0;

                    boolean adjustment_needed;//to compensate for error in transmitter due to execution time
                    boolean start_bit; // represent start o

                    // f 6 bit sequence

                    for (int i = start; i < 3840-24; i = i + 24) {
                         ones=0;
                         start_bit =false;
                         adjustment_needed=false;

                       // if (i<1200) txt.setText(txt.getText()+"i:"+String.valueOf(i)+" ");
                        for (int j = 1; j < 12; j = j + 2) {
                            if (j==1) if (data[(i+j)]<0) start_bit = true;




                            if (data[(i+j)] < 0) ones++;


                        }

                        if (ones > 3) {

                            boolean is_start_falg=false;
                            byte temp=0;
                            //txt.setText(txt.getText()+" justi:"+String.valueOf(i));

                            for (int j=13;j<24;j=j+2){
                                if (data[i+j]<0) temp++;
                               // txt.setText(txt.getText()+" inside:"+String.valueOf(temp));
                            }
                            if (temp>3){
                                i=3840;
                            }
                            else{

                                datastream[bitcount] = 1;
                                bitcount++;
                                if (!start_bit) adjustment_needed=true;
                            }



                        }
                        else
                         {  //if (bitcount>1) if (datastream[bitcount-1]==0) txt.setText(txt.getText()+" her:"+String.valueOf(i));
                            datastream[bitcount] = 0;
                            bitcount++;
                           //
                             if (start_bit) adjustment_needed=true;

                        }
                        if (adjustment_needed) i=i+2;


                    }


                }
                begning=false;


                for (int i=0;i<12;i++){
                    int mult=1;
                    int charecter=0;
                    for (int j=0;j<8;j++){
                        charecter=charecter+(datastream[i*8+j]*mult);
                        mult=mult*2;
                    }
                    datastream[i]=(byte)charecter;

                }

                out.write(datastream);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/////////////////////////////////////////////////////////////////////////////////
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

      /*  byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);*/
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override

        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btnStart:{
                //    AppLog.logString("Start Recording");

                    enableButtons(true);
                    startRecording();

                    /////////timer///////////////
                     startTime = 0L;
                     timeInMilliseconds = 0L;
                     timeSwapBuff = 0L;
                     updatedTime = 0L;
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    ////////tmer end/////////////

                    break;
                }
                case R.id.btnStop:{
              //      AppLog.logString("Start Recording");

                    enableButtons(false);
                    stopRecording();

                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);


                    break;
                }
                //////////////////////////////button2////////////////////
                case R.id.button2:{
                    txt.setText("");

                }
            }
        }
    };
    /////////////////timer//////////////////////////////////////////////
    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timer.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));

            customHandler.postDelayed(this, 0);
        }

    };///////////////////////////timer end////////////////////////////*/


}
