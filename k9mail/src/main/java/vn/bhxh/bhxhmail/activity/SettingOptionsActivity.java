package vn.bhxh.bhxhmail.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.Identity;
import vn.bhxh.bhxhmail.Preferences;
import vn.bhxh.bhxhmail.R;
import vn.bhxh.bhxhmail.activity.compose.MessageActions;

/**
 * Created by viethoavnm on 10/12/2016.
 */
public class SettingOptionsActivity extends K9Activity implements View.OnClickListener {
    private ImageView mBack;
    private TextView mVersion;
    private TextView mRefresh;
    private String version;
    private Switch mSignSw;
    private EditText mSignEd;
    private int mIdentityIndex;
    private Identity mIdentity;
    private Account mAccount;
    private String accountUuid;
    private TextView mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(vn.bhxh.bhxhmail.R.layout.activity_setting_options);

        mSignSw = (Switch) findViewById(vn.bhxh.bhxhmail.R.id.setting_user_sign);
        mSignEd = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.setting_sign);
        mTitle = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.common_title);
        mTitle.setText(vn.bhxh.bhxhmail.R.string.setting);

        accountUuid = getIntent().getStringExtra(EditIdentity.EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mIdentityIndex = getIntent().getIntExtra(EditIdentity.EXTRA_IDENTITY_INDEX, -1);
        mIdentity = mAccount.getIdentity(mIdentityIndex);
        if (mIdentityIndex == -1) {
            mIdentity = new Identity();
        } else {
            if (mIdentity != null)
                if (mIdentity.getSignatureUse()) {
                    mSignEd.setText(mIdentity.getSignature());
                    mSignSw.setChecked(mIdentity.getSignatureUse());
                    mSignEd.setVisibility(View.VISIBLE);
                }
        }
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBack = (ImageView) findViewById(vn.bhxh.bhxhmail.R.id.common_ic_back);
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(vn.bhxh.bhxhmail.R.id.setting_language).setOnClickListener(this);
        findViewById(vn.bhxh.bhxhmail.R.id.setting_rate).setOnClickListener(this);
        findViewById(vn.bhxh.bhxhmail.R.id.setting_feedback).setOnClickListener(this);
        findViewById(vn.bhxh.bhxhmail.R.id.setting_licence).setOnClickListener(this);
        mVersion = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.setting_version);
        mVersion.setText(String.format(getString(vn.bhxh.bhxhmail.R.string.setting_version), version));

        mSignSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSignEd.setVisibility(View.VISIBLE);
                    mIdentity.setSignatureUse(true);
                } else {
                    mSignEd.setVisibility(View.GONE);
                    mIdentity.setSignatureUse(false);
                }
            }
        });

        mRefresh = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.auto_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerForContextMenu(mRefresh);
                openContextMenu(mRefresh);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Tự động làm mới sau: ");
        menu.add(0, v.getId(), 0, "10 phút");//groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "30 phút");
        menu.add(0, v.getId(), 0, "1 giờ");
        menu.add(0, v.getId(), 0, "12 ");
        menu.add(0, v.getId(), 0, "Bỏ qua");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Toast.makeText(getApplicationContext(), "Thiết lập thành công", Toast.LENGTH_LONG).show();
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case vn.bhxh.bhxhmail.R.id.setting_rate:
                rateApp();
                break;
            case vn.bhxh.bhxhmail.R.id.setting_language:
                selectLanguages();
                break;
            case vn.bhxh.bhxhmail.R.id.setting_feedback:
                onFeedback();
                break;
            default:
                break;
        }
    }

    protected void rateApp() {
        String appPackage = this.getPackageName();
        String url = "market://details?id=" + appPackage;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void onFeedback() {
        MessageActions.actionCompose(this, mAccount, true);
    }

    @Override
    public void onBackPressed() {
        if (mIdentity.getSignatureUse())
            mIdentity.setSignature(mSignEd.getText().toString());
        saveIdentity();
    }

    private void selectLanguages() {
        String languages[] = getResources().getStringArray(vn.bhxh.bhxhmail.R.array.languages_available);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = this.getLayoutInflater().inflate(vn.bhxh.bhxhmail.R.layout.dialog_language, null);
        ListView listView = (ListView) view.findViewById(vn.bhxh.bhxhmail.R.id.dialog_list_lang);
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                languages));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeLanguage(position);
            }
        });
        builder.setView(view);
        builder.setNegativeButton(vn.bhxh.bhxhmail.R.string.cancel_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void changeLanguage(int i) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        switch (i) {
            case 0:
                conf.locale = Locale.ENGLISH;
                break;
            default:
                conf.locale = new Locale("vi");
                break;
        }
        Locale.setDefault(conf.locale);
        res.updateConfiguration(conf, dm);
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        prefs.edit().putInt("Lang", i).commit();
        onConfigurationChanged(conf);
    }

    private void saveIdentity() {
        List<Identity> identities = mAccount.getIdentities();
        if (mIdentityIndex == -1) {
            identities.add(mIdentity);
        } else {
            identities.remove(mIdentityIndex);
            identities.add(mIdentityIndex, mIdentity);
        }
        mAccount.save(Preferences.getPreferences(getApplication().getApplicationContext()));
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Intent intent = new Intent(this, SettingOptionsActivity.class);
        intent.putExtra(EditIdentity.EXTRA_ACCOUNT, mAccount.getUuid());
        intent.putExtra(EditIdentity.EXTRA_IDENTITY_INDEX, mIdentityIndex);
        startActivity(intent);
        finish();
    }
}
