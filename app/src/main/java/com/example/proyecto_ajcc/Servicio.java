package com.example.proyecto_ajcc;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.proyecto_ajcc.api.Api_sen;
import com.example.proyecto_ajcc.api.Datos;
import com.example.proyecto_ajcc.api.Sensor;
import com.google.gson.Gson;

import com.example.proyecto_ajcc.telegram.Chat.From;
import com.example.proyecto_ajcc.telegram.Chat.Message;
import com.example.proyecto_ajcc.telegram.Chat.Result;
import com.example.proyecto_ajcc.telegram.Chat.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Servicio extends IntentService {

    //ARDUINO
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static String address = null;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private Hilo mHilo;

    //TELEGRAM
    public static final String BROADCAST_ACTION = "alan.apps.constants.BROADCAST";
    String TOKEN = "1742715619:AAH9beo_0lj9j5QMrHCpuZXU8WHwABRZRa8";
    String URL = "https://api.telegram.org/bot" + TOKEN + "/";

    //Variables para el Chat
    Request t = new Request();
    Message mensaje = new Message();
    List<Result> resultado = new ArrayList<>();

    Sensor sensor = new Sensor();

    String offset = "0";

    public Servicio() {
        super("Servicio");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        address = intent.getDataString();
        createComunicacion();
        Consulta();
        Intent localIntent = new Intent(BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void createComunicacion() {
        //create device and set the MAC address
        Log.i("Alan", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        mHilo = new Hilo(btSocket);
        mHilo.start();
        Log.i("Alan", "createComunicacion: " + mHilo);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    //Metodos de telegram

    public void Consulta() {
        while (true) {
            String rest = "";

            rest = RevisarUpdateId(rest, offset);

            if (!rest.equals("") && !rest.contains("post")) {
                Gson gson = new Gson();
                t = gson.fromJson(rest, Request.class);
                resultado = t.getResult();
                String update_id = "";
                if (resultado != null) {
                    VerificarMensajesChat(update_id);
                }
            }
        }
    }
    //Mensajes del bot
    private void VerificarMensajesChat(String update_id) {
        for(Result result : resultado) {
            update_id=result.getUpdateId();
            mensaje = result.getMessage();
        }

        if(Integer.parseInt(update_id) > (Integer.parseInt(offset) - 1)) {
            offset = String.valueOf(Integer.parseInt(update_id) + 1);
        }

        From f = mensaje.getFrom();

        if (mensaje.getText().equals("1")) {
            Datos_API("Pulso");
        } else if (mensaje.getText().equals("2")) {
            MandarMessage("Claro " + f.getFirstName() + "! Si deseas saber mas informacion accede al siguiente link (https://coronavirus.gob.mx/covid-19/) y listo.");
        } else if (mensaje.getText().equals("Gracias")) {
            MandarMessage("De nada " + f.getFirstName() + " esta enfermedad se combate juntos <3");
        } else if (mensaje.getText().equals("Adios")) {
            MandarMessage("Hasta pronto " + f.getFirstName() + "! te esperamos para consultar todas tus dudas POST-COVID ");
        } else if (mensaje.getText().equals("Adiós")) {
            MandarMessage("Nos vemos  " + f.getFirstName() + " vuelve pronto si tienes alguna duda");
        } else if (mensaje.getText().equals("Hola")) {
            MandarMessage("¿Como estas?  " + f.getFirstName() + "!  escribe la palabra (Opciones) para navegar por el bot ");
        } else if (mensaje.getText().equals("Opciones")) {
            String mens = "Hola " + f.getFirstName() + "! \n" +
                    "Seleccione el numero que necesite : \n" +
                    "(1. Quiero saber mi pulso),\n " +
                    "(2. Mas informacion acerca del COVID-19),\n " +
                    "(4. Conectar),\n " +
                    "(5. Desconectar)";
            MandarMessage(mens);
        } else if (mensaje.getText().equals("4")) {
            if (mHilo.write("1")) {
                MandarMessage("Conectando comunicacion con Arduino");
            } else {
                MandarMessage("Hubo un error al comunicarse con el arduino, verifique si selecciono el dispositivo correcto :(");
            }
        } else if (mensaje.getText().equals("5")) {
            if (mHilo.write("0")) {
                MandarMessage("Desconectando comunicacion con Arduino");
            } else {
                MandarMessage("Hubo un error al comunicarse con el arduino, verifique si selecciono el dispositivo correcto :(");
            }
        }

        resultado.clear();
    }

    private String RevisarUpdateId(String rest, String offset) {
        try {
            java.net.URL url = new URL(URL + "getUpdates" + "?offset=" + offset + "&timeout=" + "100");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-length", "0");
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.connect();

            int status = conn.getResponseCode();

            if (status == 200) {
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(reader);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                rest = sb.toString();
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rest;
    }

    public String MandarMessage(String men) {
        Log.d("DATA2", "onResponse: " + men);
        String rest = "";
        try {
            URL ur = new URL(URL + "sendMessage?chat_id=" + mensaje.getChat().getId() + "&text=" + men);
            HttpURLConnection conn = (HttpURLConnection) ur.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-length", "0");
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.connect();

            int status = conn.getResponseCode();

            if (status == 200) {
                rest = "Message Send as BOT";
            }

            conn.disconnect();
        } catch (Exception e) {
            Log.d("ERROR", "MandarMessage: " + e.getCause());
            e.printStackTrace();
        }
        return rest;
    }
    //Conexion al server
    public void Datos_API(String registro) {

        // Create a new object from HttpLoggingInterceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add Interceptor to HttpClient
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl("http://6709d774942d.ngrok.io/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api_sen sensorApi = retrofit.create(Api_sen.class);
        Call<Sensor> call = sensorApi.getdata();
        call.enqueue(new Callback<Sensor>() {
            @Override
            public void onResponse(Call<Sensor> call, Response<Sensor> response) {
                try {
                    if (response.isSuccessful()) {
                        sensor = response.body();
                        List<Datos> datas = sensor.getData();

                        if (registro.equals("Pulso")) {
                            DatosFecha(sensor.getData());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Sensor> call, Throwable t) {
                new Enviar_msj().execute("Hola "+mensaje.getFrom().getFirstName()+"! al parecer hay un error en la conexion intentalo de nuevo mas tarde :(");
                Log.d("ERROR", "onFailure: "+t.getMessage());
            }
        });

    }

    private int DatosFecha(List<Datos> datas) {
        int c = 0;
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        String date = df1.format(Calendar.getInstance().getTime());

        Log.d("FECHA", "DatosFecha: " + date);
        if (datas.get(datas.size()-1).getTime().substring(0,10).equals(date)){
            if (datas.get(datas.size()-1).getType()>100) {
                new Enviar_msj().execute("Hola " + mensaje.getFrom().getFirstName() + "! tu pulso es de: " + datas.get(datas.size()-1).getType() + " esta un poco elevado tu pulso, te recomiendo que te relajes, todo estara bien", mensaje.getChat().getId());
            } else {
                new Enviar_msj().execute("Hola " + mensaje.getFrom().getFirstName() + "! tu pulso en estos momentos es de: " + datas.get(datas.size()-1).getType(), mensaje.getChat().getId());
            }
        }
        else {
            new Enviar_msj().execute("Hola " + mensaje.getFrom().getFirstName() + "! Hoy no se han registrado nuevos datos:(", mensaje.getChat().getId());
        }

        return c;
    }
}
