package com.android.systemui.usb;

import amigo.app.AmigoActionBar;
import amigo.app.AmigoActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import amigo.widget.AmigoButton;
import amigo.widget.AmigoCheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import amigo.changecolors.ChameleonColorManager;

import com.android.systemui.R;

public class MtpHelpDetailActivity extends AmigoActivity {
	private static final String TAG = "MtpHelpDetailActivity";

	private static final int WIN_XP = 0;
	private static final int WIN_VISTA = 1;
	private static final int LINUX = 2;
	private static final int WIN_XP_SUCCESS = WIN_VISTA;

	private int mCurrentOs = 0;

	LayoutInflater mInflater;
	LinearLayout mContentLayout;
	LinearLayout mButtonLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
		setContentView(R.layout.gn_mtp_help_page);

		mInflater = LayoutInflater.from(this);
		mContentLayout = (LinearLayout) findViewById(R.id.container);
		mButtonLayout = (LinearLayout) findViewById(R.id.button_field);

		Intent intent = getIntent();
		mCurrentOs = intent.getIntExtra("position", 0);

		initViews();

		AmigoActionBar actionBar = getAmigoActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayOptions(AmigoActionBar.DISPLAY_SHOW_CUSTOM, AmigoActionBar.DISPLAY_SHOW_HOME
				| AmigoActionBar.DISPLAY_SHOW_CUSTOM);
	}

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }
    
	/**
	 * MTP help page: Title field Content field Button field Content field and Button field are
	 * inflate dynamically
	 * 
	 * @param
	 * @return void
	 * */
	void initViews() {
		// Title string
		int[] titleId = new int[] { R.string.mtp_winxp_name, R.string.mtp_winvista_name,
				R.string.mtp_linux_ubuntu };
		// Content layouts
		int[] contentLayoutIds = new int[] { R.layout.gn_mtp_winxp_detail, R.layout.gn_mtp_vista_detail,
				R.layout.gn_mtp_linux_detail };
		// Button layouts
		int[] buttonLayouts = new int[] { R.layout.gn_mtp_win_buttons, R.layout.gn_mtp_finish_button,
				R.layout.gn_mtp_finish_button };

		// Inflate title field
		TextView title = (TextView) findViewById(R.id.os_title);
		title.setText(titleId[mCurrentOs]);

		// Inflate Content field
		addView(mContentLayout, contentLayoutIds[mCurrentOs]);
		// Inflate Button field
		addView(mButtonLayout, buttonLayouts[mCurrentOs]);

		switch (mCurrentOs) {
		case WIN_XP:
			initWinXpView();
			break;
		case WIN_VISTA:
			initWinVistaView();
			break;
		case LINUX:
			initLinuxView();
			break;
		default:
			break;
		}
	}

	/**
	 * add view by resource id
	 * 
	 * @param target
	 * @param id
	 */
	void addView(LinearLayout target, int id) {
		LinearLayout layout = (LinearLayout) mInflater.inflate(id, null);
		// Need to load the layout parameters from parent view
		layout.setLayoutParams(target.getLayoutParams());
		target.removeAllViews();
		target.addView(layout);
	}

	/**
	 * init mtp help information for Win XP
	 */
	void initWinXpView() {
		// Add web site field
		TextView website = (TextView) findViewById(R.id.website);
		String url = "<a href='http://mtp.amigo.cn'>http://mtp.amigo.cn</a>";
		website.setText(Html.fromHtml(url));
		website.setMovementMethod(LinkMovementMethod.getInstance());

		// add cancel button event
		AmigoButton cancelButton = (AmigoButton) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});

		// add next button event, if next button is clicked, then go to finish install MTP driver
		// page, so we need re-inflate the views
		final AmigoButton nextButton = (AmigoButton) findViewById(R.id.next);
		nextButton.setEnabled(false);
		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				addView(mContentLayout, R.layout.gn_mtp_winxp_finished);
				addView(mButtonLayout, R.layout.gn_mtp_finish_button);

				// Add re-install drive event
				String driverName = getString(R.string.mtp_winxp_reinstall_driver);
				TextView installView = (TextView) findViewById(R.id.winxp_driver_name);
				installView.setText(Html.fromHtml(driverName));
				installView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						addView(mContentLayout, R.layout.gn_mtp_winxp_detail);
						addView(mButtonLayout, R.layout.gn_mtp_win_buttons);
						initWinXpView();
					}
				});

				// Add finish button click event
				AmigoButton finishButton = (AmigoButton) findViewById(R.id.finish);
				finishButton.setOnClickListener(mOnFinishButtonClickListener);
			}
		});

		// Add checkbox event, if it's checked, the next button could click
		final AmigoCheckBox checkBox = (AmigoCheckBox) findViewById(R.id.confirm);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				if (isChecked) {
					nextButton.setEnabled(true);
				} else {
					nextButton.setEnabled(false);
				}
				checkBox.setChecked(isChecked);
			}
		});
	}

	/**
	 * Init mtp help information for Win Vista
	 */
	void initWinVistaView() {
		AmigoButton finishButton = (AmigoButton) findViewById(R.id.finish);
		finishButton.setOnClickListener(mOnFinishButtonClickListener);
	}

	/**
	 * Init mtp help information for Linux
	 */
	void initLinuxView() {
		AmigoButton finishButton = (AmigoButton) findViewById(R.id.finish);
		finishButton.setOnClickListener(mOnFinishButtonClickListener);
	}

	/**
	 * OnFinishButtonClicked, it's a common event to finish the help page
	 */
	private OnClickListener mOnFinishButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			finish();
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}