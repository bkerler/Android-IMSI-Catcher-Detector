package com.SecUpwN.AIMSICD.receiver;

import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SmsReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        try {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final List<SmsMessage> messages = new ArrayList<>();
                for (Object pdu : pdus) {
                    byte smsPdu[] = (byte[]) pdu;
                    /**
                     * 3GPP TS 23.040 9.2.3.9 specifies that Type Zero messages are indicated
                     * by TP_PID field set to value 0x40
                     */
                    int firstByte = smsPdu[0] & 0xff;
                    int mti = firstByte & 0x3;
                    int pID = smsPdu[1] & 0xc0;
                    Log.i("AIMSICD_SmsReceiver", "Pdu data: firstByte = " + firstByte +
                            " mti = " + mti + " TP_PID = " + pID);
                    if (pID == 0x40 || mti == 0) {
                        messages.add(SmsMessage.createFromPdu((byte[]) pdu));
                    }
                }

                if (messages.size() > 0) {
                    for (SmsMessage sms : messages) {
                        Intent smsIntent = new Intent(CellTracker.SILENT_SMS);
                        Bundle smsData = new Bundle();
                        smsData.putString("address", sms.getOriginatingAddress());
                        smsData.putString("display_address",
                                sms.getDisplayOriginatingAddress());
                        smsData.putString("class", sms.getMessageClass().name());
                        smsData.putString("service_centre", sms.getServiceCenterAddress());
                        smsData.putString("message", sms.getMessageBody());
                        smsIntent.putExtras(smsData);
                        context.sendBroadcast(smsIntent);
                        Log.i("AIMSICD_SmsReceiver", "Type 0 Message received, Sender: "
                                + sms.getOriginatingAddress() + " Message: "
                                + sms.getMessageBody());
                    }
                }
            }

        } catch (NullPointerException npe) {
            Log.e("SmsReceiver", "Exception smsReceiver" + npe);
        }
    }

}
