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
import com.payclip.clipcorepayments.interfaces.PaymentListener;
import com.payclip.clipcorepayments.interfaces.SignatureListener;
import com.payclip.clipcorepayments.models.Hardware;
import com.payclip.posLibrary.CIDManager;
import com.payclip.posLibrary.POSHardwareManager;
import com.payclip.posLibrary.activities.PCCIDActivity;
import com.payclip.posLibrary.activities.PCSignatureActivity;
import com.payclip.posLibrary.views.POSReaderDialog;

import retrofit.RetrofitError;

public class SwipeExampleActivity extends Activity implements CIDManager.CIDActivityListener, PaymentListener, POSHardwareManager.ClipReaderListener, POSHardwareManager.EmvSwipeListener, SignatureListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_example);

        // The authtoken and payment amount must be set before anything can be done.
        // The following lines are required. The payment and manual entry managers will fail if your activity does not become a listener
        CCPAPIClient.getInstance().authToken = "fJbkv0ipMUi1tPxbzT4AmCpikwzlKt9X2dO2e8Us0U8";
        PaymentManager.getInstance().setPaymentListener(this);
        PaymentManager.getInstance().setSignatureListener(this);
        CIDManager.getInstance(this).setCIDActivityListener(this);

        POSHardwareManager.getInstance(getApplicationContext()).setClipReaderListener(this);
        POSHardwareManager.getInstance(getApplicationContext()).setEmvSwipeListener(this);

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
        getMenuInflater().inflate(R.menu.swipe_example, menu);
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

    ///////////////////////////////////
    // CID Activity Listener Methods //
    ///////////////////////////////////
    @Override
    public void CIDActivityDidCancel() {
        Log.d("Clip", "CID canceled");
    }

    @Override
    public void CIDActivityDidFailWithError(Error error) {
        Log.d("Clip", "CID failed with error: "+error);
        ClipDialog.hideDialog();
    }

    @Override
    public void CIDActivityDidFinish() {
        // Limit check has succeeded, now you can start the payment
        Log.d("Clip", "CID finished");
        ClipDialog.hideDialog();
        PaymentManager.getInstance().startPayment();
    }

    @Override
    public void CIDNextButtonClicked() {
        Log.d("Clip", "CID next button clicked");
    }

    @Override
    public void limitCheckStarting() {
        // Limit Check has started so you can alert the user here.
        ClipDialog.showDialog(this, "Checking limit...");
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
        Intent intent = new Intent(SwipeExampleActivity.this, signatureActivity.getClass());
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
        PCCIDActivity cidActivity = new PCCIDActivity();
        Intent intent = new Intent(SwipeExampleActivity.this, cidActivity.getClass());
        startActivityForResult(intent, 0);
    }

    @Override
    public void onEmvFlowStarting() {
        Log.d("Clip", "EMV starting!");
    }

    @Override
    public void onClipReaderCardError(POSHardwareManager.ClipCardError cardError) {
        Log.d("Clip", "Reader card error: "+cardError);
    }

    @Override
    public void onProcessingProgress(int step, int numSteps) {
        Log.d("Clip", "processing payment");
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
}
