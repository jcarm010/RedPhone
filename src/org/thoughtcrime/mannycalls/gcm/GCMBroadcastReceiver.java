package org.thoughtcrime.mannycalls.gcm;

import android.content.Context;

public class GCMBroadcastReceiver extends com.google.android.gcm.GCMBroadcastReceiver {

  @Override
  protected String getGCMIntentServiceClassName(Context context) {
    return "org.thoughtcrime.redphone.gcm.GCMIntentService";
  }

}
