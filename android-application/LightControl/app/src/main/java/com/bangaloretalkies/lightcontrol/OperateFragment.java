package com.bangaloretalkies.lightcontrol;

/**
 * Created by Sandeep on 2/24/2015.
 */
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

public class OperateFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "OperateFragment";

    private static final int SERVER_PORT = 10000;
    private static final int TIMEOUT_MS = 2500;
    private int dynamic_timeout_ms;
    private Integer dynamic_server_port;

    private EditText editTextPort;
    private EditText editTextIp;

    private String dynamic_ip;
    private String dynamic_port;

    private boolean isSwitchChecked = false;

    private InetAddress getIpAddress() throws IOException {

        return InetAddress.getByName(editTextIp.getText().toString());
    }

    private void sendOnRequest(DatagramSocket socket) throws IOException {
        String data = String.format("on");
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
        String data = String.format("off");
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
        byte[] buf = new byte[1024];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String s = new String(packet.getData(), 0, packet.getLength());
                Log.d(TAG, "Received response " + s);
                break;
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_operate, container, false);

        Switch onOffSwitch = (Switch)  rootView.findViewById(R.id.lightcontrolswitch1);

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);
                isSwitchChecked = isChecked;
                new MyTask().execute(isChecked);
            }

        });

        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setMax(5000);
        seekBar.setProgress(TIMEOUT_MS);
        dynamic_timeout_ms = TIMEOUT_MS;

        editTextPort = (EditText) rootView.findViewById(R.id.editTextPort);
        editTextIp = (EditText) rootView.findViewById(R.id.editTextIp);

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

        return rootView;
    }

    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
    }

    public void updateIp (String ip)
    {
        dynamic_ip = ip;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.

                if (!editTextIp.getText().toString().equals(dynamic_ip)) {
                    editTextIp.setText(dynamic_ip);
                }
            }
        });
    }

    public void updatePort (String port)
    {
        dynamic_port = port;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.

                if (!editTextPort.getText().toString().equals(dynamic_port)) {
                    editTextPort.setText(dynamic_port);
                }
            }
        });
    }


    public class MyTask extends AsyncTask<Boolean, Integer, Boolean> {

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