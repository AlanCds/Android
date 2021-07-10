package com.example.proyecto_ajcc;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.proyecto_ajcc.telegram.Api.Sensor;
//import com.example.proyecto_ajcc.telegram.Api.SensorApi;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnOn, btnOff;

    TextView sensorView0;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private Hilo mHilo;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Get MAC address from Device_List via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(Device_List.EXTRA_DEVICE_ADDRESS);

        //Creamos el servicio
        Intent mServiceIntent = new Intent(MainActivity.this, Servicio.class);
        mServiceIntent.setData(Uri.parse(address));
        startService(mServiceIntent);
    }


}