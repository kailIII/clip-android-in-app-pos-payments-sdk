package com.payclip.clip_android_in_app_pos_payment_sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.bbpos.emvswipe.EmvSwipeController;
import com.payclip.clip.PayclipCommonLibrary.views.ClipDialog;
import com.payclip.clip_android_in_app_pos_payment_sdk.R;
import com.payclip.clipcorepayments.CCPAPIClient;
import com.payclip.clipcorepayments.PaymentManager;
import com.payclip.clipcorepayments.interfaces.LimitListener;
import com.payclip.clipcorepayments.interfaces.PaymentListener;
import com.payclip.clipcorepayments.interfaces.SignatureListener;
import com.payclip.clipcorepayments.models.Hardware;
import com.payclip.posLibrary.POSHardwareManager;
import com.payclip.posLibrary.activities.PCSignatureActivity;
import com.payclip.posLibrary.views.POSReaderDialog;

import retrofit.RetrofitError;

public class EMVExampleActivity extends Activity implements PaymentListener, SignatureListener, POSHardwareManager.ClipReaderListener, POSHardwareManager.EmvSwipeListener, LimitListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emvexample);

        // The authtoken and payment amount must be set before anything can be done.
        // The following lines are required. The payment and manual entry managers will fail if your activity does not become a listener
        CCPAPIClient.getInstance().authToken = "fJbkv0ipMUi1tPxbzT4AmCpikwzlKt9X2dO2e8Us0U8";
        PaymentManager.getInstance().setPaymentListener(this);
        PaymentManager.getInstance().setSignatureListener(this);
        PaymentManager.getInstance().setLimitListener(this);

        POSHardwareManager.getInstance(getApplicationContext()).setClipReaderListener(this);
        POSHardwareManager.getInstance(getApplicationContext()).setEmvSwipeListener(this);
        POSHardwareManager.getInstance(getApplicationContext()).setCountry("US");


        final EditText amountField = (EditText)findViewById(R.id.amount_field);
        amountField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                PaymentManager.getInstance().setPaymentAmount(amountField.getText().toString());
            }
        });
    }

    // Boilerplate android methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.emvexample, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //////////////////////////////
    // Payment Listener Methods //
    //////////////////////////////
    @Override
    public void onPaymentStarting() {
        Log.d("Clip", "Payment starting");
        ClipDialog.showDialog(this, "Processing payment");
    }
    @Override
    public void onPaymentApproved() {
        Log.d("Clip", "Payment approved");
        ClipDialog.hideDialog();

        PCSignatureActivity signatureActivity = new PCSignatureActivity();
        Intent intent = new Intent(EMVExampleActivity.this, signatureActivity.getClass());
        startActivityForResult(intent, 0);
    }
    @Override
    public void onPaymentDeclined() {
        Log.d("Clip", "Payment declined");
        EditText amountField = (EditText)findViewById(R.id.amount_field);
        amountField.setText("");
        PaymentManager.getInstance().clearTransactionalData();
        ClipDialog.hideDialog();
    }
    @Override
    public void onPaymentError() {
        Log.d("Clip", "Payment error");
        ClipDialog.hideDialog();
    }

    //////////////////////////////////
    // Clip Reader Listener Methods //
    //////////////////////////////////
    @Override
    public void onReaderPlugged() {
        POSHardwareManager.getInstance(getApplicationContext()).startReader();
    }

    @Override
    public void onReaderUnplugged() {
        POSReaderDialog.hideDialog();
    }

    @Override
    public void onReaderStarting() {
        POSReaderDialog.showStartingReaderDialog(this);
    }

    @Override
    public void onReaderStarted(Hardware hardware) {
        POSReaderDialog.showBatteryStatus(this, hardware);
    }

    @Override
    public void onReaderError(EmvSwipeController.Error error) {
        Log.d("Clip", "Reader error: " + error);
        POSReaderDialog.hideDialog();
    }
    ////////////////////////////////
    // EMV Swipe Listener Methods //
    ////////////////////////////////

    // This Method gets called when the card is swiped
    @Override
    public void onRequestCIDInformation(String maskedPan) {
        Log.d("Clip", "request cid info!");
    }

    @Override
    public void onEmvFlowStarting() {
        Log.d("Clip", "EMV starting!");
        PaymentManager.getInstance().checkLimit(this);
    }

    @Override
    public void onClipReaderCardError(POSHardwareManager.ClipCardError cardError) {
        Log.d("Clip", "Reader card error: "+cardError);
        ClipDialog.hideDialog();
    }

    @Override
    public void onProcessingProgress(int step, int numSteps) {
        Log.d("Clip", "processing payment");
        if (step == 1) {
            ClipDialog.showDialog(this, "Processing Payment");
        } else {
            ClipDialog.showDialog(this, "Almost done..");
        }
    }

    ////////////////////////////////
    // Signature Listener Methods //
    ////////////////////////////////
    @Override
    public void sendSignatureSuccess() {
        ClipDialog.showDialogForDuration(this, "Signature Sent");
        EditText amountField = (EditText)findViewById(R.id.amount_field);
        amountField.setText("");
        PaymentManager.getInstance().clearTransactionalData();
    }

    @Override
    public void sendSignatureFailed(RetrofitError error) {
        Log.d("Clip", "Signature send failed");
        EditText amountField = (EditText)findViewById(R.id.amount_field);
        amountField.setText("");
        PaymentManager.getInstance().clearTransactionalData();
    }

    //////////////////////////////////
    // Limit check listener methods //
    //////////////////////////////////
    @Override
    public void onLimitCheckStarting() {
        ClipDialog.showDialog(this, "Checking limit");
    }

    @Override
    public void onLimitCheckSuccess() {
        // Limit check has succeeded, now you can start the payment
        startPayment();
    }

    @Override
    public void onLimitCheckFailure() {
        ClipDialog.hideDialog();
        ClipDialog.showDialogForDuration(this, "Limit check failed");
    }

    @Override
    public void onLimitCheckError() {
        ClipDialog.hideDialog();
        ClipDialog.showDialogForDuration(this, "Limit check error");
    }

    private void startPayment() {
        ClipDialog.showDialog(this, "Processing Payment");
        PaymentManager.getInstance().startPayment();
    }
}
