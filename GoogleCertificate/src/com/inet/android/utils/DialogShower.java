package com.inet.android.utils;

import com.inet.android.certificate.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.widget.EditText;

/**
 * Class show dialog
 * 
 * @author johny homicide
 *
 */
public class DialogShower extends Activity {
	public final String LOG_TAG = "dialogShower";
	SharedPreferences sp;
	Editor ed;
	Context ctx;

	public DialogShower(Context ctx) {
		this.ctx = ctx;
	}

	public DialogShower() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Logging.doLog(LOG_TAG, "show dialog");

		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		// Intent intent = getIntent();
		ContextThemeWrapper themedContext;
		if (Build.VERSION.SDK_INT >= 14) {
			themedContext = new ContextThemeWrapper(ctx,
					android.R.style.Theme_DeviceDefault_Dialog);
		} else {
			themedContext = new ContextThemeWrapper(ctx,
					android.R.style.Theme_Dialog);
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(themedContext);
		alert.setTitle(R.string.TitleDialog);
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setMessage(R.string.reenter_account_number);

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setMaxLines(1);

		alert.setView(input);
		alert.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();

						Logging.doLog(LOG_TAG, "new account: " + value,
								"new account: " + value);

						ed = sp.edit();
						ed.putString("account", value);
						ed.commit();
						finish();
					}
				});
		alert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
				finish();
			}
		});
		final AlertDialog dialog = alert.create();

		dialog.show();
		if (input.getText().toString().length() == 0) {
			((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
					.setEnabled(false);
		} else {
			((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
					.setEnabled(true);
		}

		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (input.getText().toString().length() == 0) {

				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (input.getText().toString().length() == 0) {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (input.getText().toString().length() == 0) {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					((AlertDialog) dialog).getButton(
							AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
		});
	}

	public boolean showAccountDialog() {
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		ContextThemeWrapper themedContext;
		if (Build.VERSION.SDK_INT >= 14) {
			themedContext = new ContextThemeWrapper(this,
					android.R.style.Theme_DeviceDefault_Dialog);
		} else {
			themedContext = new ContextThemeWrapper(ctx,
					android.R.style.Theme_Dialog);
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(themedContext);
		alert.setTitle(R.string.TitleDialog);
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setMessage(R.string.Enter_account);

		final EditText input = new EditText(ctx);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setMaxLines(1);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				Logging.doLog(LOG_TAG, "new account: " + value, "new account: "
						+ value);

				ed = sp.edit();
				ed.putString("account", value);
				ed.commit();
			}
		});

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						// finish();
					}
				});

		alert.show();
		return true;
	}
}
