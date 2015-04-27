package com.android.systemui.usb;

import java.util.ArrayList;

import amigo.app.AmigoActionBar;
import amigo.app.AmigoActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import amigo.widget.AmigoButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import amigo.widget.AmigoTextView;

import com.android.systemui.R;

//import amigo.app.AmigoActivity;
//import amigo.widget.AmigoListView;
import amigo.changecolors.ChameleonColorManager;
import amigo.preference.AmigoPreferenceActivity;
import amigo.preference.AmigoPreferenceScreen;
import amigo.preference.AmigoPreference;

public class MtpHelpActivity extends AmigoPreferenceActivity implements OnClickListener {

	private static final int WIN_XP = 0;
	private static final int WIN_VISTA = 1;
	private static final int LINUX = 2;


    private static final String KEY_WINXP = "winxp";
    private static final String KEY_WINVISTA = "winvista";
    private static final String KEY_LINUX = "linux";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
		setContentView(R.layout.gn_mtp_navi_page);

        addPreferencesFromResource(R.xml.gn_mtp_help_prefs);

		AmigoButton cancelButton = (AmigoButton) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new OnCancelButtonClick());
		
		AmigoActionBar actionBar = getAmigoActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
	}

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

	@Override
	public void onClick(View view) {
	}
    @Override
    public boolean onPreferenceTreeClick(AmigoPreferenceScreen preferenceScreen, AmigoPreference preference) {
        String curkey = preference.getKey();
		int os = 0;
		if (curkey.equals(KEY_WINXP)) {
			os = WIN_XP;
		} else if (curkey.equals(KEY_WINVISTA)) {
			os = WIN_VISTA;
		} else if (curkey.equals(KEY_LINUX)) {
			os = LINUX;
		}
		Intent intent = new Intent("gn.intent.action.MTP_HELP_DETAIL");
		intent.putExtra("position", os);
		startActivity(intent);
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }	
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class OnCancelButtonClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
