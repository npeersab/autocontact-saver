package com.example.contact

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.core.content.ContextCompat

object ContactHelper {
    private const val TAG = "ContactHelper"

    data class ContactLookupResult(
        val exists: Boolean,
        val contactId: Long? = null,
        val displayName: String? = null,
        val lookupKey: String? = null,
        val permissionMissing: Boolean = false,
        val details: String? = null
    )

    /**
     * Checks if READ_CONTACTS permission is granted.
     */
    fun hasReadContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if WRITE_CONTACTS permission is granted.
     */
    fun hasWriteContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Normalizes a phone number for comparison by extracting standard digits and + prefix.
     */
    fun normalizePhoneNumber(number: String): String {
        return number.filter { it.isDigit() || it == '+' }
    }

    /**
     * Checks if a phone number already exists in device contacts provider (ContactsContract).
     * Returns lookup result with contact details if found.
     */
    fun findContactByNumber(context: Context, rawNumber: String): ContactLookupResult {
        if (rawNumber.isBlank()) return ContactLookupResult(exists = false)

        if (!hasReadContactsPermission(context)) {
            Log.w(TAG, "READ_CONTACTS permission not granted. Cannot query contacts provider.")
            return ContactLookupResult(
                exists = false,
                permissionMissing = true,
                details = "READ_CONTACTS permission missing"
            )
        }

        val cleanNumber = rawNumber.trim()

        // 1. Primary check: ContactsContract.PhoneLookup filter URI (Fast and handles telephony variations)
        try {
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(cleanNumber)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.LOOKUP_KEY,
                ContactsContract.PhoneLookup.NUMBER
            )

            context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val keyIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY)

                    val id = if (idIndex != -1) cursor.getLong(idIndex) else null
                    val name = if (nameIndex != -1) cursor.getString(nameIndex) else null
                    val key = if (keyIndex != -1) cursor.getString(keyIndex) else null

                    Log.d(TAG, "Found contact in PhoneLookup: ID=$id, Name=$name")
                    return ContactLookupResult(
                        exists = true,
                        contactId = id,
                        displayName = name ?: "Contact #$id",
                        lookupKey = key,
                        details = "Verified via PhoneLookup Provider"
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while querying PhoneLookup: ${e.message}")
            return ContactLookupResult(exists = false, permissionMissing = true, details = e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while querying PhoneLookup", e)
        }

        // 2. Secondary fallback: query CommonDataKinds.Phone table directly with PhoneNumberUtils and normalized digits
        try {
            val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
            )

            context.contentResolver.query(phoneUri, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val keyIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)

                val normalizedQuery = normalizePhoneNumber(cleanNumber)

                while (cursor.moveToNext()) {
                    val storedNumber = if (numIndex != -1) cursor.getString(numIndex) else null
                    if (storedNumber != null) {
                        val normalizedStored = normalizePhoneNumber(storedNumber)
                        val isMatch = PhoneNumberUtils.compare(storedNumber, cleanNumber) ||
                                (normalizedQuery.isNotEmpty() && normalizedStored.endsWith(normalizedQuery)) ||
                                (normalizedStored.isNotEmpty() && normalizedQuery.endsWith(normalizedStored))

                        if (isMatch) {
                            val id = if (idIndex != -1) cursor.getLong(idIndex) else null
                            val name = if (nameIndex != -1) cursor.getString(nameIndex) else null
                            val key = if (keyIndex != -1) cursor.getString(keyIndex) else null
                            Log.d(TAG, "Found contact in CommonDataKinds.Phone: ID=$id, Name=$name, Number=$storedNumber")
                            return ContactLookupResult(
                                exists = true,
                                contactId = id,
                                displayName = name ?: "Contact #$id",
                                lookupKey = key,
                                details = "Verified via CommonDataKinds.Phone ($storedNumber)"
                            )
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while querying CommonDataKinds.Phone: ${e.message}")
            return ContactLookupResult(exists = false, permissionMissing = true, details = e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while querying CommonDataKinds.Phone", e)
        }

        return ContactLookupResult(exists = false, details = "Number not found in phonebook")
    }

    /**
     * Saves a new phone number and display name to the device's Contacts provider.
     * Verifies existence before writing to avoid duplicate records.
     */
    fun saveContact(context: Context, phoneNumber: String, displayName: String): Boolean {
        if (phoneNumber.isBlank()) return false

        if (!hasWriteContactsPermission(context)) {
            Log.e(TAG, "WRITE_CONTACTS permission not granted. Cannot save contact.")
            return false
        }

        // Verify if it already exists before saving
        val existingCheck = findContactByNumber(context, phoneNumber)
        if (existingCheck.exists) {
            Log.d(TAG, "Cannot save: contact for $phoneNumber already exists as '${existingCheck.displayName}'.")
            return false
        }

        val ops = ArrayList<ContentProviderOperation>()
        val rawContactInsertIndex = ops.size

        // 1. Insert RawContact
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // 2. Insert Display Name
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build()
        )

        // 3. Insert Phone Number
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.trim())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()
        )

        return try {
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            results.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert new contact into ContactsContract", e)
            false
        }
    }

    /**
     * Creates an Intent to view a contact in the default contacts application.
     */
    fun getViewContactIntent(contactId: Long?, lookupKey: String?): Intent? {
        return if (contactId != null && lookupKey != null) {
            val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
            Intent(Intent.ACTION_VIEW, contactUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else if (contactId != null) {
            val contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId.toString())
            Intent(Intent.ACTION_VIEW, contactUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            null
        }
    }

    /**
     * Creates an Intent to dial the phone number.
     */
    fun getDialIntent(phoneNumber: String): Intent {
        return Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
