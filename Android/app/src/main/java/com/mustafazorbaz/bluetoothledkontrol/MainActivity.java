package com.mustafazorbaz.bluetoothledkontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.io.IOException;//Veri gönderirken alırken oluşan hatalar için
import java.io.InputStream; //Bluetooth tan veri alma ile ilgili
import java.io.OutputStream; //Bleutooth tan veri gönderme ile ilgili
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID; //Haberleşme için id mizi belirtir onun için kullandık.

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket; //Soket için
import android.content.BroadcastReceiver; //İşlemcimizden gelen yayınları dinlemek için kullanılır.
import android.content.Context;
import android.content.Intent;   //İştekte bulunmak için
import android.content.IntentFilter;
import android.os.Handler;    //Ekranda mesajların gözükmesi için kullanacagız.
import android.os.Message;		 //Mesajlasmamızı saglar.
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;  //Listbox a tıklamamız için
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    private Button ledonbtn, btnLedM, btnLedK, btnLedS, btnAlarm;
    private Button alarmBtn;
    ToggleButton toggleBtnS;
    private Button ledoffbtn;
    private TextView text;
    OutputStream outstream;
    ArrayAdapter<String> adaptorlist;  
    ListView liste;
    BluetoothAdapter blt;
    Set<BluetoothDevice> arraydevice;   //Ekrandan aygıların yani ipleri listeleyecegiz.
    ArrayList<String> eslesen;
    ArrayList<BluetoothDevice> aygitlar;
    public static final UUID mmuuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int baglanti = 0;
    protected static final int mesajoku = 1;
    IntentFilter filtre;
    BroadcastReceiver receiver;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case baglanti:

                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
                    Toast.makeText(getApplicationContext(), "Baglandı", 0).show();
                    String s = "successfully connected";
                    break;
                case mesajoku:
                    byte[] readBuf = (byte[]) msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, 0).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        liste = (ListView) findViewById(R.id.listem);
        liste.setOnItemClickListener(this);
        adaptorlist = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        liste.setAdapter(adaptorlist);
        blt = BluetoothAdapter.getDefaultAdapter(); //Android cihazimizin bluetooth adaptere sahip olup olmadıgını tutur.
        eslesen = new ArrayList<String>();
        filtre = new IntentFilter(BluetoothDevice.ACTION_FOUND); //Telefonumuzuzda daha önceden eslesen cihazlarlaa degilde yanımızda aktif olan cihazlarla eşleştirmek için bu filteri kullanıyoruz.
       //ACTİON_FOUND suanda çalışanları alır.
	   aygitlar = new ArrayList<BluetoothDevice>(); 

	   //Telefonumuuzda bluetooth için yayıyn vaar işlemlerden haberdar olmak için reciver ile ögreniyoruz.
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    aygitlar.add(device);
                    String s = "";

                    for (int a = 0; a < eslesen.size(); a++) {   //Çalışan aytıları listeledik
                        if (device.getName().equals(eslesen.get(a))) {  //Eşlesen aygıtımııza baglıyacaz

                            s = "(Eslesti)";
                            break;
                        }
                    }

                    adaptorlist.add(device.getName() + "" + s + "" + "\n" + device.getAddress());
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if (blt.getState() == blt.STATE_OFF) {    //Bluetooth'umuz kapalı
                        bltac();
                    }
                }

            }


        };

        registerReceiver(receiver, filtre);
        filtre = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filtre);

 
        if (blt == null) { //Bluetooh cihazinın olmadıgını gosteriyor.
            Toast.makeText(MainActivity.this, "Bluetooth aygıtları bulunamadı", Toast.LENGTH_SHORT).show();
            text.setText("Bluetooth aygıtı yok");
            finish();
        } else {
            if (!blt.isEnabled()) {
                bltac();
            }

            getPairedDevices();
            startDiscovery();

            ledonbtn = (Button) findViewById(R.id.ledon);
            ledonbtn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                   try {
                        ledon();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                    }
                }
            });

            ledoffbtn = (Button) findViewById(R.id.ledoff);
            ledoffbtn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                   try {
                        ledoff();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                    }
                }
            });


        }

        /*********************TOOGLE BUTTON ALARM İÇİN *******************************************/


        final int[] durumBtnAlarm = {0};

        btnAlarm = (Button) findViewById(R.id.buttonAlarm);
        btnAlarm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//View v button,MotionEvent evet basılıp çekildigi kontrol eder.
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                       // Toast.makeText(MainActivity.this, "Buttondan Çektin", Toast.LENGTH_SHORT).show();

                        Button view = (Button) v;
                        //if (durumBtnAlarm[0] == 0)
                            btnAlarm.setBackgroundResource(R.drawable.alarm4);
                      //  else
                       //     btnAlarm.setBackgroundResource(R.drawable.alarm_acik);

                        view.invalidate();
                        break;

                    }
                    case MotionEvent.ACTION_DOWN: {
                        // Toast.makeText(MainActivity.this,"Buttona Bastın",Toast.LENGTH_SHORT).show();
                        Button view = (Button) v;
                      /*  if (durumBtnAlarm[0] == 1) {
                            btnAlarm.setBackgroundResource(R.drawable.alarm4);
                            durumBtnAlarm[0] = 0;
                            try {
                                ledoffA();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {*/
                            btnAlarm.setBackgroundResource(R.drawable.alarm_acik);
                            durumBtnAlarm[0] = 1;
                            try {
                                ledonA();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                      //  }
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL: {

                        Button view = (Button) v;
                       /* if (durumBtnAlarm[0] == 1) {
                            btnAlarm.setBackgroundResource(R.drawable.alarm4);
                            durumBtnAlarm[0] = 0;
                            try {
                                ledoffA();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {*/
                            btnAlarm.setBackgroundResource(R.drawable.alarm_acik);
                            durumBtnAlarm[0] = 1;
                            try {
                                ledonA();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                     //   }
                        view.invalidate();
                        break;

                    }
                }

                return true;
            }
        });
        /**********************************************************************************************************/

        /*********************TOOGLE BUTTON MAVİ LED İÇİN *******************************************/

        final int[] durumBtnM = {0};
        btnLedM = (Button) findViewById(R.id.buttonLedM);
        btnLedM.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//View v button,MotionEvent evet basılıp çekildigi kontrol eder.
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                      //  Toast.makeText(MainActivity.this, "Buttondan Çektin", Toast.LENGTH_SHORT).show();

                        Button view = (Button) v;
                        if (durumBtnM[0] == 0)
                            btnLedM.setBackgroundResource(R.drawable.kplled);
                        else
                            btnLedM.setBackgroundResource(R.drawable.maviled);

                        view.invalidate();
                        break;

                    }
                    case MotionEvent.ACTION_DOWN: {
                        // Toast.makeText(MainActivity.this,"Buttona Bastın",Toast.LENGTH_SHORT).show();
                        Button view = (Button) v;
                        if (durumBtnM[0] == 1) {
                            btnLedM.setBackgroundResource(R.drawable.kplled);
                            durumBtnM[0] = 0;
                           try {
                               ledoffm();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            btnLedM.setBackgroundResource(R.drawable.maviled);
                            durumBtnM[0] = 1;
                            try {
                                ledonm();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL: {

                        Button view = (Button) v;
                        if (durumBtnM[0] == 1) {
                            btnLedM.setBackgroundResource(R.drawable.kplled);
                            durumBtnM[0] = 0;
                            try {
                                ledoffm();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            btnLedM.setBackgroundResource(R.drawable.maviled);
                            durumBtnM[0] = 1;
                            try {
                                ledonm();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        view.invalidate();
                        break;

                    }
                }

                return true;
            }
        });
        /**********************************************************************************************************/

        /*********************TOOGLE BUTTON KIRMIZI İÇİN *******************************************/

        final int[] durumBtnK = {0};

        btnLedK = (Button) findViewById(R.id.buttonLedK);
        btnLedK.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//View v button,MotionEvent evet basılıp çekildigi kontrol eder.
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                       // Toast.makeText(MainActivity.this, "Buttondan Çektin", Toast.LENGTH_SHORT).show();

                        Button view = (Button) v;
                        if (durumBtnK[0] == 0)
                            btnLedK.setBackgroundResource(R.drawable.kplled);
                        else
                            btnLedK.setBackgroundResource(R.drawable.kirmiziled);

                        view.invalidate();
                        break;

                    }
                    case MotionEvent.ACTION_DOWN: {
                        // Toast.makeText(MainActivity.this,"Buttona Bastın",Toast.LENGTH_SHORT).show();
                        Button view = (Button) v;
                        if (durumBtnK[0] == 1) {
                            btnLedK.setBackgroundResource(R.drawable.kplled);
                            durumBtnK[0] = 0;
                            try {
                                ledoffk();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            btnLedK.setBackgroundResource(R.drawable.kirmiziled);
                            durumBtnK[0] = 1;

                            try {
                                ledonk();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL: {

                        Button view = (Button) v;
                        if (durumBtnK[0] == 1) {
                            btnLedK.setBackgroundResource(R.drawable.kplled);
                            durumBtnK[0] = 0;
                           try {
                               ledoffk();
                           } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            btnLedK.setBackgroundResource(R.drawable.kirmiziled);
                            durumBtnK[0] = 1;
                           try {
                               ledonk();
                           } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        view.invalidate();
                        break;

                    }
                }

                return true;
            }
        });
        /**********************************************************************************************************/
        /*********************TOOGLE BUTTON SARI LED İÇİN *******************************************/

        final int[] durumBtnS = {0};

        btnLedS = (Button) findViewById(R.id.buttonLedS);
        btnLedS.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//View v button,MotionEvent evet basılıp çekildigi kontrol eder.
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                     //   Toast.makeText(MainActivity.this, "SARI LED'ten  Çektin", Toast.LENGTH_SHORT).show();

                        Button view = (Button) v;
                        if (durumBtnS[0] == 0)
                            btnLedS.setBackgroundResource(R.drawable.kplled);
                        else
                            btnLedS.setBackgroundResource(R.drawable.sariled);

                        view.invalidate();
                        break;

                    }
                    case MotionEvent.ACTION_DOWN: {
                       // Toast.makeText(MainActivity.this,"SARI LED'eButtona Bastın",Toast.LENGTH_SHORT).show();
                        Button view = (Button) v;
                        if (durumBtnS[0] == 1) {
                            btnLedS.setBackgroundResource(R.drawable.kplled);
                            durumBtnS[0] = 0;
                            try {
                                ledoffs();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {
                            btnLedS.setBackgroundResource(R.drawable.sariled);
                            durumBtnS[0] = 1;
                            try {
                                ledons();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL: {

                        Button view = (Button) v;
                        if (durumBtnS[0] == 1) {
                            btnLedS.setBackgroundResource(R.drawable.kplled);
                            durumBtnS[0] = 0;
                            try {
                                ledoffs();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            btnLedS.setBackgroundResource(R.drawable.sariled);
                            durumBtnS[0] = 1;
                            try {
                                ledons();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                        view.invalidate();
                        break;

                    }
                }

                return true;
            }
        });
        /**********************************************************************************************************/

    }


    private void startDiscovery() {  //Bu fonksiyon aramaya başlamak için kullanmış oldugumuz bir fonksiyon
        
        blt.cancelDiscovery();//Arama islemi varsa onu kapattık
        blt.startDiscovery();  //Yeniden başlatmış olduk.


    }

    private void bltac() {  //Bluetooth'u açmak için istekte bulunacagız bunu intent ile saglıyoruz intent istek içindi.
        // TODO Auto-generated method stub
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
        startActivityForResult(intent, 1);
    }

    private void getPairedDevices() {
        // TODO Auto-generated method stub
        arraydevice = blt.getBondedDevices();
        if (arraydevice.size() > 0) {
            for (BluetoothDevice device : arraydevice) {
                eslesen.add(device.getName());

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) { //Bir anda bluetooth gidiyor onun için uyarı mesajı yayınlamamız gerekiyor.
            Toast.makeText(getApplicationContext(), "Kontrol işlemi için bluetoothun açık olması gerekir", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) { //Eşleşen aygıtın üzerinde herhangi bir aygıta tıkladıgımızda o aygıtın seçilmesini sağlıyacagiz
        // TODO Auto-generated method stub

        if (blt.isDiscovering()) {  //Aygıtımız arama yapiyorsa bu aramayı durdurmamız gerekir onun için yazdık.Durdurma Sebebimiz ise bizim aygıtımızın eşleşme işlemini sağlamısıdır.
            blt.cancelDiscovery(); //Aramayı sonlandırdık.
        } 
        if (adaptorlist.getItem(arg2).contains("Eslesti")) { //Secilen aygıtta Eslesti içerirse işlemleri yaptıracagız.

            BluetoothDevice selectedDevice = aygitlar.get(arg2);
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();

        } else {
            Toast.makeText(getApplicationContext(), "Cihazlar eşleşmedi", 0).show();
            text.setText("Cihazlar eşleşmedi");
        }
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket; //Bluetooth için soketi tanımşarık
        private final BluetoothDevice mmDevice;  //Bluetooth içn aygıt sürücümüzü tanımladık.

        public ConnectThread(BluetoothDevice device) {

            BluetoothSocket tmp = null;
            mmDevice = device;

            try {//Buradaki Try-Cath Yapısında bağlamıs ise ve bizim idmiz ile değer dönüyorsa sorun yok.Eğer bağlanmıyorsa Bağlantı yok şeklinde bize mesaj gönderir.

                tmp = device.createRfcommSocketToServiceRecord(mmuuid);
            } catch (IOException e) {

            }
            mmSocket = tmp;
        }

        public void run() {

            blt.cancelDiscovery();

            try {

                mmSocket.connect();  //Bağlantı yapıyor

            } catch (IOException connectException) { //Bağlantıda hata meydana gelmiş ise

                try { //Bağlantı sonucunda hata olmus işe soketi kapatacagız daha sonra hatayı geri göndereceğiz.
                    mmSocket.close(); //Soketi kapat
                } catch (IOException closeException) {
                }
                return;
            }

            mHandler.obtainMessage(baglanti, mmSocket).sendToTarget();
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket; //İsimler karısasın diye mmSokent veridim.
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try { //Bluetooth tan gelen ve giden verileri Stream içerisine koyduk
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn; //Gelen veriyi  inputStream'e atadık.
            outstream = tmpOut;
        }

        public void run() {
            byte[] buffer;
            int bytes;


            while (true) {
                try {

                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);

                    mHandler.obtainMessage(mesajoku, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    public void ledonm() throws IOException {
        outstream.write("5".getBytes());
    }
    public void ledoffm() throws IOException {
        outstream.write("6".getBytes());
    }
    public void ledonk() throws IOException {
        outstream.write("3".getBytes());
    }
    public void ledoffk() throws IOException {
        outstream.write("4".getBytes());
    }

    public void ledons() throws IOException {
        outstream.write("1".getBytes());
    }
    public void ledoffs() throws IOException {
        outstream.write("2".getBytes());
    }

    /*******Alarm için ***/

    public void ledonA() throws IOException {
        outstream.write("7".getBytes());
    }
    public void ledoffA() throws IOException {
        outstream.write("8".getBytes());
    }
    /**********************************/












    public void ledon() throws IOException {
        outstream.write("1".getBytes());
    }

       public void ledoff() throws IOException {
            outstream.write("2".getBytes());
        }

}













