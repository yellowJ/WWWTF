package com.hasoji.poke.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hasoji.poke.R;
import com.hasoji.poke.api.ApiHolder;
import com.hasoji.poke.api.PokemonClient;
import com.hasoji.poke.log.LocationLogger;
import com.hasoji.poke.log.Logger;
import com.hasoji.poke.services.PokeTaskService;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Data.PlayerDataOuterClass;

public class MainActivity extends Activity implements Logger.LogChangeListener, LocationLogger.LocationChangeListener {
    private View editView;
    private EditText latEdit;
    private EditText lngEdit;
    private TextView locationText;
    private ScrollView logScroll;
    private TextView logTextView;
    private View loginLayout;
    private String loginTag;
    private EditText pkmNameEdit;
    private TextView targetText;

    private void appendPlayerProfile() {
        runOnUiThread(new Runnable() {
            public void run() {
                MainActivity.this.loginLayout.setVisibility(View.GONE);
//                MainActivity.this.findViewById(2131296273).setVisibility(0);
            }
        });
        new Thread() {
            public void run() {
                PlayerProfile localPlayerProfile = ApiHolder.getInstance().sharePokemonClient(MainActivity.this.loginTag).getGo().getPlayerProfile();
                try {
                    PlayerDataOuterClass.PlayerData localPlayerData = localPlayerProfile.getPlayerData();
                    Logger.getInstance().appendLog("username : " + localPlayerData.getUsername());
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void showLocationDialog() {
        if (this.editView == null) {
//            this.editView = LayoutInflater.from(this).inflate(2130968581, null);
//            this.latEdit = ((EditText)this.editView.findViewById(2131296285));
//            this.lngEdit = ((EditText)this.editView.findViewById(2131296286));
//            this.pkmNameEdit = ((EditText)this.editView.findViewById(2131296287));
        }
        this.latEdit.setText("");
        this.lngEdit.setText("");
        ((ViewGroup)this.editView.getParent()).removeAllViews();
        new AlertDialog.Builder(this).setTitle("开飞机啦").setView(this.editView).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                double d1 = Double.parseDouble(MainActivity.this.latEdit.getText().toString());
                double d2 = Double.parseDouble(MainActivity.this.lngEdit.getText().toString());
                String str = MainActivity.this.pkmNameEdit.getText().toString();
                ApiHolder.getInstance().sharePokemonClient(MainActivity.this.loginTag).catchPokemonByLocation(Double.valueOf(d1), Double.valueOf(d2), str);
            }}).setNegativeButton("取消", null).create().show();
    }

    private void showToast(final String paramString) {
        runOnUiThread(new Runnable() {
            public void run() {
                Logger.getInstance().appendLog(paramString);
                Toast.makeText(MainActivity.this, paramString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
//        setContentView(2130968577);
//        Logger.getInstance().setLogChangeListener(this);
//        LocationLogger.getInstance().setLocationChangeListener(this);
//        this.loginLayout = findViewById(2131296262);
//        this.logTextView = ((TextView)findViewById(2131296270));
//        this.logScroll = ((ScrollView)findViewById(2131296268));
//        this.locationText = ((TextView)findViewById(2131296271));
//        this.targetText = ((TextView)findViewById(2131296272));
//        findViewById(2131296266).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View paramAnonymousView) {
//                EditText localEditText1 = (EditText)MainActivity.this.findViewById(2131296264);
//                EditText localEditText2 = (EditText)MainActivity.this.findViewById(2131296265);
//                String str1 = localEditText1.getText().toString();
//                String str2 = localEditText2.getText().toString();
//                if (PokeTaskService.INSTANCE != null) {
//                    PokeTaskService.INSTANCE.login(str1, str2, new PokemonClient.OnLoginResultListener() {
//                        public void onLoginFinish(Exception paramAnonymous2Exception) {
//                            if (paramAnonymous2Exception == null) {
//                                MainActivity.this.showToast("登录成功");
//                                MainActivity.this.appendPlayerProfile();
//                                return;
//                            }
//                            MainActivity.this.showToast("登录失败 , " + paramAnonymous2Exception.getMessage());
//                            paramAnonymous2Exception.printStackTrace();
//                        }
//                    });
//                    MainActivity.access$202(MainActivity.this, str1);
//                }
//            }
//        });
//        findViewById(2131296267).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View paramAnonymousView) {}
//        });
//        findViewById(2131296276).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View paramAnonymousView) {
//                MainActivity.this.showLocationDialog();
//            }
//        });
//        findViewById(2131296274).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View paramAnonymousView) {
//                PlayerProfile localPlayerProfile = ApiHolder.getInstance().shareTopClient().getPlayerProfile();
//                try {
//                    Stats localStats = localPlayerProfile.getStats();
//                    int i = localStats.getLevel();
//                    long l1 = localStats.getExperience();
//                    long l2 = localStats.getNextLevelXp();
//                    long l3 = localStats.getPrevLevelXp();
//                    Toast.makeText(MainActivity.this, localPlayerProfile.getPlayerData().getUsername() + " Lv." + i + " ( " + (l1 - l3) + " , " + (l2 - l3) + " ) " + " " + 100L * (l1 - l3) / (l2 - l3) + "%", 0).show();
//                    return;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return;
//                }
//            }
//        });
//        findViewById(2131296275).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View paramAnonymousView) {
//                MainActivity.this.startActivity(new Intent(MainActivity.this.getBaseContext(), InventoryActivity.class));
//            }
//        });
//        if (ApiHolder.getInstance().shareTopClient() != null) {
//            this.loginLayout.setVisibility(8);
//            findViewById(2131296273).setVisibility(0);
//            this.loginTag = ApiHolder.getInstance().shareTopClient().getTag();
//            onLogChanged();
//        }
    }

    public void onLocationChanged(final double paramDouble1, final double paramDouble2) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (MainActivity.this.locationText != null)
                    MainActivity.this.locationText.setText(paramDouble1 + "\n" + paramDouble2);
            }
        });
    }

    public void onLogAppend(final String paramString) {
        runOnUiThread(new Runnable() {
            public void run() {
                MainActivity.this.logTextView.append(paramString);
                MainActivity.this.logScroll.fullScroll(130);
            }
        });
    }

    public void onLogChanged() {
        runOnUiThread(new Runnable() {
            public void run() {
                MainActivity.this.logTextView.setText(Logger.getInstance().getLog());
            }
        });
    }

    public void onTargetDistanceChanged(final String paramString, final double paramDouble) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (MainActivity.this.targetText != null)
                    MainActivity.this.targetText.setText("目标 : " + paramString + "\n" + paramDouble + " m");
            }
        });
    }
}
