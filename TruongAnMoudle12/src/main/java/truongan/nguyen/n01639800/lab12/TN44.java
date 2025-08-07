package truongan.nguyen.n01639800.lab12;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class TN44 extends Fragment {

    private static final String TAG = "TN44";
    private EditText phoneInput, messageInput;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tn44_fragment, container, false);

        phoneInput = view.findViewById(R.id.phoneInput);
        messageInput = view.findViewById(R.id.messageInput);
        Button sendBtn = view.findViewById(R.id.sendBtn);

        sendBtn.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                phoneInput.setError("can not be empty");
                return;
            }

            if (phone.length() < 10) {
                phoneInput.setError("must be 10 digits");
                return;
            }

            if (TextUtils.isEmpty(message)) {
                messageInput.setError("can not be empty");
                return;
            }

            // Permission check
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            } else {
                sendSMS(phone, message);
            }
        });

        return view;
    }

    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    String phone = phoneInput.getText().toString();
                    String message = messageInput.getText().toString();
                    sendSMS(phone, message);
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void sendSMS(String phone, String message) {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null, cannot send SMS");
            return;
        }

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String toastMsg;
                switch (getResultCode()) {
                    case android.app.Activity.RESULT_OK:
                        toastMsg = "SMS sent";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        toastMsg = "Generic failure";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        toastMsg = "No service";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        toastMsg = "Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        toastMsg = "Radio off";
                        break;
                    default:
                        toastMsg = "Unknown error";
                }
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(SENT), Context.RECEIVER_NOT_EXPORTED);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context,
                        getResultCode() == android.app.Activity.RESULT_OK ?
                                "SMS delivered" : "SMS not delivered", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(DELIVERED), Context.RECEIVER_NOT_EXPORTED);


        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI);

            phoneInput.setText("");
            messageInput.setText("");

        } catch (Exception e) {
            Toast.makeText(context, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "sendSMS: Exception: ", e);
        }
    }
}
