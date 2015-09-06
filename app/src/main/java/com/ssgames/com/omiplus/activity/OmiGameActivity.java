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
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.bluetooth.BTConnection;
import com.ssgames.com.omiplus.bluetooth.BTConnectionHandler;
import com.ssgames.com.omiplus.bluetooth.BTConnectionListener;
import com.ssgames.com.omiplus.bluetooth.BTDataPacket;
import com.ssgames.com.omiplus.model.OmiPlayer;
import com.ssgames.com.omiplus.util.Constants;
import com.ssgames.com.omiplus.util.SettingsManager;
import com.ssgames.com.omiplus.views.OmiGameView;
import com.ssgames.com.omiplus.views.OmiHostView;
import com.ssgames.com.omiplus.views.OmiJoinView;

import org.json.JSONObject;

import java.util.ArrayList;

public class OmiGameActivity extends Activity implements BTConnectionListener , OmiGameView.OmiGameViewListener{

    private static final String TAG = OmiGameActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_BT_DISCOVERY = 2;
    private static final int maxConnCount = 3;

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
    private boolean isJoinedToGame = false;

    private boolean isGameStarted = false;

    //UIs
    private LinearLayout mGameLayout = null;
    private LinearLayout mPopupLayout = null;

    private OmiJoinView mOmiJoinView = null;
    private OmiHostView mOmiHostView = null;
    private OmiGameView mOmiGameView = null;

    private boolean preventGoBack = true;

    private String partnerNickname = null;
    private String partnerUniqueName = null;

