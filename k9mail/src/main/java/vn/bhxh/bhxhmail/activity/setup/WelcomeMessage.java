package vn.bhxh.bhxhmail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import vn.bhxh.bhxhmail.activity.Accounts;
import vn.bhxh.bhxhmail.activity.K9Activity;
import vn.bhxh.bhxhmail.helper.HtmlConverter;

/**
 * Displays a welcome message when no accounts have been created yet.
 */
public class WelcomeMessage extends K9Activity implements OnClickListener{

    public static void showWelcomeMessage(Context context) {
        Intent intent = new Intent(context, WelcomeMessage.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(vn.bhxh.bhxhmail.R.layout.welcome_message);

        TextView welcome = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.welcome_message);
        welcome.setText(HtmlConverter.htmlToSpanned(getString(vn.bhxh.bhxhmail.R.string.accounts_welcome)));
        welcome.setMovementMethod(LinkMovementMethod.getInstance());

        findViewById(vn.bhxh.bhxhmail.R.id.next).setOnClickListener(this);
        findViewById(vn.bhxh.bhxhmail.R.id.import_settings).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case vn.bhxh.bhxhmail.R.id.next: {
                AccountSetupBasics.actionNewAccount(this);
                finish();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.import_settings: {
                Accounts.importSettings(this);
                finish();
                break;
            }
        }
    }
}
