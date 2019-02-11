package com.jantzapps.jantz.xmltranslatorfree;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class InstructionActivity extends AppCompatActivity {

    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);
        TextView yandexLink =(TextView)findViewById(R.id.yandex_link);
        yandexLink.setClickable(true);
        yandexLink.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://translate.yandex.com/'>\""+getString(R.string.powered_by_yandex_translate)+"\"</a>";
        yandexLink.setText(Html.fromHtml(text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_home:
                Intent homeIntent = new Intent(InstructionActivity.this, MainActivity.class);
                InstructionActivity.this.startActivity(homeIntent);
                finish();
                return true;
            case R.id.action_disclaimer:
                Intent disclaimerIntent = new Intent(InstructionActivity.this, DisclaimerActivity.class);
                InstructionActivity.this.startActivity(disclaimerIntent);
                finish();
                return true;

            default:

        }
        return super.onMenuItemSelected(item.getItemId(),item);
    }
}

