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
            Visitor visitor = new Visitor();
            int i = 0;
            do {
                if (i >= 20) break;
                Log.d("blockNumber", Integer.toString(i));
                byte[] suicaLogBin = getSuicaLogBin(nfcF, IDm, i);
                visitor.importSuicaLog(new SuicaLog(suicaLogBin));
                i++;
            } while (true /*visitor.isToday(i)*/);
            mListener.onDiscovered(visitor);
        } catch (IOException e) {
            mListener.onError(e);
        }
    }

    byte[] getSuicaLogBin(NfcF nfcF, byte[] IDm, int blockNumber) throws IOException {
        byte[] res = new byte[0];
        try {
            res = readWithoutEncryption(nfcF, IDm, blockNumber);
        } catch (IOException e) {
            Log.e("nfc", e.getMessage(), e);
            e.printStackTrace();
        }
        if (res == null) return null;
        try {
            byte[] raw = new byte[]{
                    res[13], res[14], res[15], res[16],
                    res[17], res[18], res[19], res[20],
                    res[21], res[22], res[23], res[24],
                    res[25], res[26], res[27], res[28]
            };
            return raw;
        } catch (ArrayIndexOutOfBoundsException e) {
            mListener.onError(e);
        }

        return null;
    }

    byte[] readWithoutEncryption(NfcF nfcF, byte[] IDm, int blockNumber) throws IOException {
        //keep blockNumber under 20
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
         * blockNumber    service code list order
         * -> b1000 **** = 0x80**
         */
        bout.write(0x80);
        bout.write(blockNumber);

        byte[] msg = bout.toByteArray();
        nfcF.connect();
        res = nfcF.transceive(msg);
        nfcF.close();

        return res;
    }

    public interface CardReaderListener {
        void onDiscovered(Visitor visitor);
        void onError(Exception exception);
    }
}
