// IAPServiceCallback.aidl
package com.sec.android.iap;

interface IAPServiceCallback {
	oneway void responseCallback(in Bundle bundle);
}