package com.omegaki113r.xtronic.hm10_custom_android;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEService extends Service implements BLECallback{

    private static UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static UUID HM10_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static UUID NOTIFICATION_CHARACTERISTIC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public BLECallback callback;
    private BLEServiceCallbackBinder bleServiceCallbackBinder = new BLEServiceCallbackBinder();

    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;

    private BluetoothDevice _device;

    private String _message = "";


    @Override
    public IBinder onBind(Intent intent) {
        manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        adapter.getBluetoothLeScanner().startScan(scanCallback);
        return bleServiceCallbackBinder;
    }

    public void setCallback(BLECallback callback){
        Log.d("BLE_SERVICE","callback set from main activity");
        this.callback = callback;
        this.callback.callbackSet(this);
    }



    public class BLEServiceCallbackBinder extends Binder{
        public BLEService getService(){
            return BLEService.this;
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String _BLEDeviceName = result.getDevice().getName();
//            Log.d("BLE_SERVICE",_BLEDeviceName);
            if(_BLEDeviceName != null && _BLEDeviceName.equals("BT05")){
                Log.d("BLE_SERVICE","Device Found");
                _device = result.getDevice();
                adapter.getBluetoothLeScanner().stopScan(this);
                gatt = _device.connectGatt(getApplicationContext(),true,BLECallback);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d("BLE_SERVICE",results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("BLE_SERVICE","Scan Failed");
        }
    };

    private BluetoothGattCallback BLECallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {

        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {

        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED){
                Log.d("BLE_SERVICE","Connected to HM10");
                gatt.discoverServices();
            }else if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_DISCONNECTED){
                gatt.close();
            }else{

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            ArrayList<BluetoothGattService> serviceArrayList = (ArrayList<BluetoothGattService>) gatt.getServices();
            for(int i=0;i<serviceArrayList.size();++i){
               BluetoothGattService service = serviceArrayList.get(i);
               if(service.getUuid().equals(SERVICE_UUID)){
                   Log.d("BLE_SERVICE","Matching service Found");
                   BluetoothGattCharacteristic characteristic = service.getCharacteristic(HM10_CHARACTERISTIC);
                   BluetoothGattDescriptor descriptor = characteristic.getDescriptor(NOTIFICATION_CHARACTERISTIC);
                   descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                   gatt.setCharacteristicNotification(characteristic,true);
//                   characteristic.setValue(_message);
//                   gatt.writeCharacteristic(characteristic);
               }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("BLE_SERVICE","Wrote to characteristic");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String _recievedMessage = characteristic.getStringValue(0);
            Log.d("BLE_SERVICE","Characteristic changed " + _recievedMessage);
            _disconnect(_recievedMessage);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d("BLE_SERVICE","Wrote to descriptor");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

        }
    };

    @Override
    public void sendMessages(String _message) {
        this._message = _message;
        BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(HM10_CHARACTERISTIC);
        characteristic.setValue(_message);
        gatt.writeCharacteristic(characteristic);
    }

    @Override
    public void recievedMessage(String _message) {
    }

    @Override
    public void callbackSet(BLECallback callback) {

    }

    void _disconnect(String string){
        callback.recievedMessage(string);
    }
}