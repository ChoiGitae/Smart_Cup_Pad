package com.example.choi.smart_cup_pad;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ImageView imageView_01;
    Button button_01;
    Button reset;
    TextView water_01;
    ProgressBar progressBar1;

    int percent = 0;
    double water = 0;
    double temp = 0;
    int i = 0;

    Calendar now = Calendar.getInstance();

    int hour = now.get(Calendar.HOUR);
    int minute = now.get(Calendar.MINUTE);
    int second = now.get(Calendar.SECOND);

    private static final String TAG = "MAIN";

    public static final int MESSAGE_STATE_CHANGE = 1;
    //    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_READ = 3;

    private static final int REQUEST_CONNEXT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final boolean D = true;

    public static final int MODE_REQUEST = 1;

    private Button btn_Connect;

    // synchronized flags
    private static final int STATE_SENDING = 1;
    private static final int STATE_NO_SENDING = 2;
    private int mSendingState;

    private BluetoothService bluetoothService_obj = null;
    private StringBuffer mOutStringBuffer;

    private String mConnectedDeviceName = null;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Connection Success!", Toast.LENGTH_SHORT).show();
                            break;

                        case BluetoothService.STATE_FAIL:
                            Toast.makeText(getApplicationContext(), "Connection Failed..", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                        /*
                        case MESSAGE_WRITE:
                            byte[] writeBuf = (byte[]) msg.obj;
                            // construct a string from the buffer
                            String writeMessage= new String(writeBuf);
                            writeMessage = button_01.getText().toString();
                            break;
                        */

                case MESSAGE_READ:
                    String readMessage = null;
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    readMessage = new String(readBuf, 0, msg.arg1);

                    while (true) {
                        if (readBuf[i] == '\0') {
                            temp = Integer.parseInt(readMessage);
                            readMessage = null;
                            break;
                        } else {
                            readMessage += readBuf[i];
                            i++;
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult" + resultCode);
        // TODO Auto-generated method stub

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                //When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK)  //취소를 눌렀을 때
                {
                    bluetoothService_obj.scanDevice();
                } else {
                    Log.d(TAG, "Bluetooth is not enable");
                }
                break;

            case REQUEST_CONNEXT_DEVICE:
                if (resultCode == Activity.RESULT_OK) ;
            {
                bluetoothService_obj.getDeviceInfo(data);
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        imageView_01 = (ImageView) findViewById(R.id.imageView_01);
        button_01 = (Button) findViewById(R.id.button_01);
        reset = (Button) findViewById(R.id.reset);
        water_01 = (TextView) findViewById(R.id.water_01);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        btn_Connect = (Button) findViewById(R.id.bluetooth_connect);
        btn_Connect.setOnClickListener(mClickListener);

        if (bluetoothService_obj == null) {
            bluetoothService_obj = new BluetoothService(this, mHandler);
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //분기.
            switch (v.getId()) {

                case R.id.bluetooth_connect:  //모든 블루투스의 활성화는 블루투스 서비스 객체를 통해 접근한다.

                    if (bluetoothService_obj.getDeviceState()) // 블루투스 기기의 지원여부가 true 일때
                    {
                        bluetoothService_obj.enableBluetooth();  //블루투스 활성화 시작.
                    } else {
                        finish();
                    }
                    break;

                default:
                    break;

            }//switch
        }
    };

    public void onClick01(View V) {

        if (hour == 0 && minute == 0 && second == 5) {
            water = 0;
            percent = 0;
            temp = 0;
            water_01.setText(0.0 + "%");
            progressBar1.setProgress(percent);
        }

        if (bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED) {
            water += (temp * 30);
            percent = (int) ((water / 1500) * 100);

            if (water <= 1500) {
                water_01.setText(percent + "%");
                progressBar1.setProgress(percent);

            } else {
                water_01.setText(0 + "%");
                progressBar1.setProgress(0);
            }

            if (water >= 200 && water < 500) {
                imageView_01.setImageResource(R.drawable.test1);
            } else if (water >= 500 && water < 800) {
                imageView_01.setImageResource(R.drawable.test2);
            } else if (water >= 800 && water < 1400) {
                imageView_01.setImageResource(R.drawable.test3);
            } else if (water >= 1400 && water < 1500) {
                Toast toast_01 = Toast.makeText(this, "Well done!", Toast.LENGTH_LONG);
                imageView_01.setImageResource(R.drawable.test4);
                toast_01.show();
            }

            temp = 0;
        }

        else {
            Toast.makeText(getApplicationContext(), "No Connection!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick02(View V) {
        water = 0;
        water_01.setText(0 + "%");
        progressBar1.setProgress(0);
        imageView_01.setImageResource(R.drawable.test);
    }
}
