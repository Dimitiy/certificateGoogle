package com.inet.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;

/**
 * Class show dialog
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
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		Intent intent = getIntent();
//	        String text = "";
//	        if(intent.hasExtra("text")) text = intent.getStringExtra("text");
	        
	        AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        alert.setTitle("Attention!!!");
			alert.setMessage("Reenter account number!");

			final EditText input = new EditText(this);
			alert.setView(input);
	        alert.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString();

					Logging.doLog(LOG_TAG, "new account: " + value, "new account: " + value);

					ed = sp.edit();
					ed.putString("account", value);
					ed.commit();
					finish();
				}
			});
	        alert.setNegativeButton("Later",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// Canceled.
							finish();
						}
					});
	        alert.show();
	}
	
	public boolean showAccountDialog() {
		sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

		alert.setTitle("Attention!!!");
		alert.setMessage("Enter account number!");

		final EditText input = new EditText(ctx);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				Logging.doLog(LOG_TAG, "new account: " + value, "new account: " + value);

				ed = sp.edit();
				ed.putString("account", value);
				ed.commit();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
//						finish();
					}
				});

		alert.show();
		return true;
	}
}
