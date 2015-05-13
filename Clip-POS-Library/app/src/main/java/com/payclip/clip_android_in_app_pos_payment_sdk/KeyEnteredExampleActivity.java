package com.payclip.clip_android_in_app_pos_payment_sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.payclip.clip.PayclipCommonLibrary.views.ClipDialog;
import com.payclip.clip_android_in_app_pos_payment_sdk.R;
import com.payclip.clipcorepayments.CCPAPIClient;
import com.payclip.clipcorepayments.PaymentManager;
import com.payclip.clipcorepayments.interfaces.PaymentListener;
import com.payclip.clipcorepayments.interfaces.SignatureListener;
import com.payclip.posLibrary.ManualEntryManager;
import com.payclip.posLibrary.activities.PCKeyEnteredActivity;
import com.payclip.posLibrary.activities.PCSignatureActivity;

import retrofit.RetrofitError;

public class KeyEnteredExampleActivity extends Activity implements ManualEntryManager.KeyEnteredActivityListener, PaymentListener, SignatureListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_entered_example);

        // The authtoken and payment amount must be set before anything can be done.
        // The following lines are required. The payment and manual entry managers will fail if your activity does not become a listener
        CCPAPIClient.getInstance().authToken = "fJbkv0ipMUi1tPxbzT4AmCpikwzlKt9X2dO2e8Us0U8";
        PaymentManager.getInstance().setPaymentListener(this);
        // This method sets the API endpoint, it defaults to debug if the method is not called.
        PaymentManager.getInstance().setAPIEndpoint(CCPAPIClient.PCAPIEndpoint.STAGING);
        ManualEntryManager.getInstance(this).setKeyEnteredListener(this);
        PaymentManager.getInstance().setSignatureListener(this);
    }

    // Boilerplate android methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.key_entered_example, menu);
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

    // This is for the "Enter Card Info" button. Clicking it sets the payment amount(required) and shows the KeyEntered Activity
    public void buttonClicked(View v) {
        EditText amountField = (EditText)findViewById(R.id.amount_field);
        PaymentManager.getInstance().setPaymentAmount(amountField.getText().toString());

        PCKeyEnteredActivity keyEnteredActivity = new PCKeyEnteredActivity();
        Intent intent = new Intent(KeyEnteredExampleActivity.this, keyEnteredActivity.getClass());
        startActivityForResult(intent, 0);
    }

    //////////////////////////////////
    // Key Entered Listener Methods //
    //////////////////////////////////
    @Override
    public void keyEnteredActivityDidCancel() {
        Log.d("Clip", "activity canceled");
    }
    @Override
    public void keyEnteredActivityReceivedCardToken(String s) {
        Log.d("Clip", "Recieved card token: "+s);
    }
    @Override
    public void keyEnteredActivityDidFailWithError(Error error) {
        Log.d("Clip", "Activity failed with error: "+error);
    }
    @Override
    public void keyEnteredActivityDidFinish() {
        PaymentManager.getInstance().startPayment();
    }
    @Override
    public void keyEnteredActivityNextButtonClicked() {
        Log.d("Clip", "Next button clicked");
        ClipDialog.showDialog(this, "Checking card...");
    }
    @Override
    public void limitCheckStarting() {
        Log.d("Clip", "Limit check starting");
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
        Intent intent = new Intent(KeyEnteredExampleActivity.this, signatureActivity.getClass());
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
