package jp.gr.java_conf.pasora.orf2015;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by masahikohara on 2015/10/30.
 */
public class CardReader implements NfcAdapter.ReaderCallback {
    protected Activity mActivity;
    protected NfcAdapter mNfcAdapter;
    protected CardReaderListener mListener;

    final protected static char[] hexArray;

    static {
        hexArray = "0123456789ABCDEF".toCharArray();
    }

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
            String suicaLogStr = getSuicaLogStr(nfcF, IDm);
            mListener.onDiscovered(suicaLogStr);
        } catch (IOException e) {
            mListener.onError(e);
        }
    }

    String getSuicaLogStr(NfcF nfcF, byte[] IDm) throws IOException {
        byte[] res = new byte[0];
        try {
            res = readWithoutEncryption(nfcF, IDm);
        } catch (IOException e) {
            Log.e("nfc", e.getMessage(), e);
            e.printStackTrace();
        }
        if (res == null) return null;
        byte[] raw = new byte[]{
                res[13], res[14], res[15], res[16],
                res[17], res[18], res[19], res[20],
                res[21], res[22], res[23], res[24],
                res[25], res[26], res[27], res[28]
        };
        return bytesToHex(raw);
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
        bout.write(0x01);

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

    String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public interface CardReaderListener {
        void onDiscovered(String suicaLogStr);
        void onError(Exception exception);
    }
}
