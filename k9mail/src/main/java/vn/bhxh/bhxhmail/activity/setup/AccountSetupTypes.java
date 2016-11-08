package vn.bhxh.bhxhmail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import vn.bhxh.bhxhmail.activity.K9Activity;

/**
 * Created by viethoavnm on 9/29/2016.
 */

public class AccountSetupTypes extends K9Activity {

    TextView mTitle;
    ImageView mBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(vn.bhxh.bhxhmail.R.layout.account_setup_types);
        mTitle = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.common_title);
        mBack = (ImageView) findViewById(vn.bhxh.bhxhmail.R.id.common_ic_back);
        mTitle.setText(getString(vn.bhxh.bhxhmail.R.string.c_login_imail));
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void onLogin(View view) {
        Intent intent = new Intent(this, AccountSetupBasics.class);
        intent.putExtra("IS_MANUAL", false);
        startActivity(intent);
    }
    public void onLoginGmail(View view) {
        Intent intent = new Intent(this, AccountSetupBasics.class);
        intent.putExtra("IS_MANUAL", false);
        intent.putExtra("IS_GMAIL", true);
        startActivity(intent);
    }

    public void onLoginManual(View view) {
        Intent intent = new Intent(this, AccountSetupBasics.class);
        intent.putExtra("IS_MANUAL", true);
        startActivity(intent);
    }

    public static void actionNewAccount(Context context) {
        Intent i = new Intent(context, AccountSetupTypes.class);
        context.startActivity(i);
    }


}
