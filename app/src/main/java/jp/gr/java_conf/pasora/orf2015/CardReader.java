package jp.gr.java_conf.pasora.orf2015;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;

/**
 * Created by masahikohara on 2015/10/30.
 */
public class CardReader implements NfcAdapter.ReaderCallback {
    protected Activity mActivity;
    protected NfcAdapter mNfcAdapter;
    protected CardReaderListener mListener;

    public CardReader(Activity activity, CardReaderListener listener) {
        mActivity = activity;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity);
        mListener = listener;
    }

    public void enable() {
        if (mActivity == null) return;
        mNfcAdapter.enableReaderMode(mActivity, this, NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    public void disable() {
        if (mActivity == null) return;
        mNfcAdapter.disableReaderMode(mActivity);
    }

    public boolean isNfcInstalled() {
        return mActivity != null;
    }

    public boolean isNfcEnabled() {
        return mActivity != null && mNfcAdapter.isEnabled();
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        NfcF nfcF = NfcF.get(tag);
        byte[] IDm = tag.getId();
        try {
            String[] suicaLog = getSuicaLog(nfcF, IDm);
            mListener.onDiscovered(suicaLog);
        } catch (IOException e) {
            mListener.onError(e);
        }
    }

    String[] getSuicaLog(NfcF nfcF, byte[] IDm) throws IOException {
        byte[] res = new byte[0];
        try {
            res = readWithoutEncryption(nfcF, IDm);
        } catch (IOException e) {
            Log.e("nfc", e.getMessage(), e);
            e.printStackTrace();
        }
        if (res == null) return null;
        byte[] numberCodes1 = new byte[]{res[13], res[14]};
        byte[] numberCodes2 = new byte[]{res[13], res[14]};
        String suicaLog1 = new String(numberCodes1, "US-ASCII");
        String suicaLog2 = new String(numberCodes1, "US-ASCII");
        String[] hoge = {suicaLog1, suicaLog2};
        return hoge;
    }

    byte[] readWithoutEncryption(NfcF nfcF, byte[] IDm) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
        byte[] res;

        //size of data to send
        bout.write(0x10);

        /**********************
         * command packet data
         **********************
         * command code         0x06(Read Without Encryption)
         * idm
         * number of services   0x01
         * service code list    0x0F09 (little endian)
         * number of blocks     0x01
         */
        bout.write(0x06);
        bout.write(IDm);
        bout.write(0x01);
        bout.write(0x0F);
        bout.write(0x09);
        bout.write(0x14);

        /***********************
         *  block list element
         ***********************
         * b1       2 bytes block list element
         * b000     access mode
         * b1001    service code list order
         * -> b1000 1001 = 0x8001
         */
        bout.write(0x80);
        bout.write(0x01);

        byte[] msg = bout.toByteArray();
        nfcF.connect();
        res = nfcF.transceive(msg);
        nfcF.close();

        return res;

    }

    public interface CardReaderListener {
        void onDiscovered(String[] suicaLog);
        void onError(Exception exception);
    }
}