    private ArrayList<OmiPlayer> mPlayerArrayList = null;

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
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                if (!mIsHostGame) {
                    searchingFinished();
                }
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED
                    .equals(action)) {
                if (mIsHostGame) {
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE) == BluetoothAdapter.SCAN_MODE_NONE) {
                        discoverableFinished();
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBtConnectionHandler.removeDisconnectedRemoteConnection(device);
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBtConnectionHandler.addRemoteDeviceIfNotExist(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omi_game);

        mGameLayout = (LinearLayout)findViewById(R.id.gameLayout);
        mPopupLayout = (LinearLayout)findViewById(R.id.popupLayout);
        mPopupLayout.setVisibility(View.GONE);

        mOmiGameView = new OmiGameView(OmiGameActivity.this, this);
        mGameLayout.addView(mOmiGameView);

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
        }else if (requestCode == REQUEST_ENABLE_BT_DISCOVERY) {
            if (resultCode == RESULT_CANCELED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Sorry!");
                builder.setMessage("Your device should discoverable to other devices to join your game.");
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

    @Override
    public void onBackPressed() {

        Log.v(TAG, "onBackPressed..............");

        if (preventGoBack) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure you want to go back?");
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.dismiss();
                }
            });
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.dismiss();
                    preventGoBack = false;
                    if (mIsHostGame) {
                        mBtConnectionHandler.endWorkingAsHost = true;
                    }
                    onBackPressed();
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }else {
            super.onBackPressed();
        }
    }

    private void noBluetoothAndEndGame() {
        finish();
    }

    public void endGame() {
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
        mBtConnectionHandler.setMaxConnections(maxConnCount);
        mBtConnectionHandler.addConnectionListener(this);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        if (mIsHostGame) {
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        }else {
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
        }

        this.registerReceiver(mReceiver, filter);

        if (mIsHostGame) {
            mBtConnectionHandler.workAsHost = true;
            mBtConnectionHandler.startListenIncomingConnections();

            mOmiHostView = new OmiHostView(OmiGameActivity.this, new OmiHostView.OmiHostViewListener() {
                @Override
                public void visibleButtonTapped() {
                    enableDiscoverable();
                }

                @Override
                public void partnerSelected(String partnerName) {
                    partnerSelectedAndPartnersAreReady(partnerName);
                }
            });

            mPopupLayout.addView(mOmiHostView);
            mPopupLayout.setVisibility(View.VISIBLE);
            enableDiscoverable();
        }else {

            mOmiJoinView = new OmiJoinView(OmiGameActivity.this, new OmiJoinView.OmiJoinViewListener() {
                @Override
                public void searchButtonTapped() {
                    searchHostedGames();
                }

                @Override
                public void gameSelected(String gameName) {
                    connectPlayerToHost(gameName);
                }
            });

            mPopupLayout.addView(mOmiJoinView);
            mPopupLayout.setVisibility(View.VISIBLE);
            searchHostedGames();
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
        //nameET.setTextColor(getResources().getColor(R.color.black));
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

    private void changeHostBluetoothNickName() {
        String nickName = SettingsManager.getSetting(Constants.UserKey.NICK_NAME, "", getApplicationContext());
        Log.v(TAG, "Changed to host bt to: " + nickName);
        mBTAdapter.setName(nickName);

        SettingsManager.addSetting(Constants.UserKey.NEW_BT_NAME, nickName, getApplicationContext());
    }

    private void resetHostBluetoothName() {
        String oldBtName = SettingsManager.getSetting(Constants.UserKey.ORI_BT_NAME, "", getApplicationContext());
        Log.v(TAG, "rename hosted bt to: " + oldBtName);
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
        startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT_DISCOVERY);

        if (mOmiHostView != null) {
            mOmiHostView.visibilityStarted();
        }
    }

    private void discoverableFinished() {
        if (mOmiHostView != null) {
            mOmiHostView.visibilityFinished();
        }
    }

    private void updateConnectedPartners() {
        ArrayList<BTConnection> connectedList = mBtConnectionHandler.getConnectionList();

        if (mOmiHostView != null) {
            String[] partners = new String[connectedList.size()];
            int i = 0;
            for (BTConnection btConnection : connectedList) {
                BluetoothDevice device = btConnection.getBtDevice();
                partners[i] = getJoinRemovedName(device.getName());
                i++;
            }

            mOmiHostView.addPartners(partners);
        }

        Log.v(TAG, "Connected count: " + connectedList.size());

        if (connectedList.size() >= maxConnCount) {
            mOmiHostView.showPartnerSelection();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select your partner");
            builder.setMessage("Please select your partner.");
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.dismiss();
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }
    }

    private void joinedConnectionDisconnected(BluetoothDevice device) {
        updateConnectedPartners();
        if (isGameStarted) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Oops!");
            builder.setMessage("One partner's connection lost.");
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.dismiss();
                    isGameStarted = false;
                    endGame();
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }
    }

    private void partnerSelectedAndPartnersAreReady(String partnerName) {
        changeHostBluetoothNickName();

        ArrayList<BTConnection> connectedList = mBtConnectionHandler.getConnectionList();
        for (BTConnection btConnection : connectedList) {
            BluetoothDevice device = btConnection.getBtDevice();
            if (device.getName().contains(partnerName)) {
                partnerUniqueName = device.getAddress();
                partnerNickname = device.getName();
            }
        }

        if (mOmiHostView != null) {
            mPopupLayout.removeView(mOmiHostView);
            mOmiHostView = null;
        }
        mPopupLayout.setVisibility(View.GONE);

        mBtConnectionHandler.stopListenIncomingConnections();
        sendPlayerNames();
    }

    private void sendPlayerNames() {
        String player1 = SettingsManager.getSetting(Constants.UserKey.NICK_NAME, "", getApplicationContext());
        String player3 = partnerNickname;
        String player2 = "";
        String player4 = "";

        if(mPlayerArrayList != null) {
            mPlayerArrayList.clear();
        }else {
            mPlayerArrayList = new ArrayList<>();
        }

        OmiPlayer omiPlayer1 = new OmiPlayer();
        OmiPlayer omiPlayer2 = new OmiPlayer();
        OmiPlayer omiPlayer3 = new OmiPlayer();
        OmiPlayer omiPlayer4 = new OmiPlayer();

        mPlayerArrayList.add(omiPlayer1);
        mPlayerArrayList.add(omiPlayer2);
        mPlayerArrayList.add(omiPlayer3);
        mPlayerArrayList.add(omiPlayer4);

        omiPlayer1.setNickName(player1);
        omiPlayer1.setPlayerNo(1);
        omiPlayer1.setWonCount(0);
        omiPlayer1.setUniqueName("");

        omiPlayer2.setPlayerNo(2);
        omiPlayer2.setWonCount(0);

        omiPlayer3.setPlayerNo(3);
        omiPlayer3.setWonCount(0);
        omiPlayer3.setUniqueName(partnerUniqueName);

        omiPlayer4.setPlayerNo(4);
        omiPlayer4.setWonCount(0);

        ArrayList<BTConnection> connectedList = mBtConnectionHandler.getConnectionList();
        for (BTConnection btConnection : connectedList) {
            BluetoothDevice device = btConnection.getBtDevice();
            if (!device.getName().equalsIgnoreCase(partnerNickname)) {
                if (player2.equalsIgnoreCase("")) {
                    player2 = device.getName();
                    omiPlayer2.setUniqueName(device.getAddress());
                }else {
                    player4 = device.getName();
                    omiPlayer4.setUniqueName(device.getAddress());
                }
            }
        }

        player2 = getJoinRemovedName(player2);
        player3 = getJoinRemovedName(player3);
        player4 = getJoinRemovedName(player4);

        omiPlayer2.setNickName(player2);
        omiPlayer3.setNickName(player3);
        omiPlayer4.setNickName(player4);

        BTDataPacket btDataPacket = new BTDataPacket();
        btDataPacket.setOpCode(Constants.OpCodes.OPCODE_SET_PLAYER_NAMES);

        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"" + Constants.OmiJsonKey.PLAYER_NAME_1_KEY + "\":\"" + player1 + "\",");
        stringBuilder.append("\"" + Constants.OmiJsonKey.PLAYER_NAME_2_KEY + "\":\"" + player2 + "\",");
        stringBuilder.append("\"" + Constants.OmiJsonKey.PLAYER_NAME_3_KEY + "\":\"" + player3 + "\",");
        stringBuilder.append("\"" + Constants.OmiJsonKey.PLAYER_NAME_4_KEY + "\":\"" + player4 + "\"}");

        String jsonBody = stringBuilder.toString();

        btDataPacket.setBody(jsonBody);
        sendCommandToAllConnections(btDataPacket);

        mOmiGameView.myName = player1;

        JSONObject nameJson = null;
        try {
            nameJson = new JSONObject(jsonBody);
        }catch (Exception e) {}

        if (nameJson != null) {
            mOmiGameView.setPlayerNames(nameJson);
        }

        sendStartCommand();
    }

    private String getJoinRemovedName(String name) {
        if (name == null) {
            return "";
        }
        String joinStr = "Join Omi - ";
        if (name.contains(joinStr)) {
            name = name.replace(joinStr,"");
        }
        return name;
    }

    private void sendStartCommand() {
        BTDataPacket btDataPacket = new BTDataPacket();
        btDataPacket.setOpCode(Constants.OpCodes.OPCODE_START_GAME);
        sendCommandToAllConnections(btDataPacket);

        startHostedGame();
    }

    private void startHostedGame() {
        isGameStarted = true;
    }

    private void testWriteToAll() {
        BTDataPacket btDataPacket = new BTDataPacket();
        btDataPacket.setOpCode(Constants.OpCodes.OPCODE_NONE);
        btDataPacket.setBody("Test");
        sendCommandToAllConnections(btDataPacket);
    }

    //host data handling



    private void sendCommandToAllConnections(BTDataPacket btDataPacket) {
        ArrayList<BTConnection> connectedList = mBtConnectionHandler.getConnectionList();
        for (BTConnection btConnection : connectedList) {
            btConnection.getBtConnectedThread().write(btDataPacket.getBuffer());
        }
    }

    private void sendCommandToPlayer(OmiPlayer player, BTDataPacket btDataPacket) {
        ArrayList<BTConnection> connectedList = mBtConnectionHandler.getConnectionList();
        for (BTConnection btConnection : connectedList) {
            if (btConnection.getBtDevice().getAddress().equalsIgnoreCase(player.getUniqueName())) {
                btConnection.getBtConnectedThread().write(btDataPacket.getBuffer());
                break;
            }
        }
    }

    //host received command handling
    private void playerPlayedCard(OmiPlayer player, BTDataPacket btDataPacket) {
        mOmiGameView.playerPlayedCard(player, btDataPacket);

        for (OmiPlayer omiPlayer : mPlayerArrayList) {
            if (omiPlayer.getPlayerNo() != 1 && omiPlayer.getPlayerNo() != player.getPlayerNo()) {
                sendCommandToPlayer(omiPlayer, btDataPacket);
            }
        }
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
        Log.v(TAG, "rename joined bt to: " + oldBtName);
        if (oldBtName.length() > 0) {
            mBTAdapter.setName(oldBtName);
        }

        SettingsManager.addSetting(Constants.UserKey.ORI_BT_NAME, "", getApplicationContext());
        SettingsManager.addSetting(Constants.UserKey.NEW_BT_NAME, "", getApplicationContext());
    }

    private void searchHostedGames() {
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
        }

        mBTAdapter.startDiscovery();
        if (mOmiJoinView != null) {
            mOmiJoinView.searchStarted();
        }
    }

    private void searchingFinished() {
        if (mOmiJoinView != null && mOmiJoinView.getVisibility() == View.VISIBLE) {
            mOmiJoinView.searchFinished();
        }
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
        if (mOmiJoinView != null) {
            String[] hostedGames = new String[discoveredList.size()];
            int i = 0;
            for (BluetoothDevice device : discoveredList) {
                hostedGames[i] = device.getName();
                i++;
            }

            mOmiJoinView.addGames(hostedGames);
        }
    }

    private void connectPlayerToHost(String hostName) {

        Log.v(TAG, "connectPlayerToHost: " + hostName);

        BluetoothDevice gameDevice = null;
        for (BluetoothDevice device : discoveredList) {
            if(device.getName().equalsIgnoreCase(hostName)) {
                gameDevice = device;
                break;
            }
        }

        if (gameDevice != null) {
            mBtConnectionHandler.connectDevice(gameDevice);
        }else {
            Log.v(TAG, "connectPlayerToHost: " + "host not found");
        }
    }

    private void connectedToHostedGame() {
        if (mOmiJoinView != null) {
            mPopupLayout.removeView(mOmiJoinView);
            mOmiJoinView = null;
        }
        mPopupLayout.setVisibility(View.GONE);

        CharSequence text = "Join game succeeded. Please wait until others joined.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();

        mOmiGameView.myName = BluetoothAdapter.getDefaultAdapter().getName();
    }

    private void deviceNotConnectedToHost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sorry!");
        builder.setMessage("Your device not connected to game. Please try again.");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }

    private void joinedGameDisconnected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Oops!");
        builder.setMessage("Game connection lost.");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
                if (!isJoinedToGame) {
                    endGame();
                }
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }

    private void startJoinedGame() {
        isGameStarted = true;
        //TODO
    }

    private void writeDataToHost(BTDataPacket btDataPacket) {
        ArrayList<BTConnection> connectedList = mBtConnectionHandler.getConnectionList();

        for (BTConnection btConnection : connectedList) {
            btConnection.getBtConnectedThread().write(btDataPacket.getBuffer());
        }
    }



    private void playerPlayedCard(BTDataPacket btDataPacket) {
        JSONObject bodyObj = btDataPacket.getBodyAsJson();
        int playerNo = bodyObj.optInt(Constants.OmiJsonKey.PLAYER_NUMBER_KEY);
        for (OmiPlayer omiPlayer : mPlayerArrayList) {
            if (playerNo == omiPlayer.getPlayerNo()) {
                mOmiGameView.playerPlayedCard(omiPlayer, btDataPacket);
                break;
            }
        }
    }


    //Host data handling
    private void handleReceivedData(BTConnection btConnection,  byte[] buffer) {

        OmiPlayer omiPlayer = null;
        BluetoothDevice device = btConnection.getBtDevice();

        for (OmiPlayer player : mPlayerArrayList) {
            if (device.getAddress().equalsIgnoreCase(player.getUniqueName())) {
                omiPlayer = player;
                break;
            }
        }


        //TODO

        BTDataPacket btDataPacket = new BTDataPacket(buffer);
        switch (btDataPacket.getOpCode()) {
            case Constants.OpCodes.OPCODE_NONE :
            {

            }
            break;
            case Constants.OpCodes.OPCODE_PLAYER_SELECTED_TRUMPS:
            {

            }
            break;
            case Constants.OpCodes.OPCODE_PLAYER_PLAYED_CARD:
            {
                playerPlayedCard(omiPlayer, btDataPacket);
            }
            break;
            default:
                break;
        }
    }

    //Join data
    private void handleReceivedData(byte[] buffer) {

        //TODO

        BTDataPacket btDataPacket = new BTDataPacket(buffer);
        switch (btDataPacket.getOpCode()) {
            case Constants.OpCodes.OPCODE_NONE :
            {

            }
            break;
            case Constants.OpCodes.OPCODE_SET_PLAYER_NAMES :
            {
                if (mOmiGameView != null) {
                    mOmiGameView.setPlayerNames(btDataPacket.getJsonObject().optJSONObject(BTDataPacket.BODY));
                }
            }
            break;
            case Constants.OpCodes.OPCODE_START_GAME :
            {
                startJoinedGame();
            }
            break;
            case Constants.OpCodes.OPCODE_PLAYER_PLAYED_CARD:
            {
                playerPlayedCard(btDataPacket);
            }
            break;
            default:
                break;
        }
    }

    /*
	 * BTConnectionListener methods
	 */
    @Override
    public void connectionEstablished(BTConnection connection) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mIsHostGame) {
                    updateConnectedPartners();
                }else {
                    isJoinedToGame = true;
                    connectedToHostedGame();
                }
            }
        });
    }

    @Override
    public void connectionDisconnected(BluetoothDevice device) {
        final BluetoothDevice fDevice = device;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mIsHostGame) {
                    joinedConnectionDisconnected(fDevice);
                }else {
                    if (isJoinedToGame) {
                        isJoinedToGame = false;
                        joinedGameDisconnected();
                    }
                }
            }
        });
    }

    @Override
    public void deviceNotConnected(BluetoothDevice device) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!mIsHostGame) {
                    deviceNotConnectedToHost();
                }
            }
        });
    }

    @Override
    public void dataReceived(final BTConnection connection, final byte[] buffer) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.v(TAG, "dataReceived...................");

                if (mIsHostGame) {
                    handleReceivedData(connection, buffer);
                }else {
                    //handle incoming data (commands)
                    handleReceivedData(buffer);
                }
            }
        });
    }

    @Override
    public void dataDidSend(BTConnection connection) {

        final BTConnection conn = connection;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Data did send");
                if(conn != null){
                    Log.v(TAG, "Data did send to: " + conn.getBtDevice().getName());
                }
            }
        });
    }

    @Override
    public void dataDidNotSend(BTConnection connection, byte[] buffer) {
        final BTConnection conn = connection;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Data did not send");
                if(conn != null){
                    Log.v(TAG, "Data did not send to: " + conn.getBtDevice().getName());
                }
            }
        });
    }

    /*
	 * OmiGameViewListener methods
	 */
    @Override
    public void playerIsShufflingPack(int playerNo) {

    }

    @Override
    public void playerIsSelectingTrumps(int playerNo) {

    }

    @Override
    public void playerDidSelectTrumps(int playerNo, Constants.OmiSuit suit, boolean isFromNextHand) {

    }

    @Override
    public void playerDidPlayACard(int playerNo, int cardNo, int option) {

    }


}
