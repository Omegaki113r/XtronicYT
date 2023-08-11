package com.omegaki113r.xtronic.hm10_custom_android;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.IBinder;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServiceConnection,BLECallback{

    private static final int REQUEST_ENABLE_COARSE_LOCATION = 1000;
    private static final int REQUEST_ENABLE_FINE_LOCATION = 1001;
    private static final int REQUEST_ENABLE_BLUETOOTH_ADMIN = 1003;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1004;

    BLEService service = null;
    private ServiceConnection connection = this;
    BLECallback callback;

    TextView _recievedMessaage;
    TextView _readMessage;
    EditText _messageToBeSent;
    Button _sendMessage;
    Button _readMessageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         _recievedMessaage = findViewById(R.id.tv_ble_recieved);
         _readMessage = findViewById(R.id.tv_received_message);
         _messageToBeSent = findViewById(R.id.et_message);
         _sendMessage = findViewById(R.id.btn_send_ble_message);
         _readMessageButton = findViewById(R.id.btn_read_ble);


         _sendMessage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                callback.sendMessages(_messageToBeSent.getText().toString().trim());
             }
         });

         _readMessageButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String readRequest = "$READ$";
                 callback.sendMessages(readRequest);
             }
         });


         Intent serviceIntent = new Intent(getApplicationContext(),BLEService.class);
         bindService(serviceIntent,connection,BIND_AUTO_CREATE);
    }

    @Override
    public void sendMessages(String _message) {

    }

    @Override
    public void recievedMessage(String _message) {
        Log.d("Main_Activity",_message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(_message.startsWith("$RR$")){
                    _readMessage.setText(_message.substring("$RR$".length()));
                }else {
                    _recievedMessaage.setText(_message);
                }
            }
        });

    }

    @Override
    public void callbackSet(BLECallback callback) {
        Log.d("Main_Activity","BLE service callback set");
        this.callback = callback;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d("Main_Activity","Service Connected");
        service = ((BLEService.BLEServiceCallbackBinder)iBinder).getService();
        service.setCallback(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    private void checkForPermission() {
        int coarseLocationPermission,fineLocationPermission,bluetoothAdmin,bluetooth;
        coarseLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        fineLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        bluetoothAdmin = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);
        bluetooth = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH);

        if(coarseLocationPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_ENABLE_COARSE_LOCATION);
            return;
        }
        if(fineLocationPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_ENABLE_FINE_LOCATION);
            return;
        }
        if(bluetoothAdmin != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_ADMIN},REQUEST_ENABLE_BLUETOOTH_ADMIN);
            return;
        }
        if(bluetooth != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH},REQUEST_ENABLE_BLUETOOTH);
            return;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_ENABLE_COARSE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Bluetooth Low Energy needs the Coarse Location service permission. Please accept the request",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_ENABLE_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Bluetooth Low Energy needs the Fine Location service permission. Please accept the request",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_ENABLE_BLUETOOTH_ADMIN:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getApplicationContext(),
                            "Bluetooth Low Energy needs the bluetooth admin permission. Please accept the request",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_ENABLE_BLUETOOTH:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(getApplicationContext(),
                            "Bluetooth Low Energy needs the bluetooth permission. Please accept the request",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
        checkForPermission();
    }

    private void checkBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

}