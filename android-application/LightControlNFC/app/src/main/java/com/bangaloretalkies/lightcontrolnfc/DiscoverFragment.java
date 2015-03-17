package com.bangaloretalkies.lightcontrolnfc;

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
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

public class DiscoverFragment extends Fragment {
    private static final String TAG = "DiscoverFragment";
    private TextView textView;
    private Button refreshButton;

    private void sendDiscoverRequest(DatagramSocket socket) throws IOException {
        String data = String.format("discover");

        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                getBroadcastAddress(), 8080);
        socket.send(packet);
    }

    public static InetAddress getBroadcastAddress() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    Log.e(TAG, "Broadcast Address: " + interfaceAddress.getBroadcast());

                    if (null != interfaceAddress.getBroadcast())
                        return interfaceAddress.getBroadcast();
                }
            }
        }
        return null;
    }

    private void listenForResponses(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[1024];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String s = new String(packet.getData(), 0, packet.getLength());
                Log.d(TAG, "Received response " + s);

                //if (null != textView) {
                    //textView.setText(s.toCharArray(), 0, s.length());
                //}

                String tagName = "android:switcher:" + R.id.pager + ":" + 1;
                //Log.d(TAG, "Tag Name: " + tagName);
                OperateFragment f2 = (OperateFragment) getActivity().getSupportFragmentManager().findFragmentByTag(tagName);
                //f2.updateIp(s.split("|").toString().split(":").toString());


                String[] hosts = s.split(":");
                //String[] host1params = hosts[0].split(":");

                String[] port = hosts[1].split("/");

                Log.d(TAG, "IP: " + hosts[0]);
                Log.d(TAG, "Port: " + port[0]);
                f2.updateIp(hosts[0]);
                f2.updatePort(port[0]);
                break;
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        textView = (TextView) rootView.findViewById(R.id.discoverTextView);
        refreshButton = (Button) rootView.findViewById(R.id.refreshbutton);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyTask().execute();
            }
        });

        new MyTask().execute();

        return rootView;
    }


    public class MyTask extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... isChecked) {

            try {
                DatagramSocket discoverSocket = new DatagramSocket();
                discoverSocket.setBroadcast(true);
                discoverSocket.setSoTimeout(5000);

                sendDiscoverRequest (discoverSocket);
                listenForResponses(discoverSocket);
                discoverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not send discovery request", e);
            }

            return null;
        }
    }
}