package com.ssgames.com.omiplus.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.bluetooth.BTConnection;
import com.ssgames.com.omiplus.bluetooth.BTConnectionHandler;
import com.ssgames.com.omiplus.bluetooth.BTConnectionListener;
import com.ssgames.com.omiplus.util.Constants;
import com.ssgames.com.omiplus.util.SettingsManager;

import java.util.ArrayList;

public class OmiGameActivity extends Activity implements BTConnectionListener {

    private static final String TAG = OmiGameActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;

    private boolean bluetoothIsUsing = false;

    private AlertDialog mAlertDialog = null;

    private boolean mIsHostGame = false;
    private BluetoothAdapter mBTAdapter = null;
    private BTConnectionHandler mBtConnectionHandler = null;


    //nickname
    private AlertDialog mNickNameDialog = null;
    private String oldTextVal = "";
    private TextWatcher textWatcher = null;

    //Join
    private ArrayList<BluetoothDevice> discoveredList = null;

    /*
	 * Bluetooth broadcast receiver
	 */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                if (!mIsHostGame) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART
                            && device.getName().contains("Join Omi")) {
                        bluetoothDeviceFound(device);
                        Log.v(TAG,"Hosted device found");
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                //txtSearch.setText("SEARCH FOR DEVICES");
                //refreshViews();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBtConnectionHandler.removeDisconnectedRemoteConnection(device);
                //connectionList = btConnectionHandler.getConnectionList();
                //refreshViews();
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBtConnectionHandler.addRemoteDeviceIfNotExist(device);
                //connectionList = btConnectionHandler.getConnectionList();
                //refreshViews();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omi_game);

        Intent intent = getIntent();
        mIsHostGame = intent.getBooleanExtra(Constants.ExtraKey.EXTRA_KEY_HOST_OR_JOIN, false);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sorry!");
            builder.setMessage("Your device does not support bluetooth.");
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.dismiss();
                    noBluetoothAndEndGame();
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }else {
            if (!mBTAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else {
                configureGame();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothIsUsing) {
            if (mIsHostGame) {
                resetHostBluetoothName();
            }else {
                resetJoinBluetoothName();
            }

            mBtConnectionHandler.removeConnectionListener(this);
            mBtConnectionHandler.clear();

            if (mBTAdapter != null) {
                mBTAdapter.cancelDiscovery();
            }

            try {
                this.unregisterReceiver(mReceiver);
            }catch (Exception e){}
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                configureGame();
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Sorry!");
                builder.setMessage("Bluetooth not enabled.");
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();
                        noBluetoothAndEndGame();
                    }
                });
                mAlertDialog = builder.create();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.setCancelable(false);
                mAlertDialog.show();
            }
        }
    }

    private void noBluetoothAndEndGame() {
        finish();
    }

    private void configureGame() {

        this.bluetoothIsUsing = true;

        String nickName = SettingsManager.getSetting(Constants.UserKey.NICK_NAME, "", getApplicationContext());
        if (nickName.length() == 0) {
            setNickName();
            return;
        }

        if (mIsHostGame) {
            //configure as a Host
            changeHostBluetoothName();
        }else {
            //configure as a Join
            changeJoinBluetoothName();
        }

        startBluetoothServices();
    }

    private void startBluetoothServices() {

        mBtConnectionHandler = BTConnectionHandler.getSharedInstance();
        mBtConnectionHandler.addConnectionListener(this);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        if (mIsHostGame) {

        }else {
            filter.addAction(BluetoothDevice.ACTION_FOUND);
        }

        this.registerReceiver(mReceiver, filter);

        if (mIsHostGame) {
            mBtConnectionHandler.workAsHost = true;
            mBtConnectionHandler.startListenIncomingConnections();
            enableDiscoverable();
        }else {
            searchHostedGame();
        }
    }

    /*
	 * Common
	 */
    private void setNickName() {

        final EditText nameET = new EditText(OmiGameActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nameET.setTextColor(getResources().getColor(R.color.black));
        nameET.setHint("Nickname");
        nameET.setLayoutParams(lp);

        textWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 6) {
                    nameET.removeTextChangedListener(textWatcher);
                    if (oldTextVal.length() == 6) {
                        nameET.setText(oldTextVal);
                    }else {
                        String text = s.toString();
                        text = text.substring(0, 6);
                        nameET.setText(text);
                    }
                    nameET.setSelection(6);
                    nameET.addTextChangedListener(textWatcher);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                oldTextVal = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        nameET.addTextChangedListener(textWatcher);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter a name for you.(Your friends will find you using this name.)");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String name = nameET.getText().toString();
                if (name != null && name.length() > 0) {
                    SettingsManager.addSetting(Constants.UserKey.NICK_NAME, name, getApplicationContext());
                    mNickNameDialog.dismiss();
                    configureGame();
                }
            }
        });
        mNickNameDialog = builder.create();
        mNickNameDialog.setView(nameET);
        mNickNameDialog.setCanceledOnTouchOutside(false);
        mNickNameDialog.setCancelable(false);
        mNickNameDialog.show();
    }

    /*
	 * Host game config
	 */
    private void changeHostBluetoothName() {
        String oriBtName = mBTAdapter.getName();
        String nickName = SettingsManager.getSetting(Constants.UserKey.NICK_NAME, "", getApplicationContext());
        String newBtName = "Join Omi - " + nickName;

        mBTAdapter.setName(newBtName);

        SettingsManager.addSetting(Constants.UserKey.ORI_BT_NAME, oriBtName, getApplicationContext());
        SettingsManager.addSetting(Constants.UserKey.NEW_BT_NAME, newBtName, getApplicationContext());
    }

    private void resetHostBluetoothName() {
        String oldBtName = SettingsManager.getSetting(Constants.UserKey.ORI_BT_NAME, "", getApplicationContext());
        if (oldBtName.length() > 0) {
            mBTAdapter.setName(oldBtName);
        }

        SettingsManager.addSetting(Constants.UserKey.ORI_BT_NAME, "", getApplicationContext());
        SettingsManager.addSetting(Constants.UserKey.NEW_BT_NAME, "", getApplicationContext());
    }

    private void enableDiscoverable() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    /*
	 * Join game config
	 */
    private void changeJoinBluetoothName() {
        String oriBtName = mBTAdapter.getName();
        String nickName = SettingsManager.getSetting(Constants.UserKey.NICK_NAME, "", getApplicationContext());
        mBTAdapter.setName(nickName);

        SettingsManager.addSetting(Constants.UserKey.ORI_BT_NAME, oriBtName, getApplicationContext());
        SettingsManager.addSetting(Constants.UserKey.NEW_BT_NAME, nickName, getApplicationContext());
    }

    private void resetJoinBluetoothName() {
        String oldBtName = SettingsManager.getSetting(Constants.UserKey.ORI_BT_NAME, "", getApplicationContext());
        if (oldBtName.length() > 0) {
            mBTAdapter.setName(oldBtName);
        }

        SettingsManager.addSetting(Constants.UserKey.ORI_BT_NAME, "", getApplicationContext());
        SettingsManager.addSetting(Constants.UserKey.NEW_BT_NAME, "", getApplicationContext());
    }

    private void searchHostedGame() {
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
        }

        mBTAdapter.startDiscovery();
    }

    private void bluetoothDeviceFound(BluetoothDevice device) {
        if (discoveredList == null) {
            discoveredList = new ArrayList<>();
        }

        if (!discoveredList.contains(device)) {
            discoveredList.add(device);
            refreshJoinListView();
        }
    }

    private void refreshJoinListView() {

    }


    /*
	 * BTConnectionListener methods
	 */
    @Override
    public void connectionEstablished(BTConnection connection) {

    }

    @Override
    public void connectionDisconnected(BluetoothDevice device) {

    }

    @Override
    public void deviceNotConnected(BluetoothDevice device) {

    }

    @Override
    public void dataReceived(BTConnection connection, byte[] buffer) {

    }

    @Override
    public void dataDidSend(BTConnection connection) {

    }

    @Override
    public void dataDidNotSend(BTConnection connection, byte[] buffer) {

    }
}
