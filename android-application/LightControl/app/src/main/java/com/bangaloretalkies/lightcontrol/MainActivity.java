package com.bangaloretalkies.lightcontrol;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class MainActivity extends Activity {

    private static final String TAG = "Light Control";
    private static final int SERVER_PORT = 10000;
    private static final int TIMEOUT_MS = 2500;
    private int dynamic_timeout_ms;
    private Integer dynamic_server_port;

    private EditText editTextPort;
    private EditText editTextIp;

    private boolean isSwitchChecked = false;

    private InetAddress getIpAddress() throws IOException {

        return InetAddress.getByName(editTextIp.getText().toString());
    }

    private void sendOnRequest(DatagramSocket socket) throws IOException {
        String data = String.format("ON");
        Log.d(TAG, "Sending data " + data);

        if (null != editTextPort && null != editTextPort.getText()) {
            Integer dsp = new Integer(editTextPort.getText().toString());
            dynamic_server_port = dsp.intValue();
            if (dynamic_server_port > 65535) {
                dynamic_server_port = SERVER_PORT;
                editTextPort.setText("10000");
            }
        }
        else {
            dynamic_server_port = SERVER_PORT;
        }
        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                getIpAddress(), dynamic_server_port);
        socket.send(packet);
    }

    private void sendOffRequest(DatagramSocket socket) throws IOException {
        String data = String.format("OF");
        Log.d(TAG, "Sending data " + data);

        if (null != editTextPort && null != editTextPort.getText()) {
            Integer dsp = new Integer(editTextPort.getText().toString());
            dynamic_server_port = dsp.intValue();
            if (dynamic_server_port > 65535) {
                dynamic_server_port = SERVER_PORT;
                editTextPort.setText("10000");
            }
        }
        else {
            dynamic_server_port = SERVER_PORT;
        }
        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                getIpAddress(), dynamic_server_port);
        socket.send(packet);
    }

    private void listenForResponses(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[2];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String s = new String(packet.getData(), 0, packet.getLength());
                Log.d(TAG, "Received response " + s);
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch onOffSwitch = (Switch)  findViewById(R.id.lightcontrolswitch1);

        onOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);
                isSwitchChecked = isChecked;
                new MyTask().execute(isChecked);
            }

        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(5000);
        seekBar.setProgress(TIMEOUT_MS);
        dynamic_timeout_ms = TIMEOUT_MS;

        editTextPort = (EditText) findViewById(R.id.editTextPort);
        editTextIp = (EditText) findViewById(R.id.editTextIp);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                Log.v("Progress=", "" + progresValue);
                dynamic_timeout_ms = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MyTask extends AsyncTask<Boolean, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(Boolean... isChecked) {
            try {
                DatagramSocket socket = new DatagramSocket();
                //socket.setBroadcast(true);
                socket.setSoTimeout(TIMEOUT_MS);

                if (isSwitchChecked)
                {
                    sendOnRequest(socket);
                }
                else
                {
                    sendOffRequest(socket);
                }

                listenForResponses(socket);

                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not send discovery request", e);
            }

            return null;
        }
    }
}
