package etna.mycallrecorder;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    private TelephonyManager manager;
    private File audiofile;
   // private AudioRecord recorder;
    private MediaRecorder recorder;
    private String TAG;
    private int swap;
    private File sampleDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(this, DeviceAdminDemo.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                // mDPM.lockNow();
                // Intent intent = new Intent(MainActivity.this,
                // TrackDeviceService.class);
                // startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(stateListener, PhoneStateListener.LISTEN_CALL_STATE);

        recorder = new MediaRecorder();

        swap = 0;

        TAG = "TelephonyExample";
        sampleDir = new File(Environment.getExternalStorageDirectory(), "/storage/TestRecordingDasa1");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_CODE == requestCode) {
            Intent intent = new Intent(MainActivity.this, TService.class);
            startService(intent);
        }
    }
    PhoneStateListener stateListener = new PhoneStateListener() {

        // Appelee quand est declenche l'evenement LISTEN_CALL_STATE
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "Pas d'appel en cours");
                    if(swap != 0) {
                        recorder.stop();
                        swap = 0;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "Il y a une communication téléphonique en cours");
/*
                    recorder.startRecording();
                    int sampleRateInHz = 44100;
                    int channelconfig = AudioFormat.CHANNEL_IN_STEREO;
                    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                    int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelconfig, audioFormat);
                    AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_CALL, sampleRateInHz, channelconfig, audioFormat, bufferSize);
                    short[] buffer = new short[bufferSize];
                    while(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                        int nombreDeShorts = recorder.read(buffer, 0, bufferSize);
                        Log.d(TAG, String.valueOf(nombreDeShorts));
                    }
*/


                    String out = new SimpleDateFormat("hh-mm-ss").format(new Date());
                    String file_name = incomingNumber + "_" +out;
                    try {
                        audiofile = File.createTempFile(file_name, ".amr",sampleDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    recorder.setOutputFile(audiofile.getAbsolutePath());
                    try {
                        recorder.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                        recorder.reset();
                        recorder.release();
                    }
                    swap = 1;
                    recorder.start();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "Le téléphone sonne, l'appelant est " + incomingNumber);
                    break;
                default:
                    Log.d(TAG, "Etat inconnu");
            }
        }
    };
}
