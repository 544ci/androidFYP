package com.example.fyp_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.security.KeyChain;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Encryption{
    private Context context;
    private EncryptionListener listener;
    private Crypto crypto;
    private ExecutorService executorService;
    private int threadCount;
    private String[] fileTypesToEncrypt;
    private static final String ALIAS= "encrypt";
    private SharedPreferences.Editor editor;
    private SharedPreferences sp;
    Encryption(Context context, EncryptionListener listener,String encryptionKey, String[] fileTypesToEncrypt, int threadCount) {
        this.context = context;
        this.listener = listener;
        executorService = Executors.newFixedThreadPool(threadCount);
        this.threadCount = threadCount;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();
        ConstKeyChain pgkc;
        pgkc = new ConstKeyChain(encryptionKey.getBytes(StandardCharsets.UTF_8), CryptoConfig.KEY_256);
        crypto = AndroidConceal.get().createDefaultCrypto(pgkc);
        this.fileTypesToEncrypt = fileTypesToEncrypt;
    }

    public void encryptSdCard() {
        ArrayList<File> files = getSdCardFiles();
        for (File file : files) {
            executorService.execute(new EncryptFile(file));
        }
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            listener.onSDCardEncryptionComplete();
        } catch (InterruptedException e) {
            listener.onSDCardEncryptionFailed(e.getMessage());

        }

    }

    public void decryptSdCard() {
        ArrayList<File> files = getSdCardFiles();
        for (File file : files) {
            executorService.execute(new DecryptFile(file));
        }
        try {
            //  executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            executorService.awaitTermination(5, TimeUnit.SECONDS);

            listener.onSDCardDecryptionComplete();

        } catch (InterruptedException e) {
            listener.onSDCardDecryptionFailed(e.getMessage());
        }
    }

    private String getFileType(File file) {
        String name = file.getName();
        String[] p = name.split("\\.");
        if (p.length != 0) {
            return (p[p.length - 1]);
        }
        return "";
    }

    private ArrayList<File> getSdCardFiles() {
        return getFilesFromFolder(Environment.getExternalStorageDirectory());
    }

    public ArrayList<File> getFilesFromFolder(File folder) {
        ArrayList<File> files = new ArrayList<>();
        if (!folder.isDirectory())
            return files;
        for (File f : folder.listFiles())
            if (f.isDirectory())
                files.addAll(getFilesFromFolder(f));
            else if (!f.isHidden() && f.canWrite() && !f.canExecute() && allowEncryption(f))
                files.add(f);

        return files;
    }

    private boolean allowEncryption(File file) {
        String type = getFileType(file);
        if (!type.equals("")) {
            for (String allowedType : fileTypesToEncrypt)
                if (allowedType.equals(type))
                    return true;
        }
        return false;
    }


    private boolean keyCreated(){
        return !sp.getString("iv","").equals("");
    }
    private File createTempFile(File file) throws IOException {
        File tmp = new File(file.getParent() + "/" + UUID.randomUUID().toString() + "." + getFileType(file));
        tmp.createNewFile();
        return tmp;
    }

    private OutputStream getEncryptionStream(File f) throws IOException, KeyChainException, CryptoInitializationException {
        OutputStream fileStream = new BufferedOutputStream(
                new FileOutputStream(f.getPath()));
        return crypto.getCipherOutputStream(
                fileStream, new Entity("Password"));
    }

    private InputStream getDecryptionStream(File f) throws IOException, KeyChainException, CryptoInitializationException {
        InputStream fileStream = new BufferedInputStream(
                new FileInputStream(f.getPath()));
        return crypto.getCipherInputStream(
                fileStream, new Entity("Password"));
    }

    private class EncryptFile implements Runnable {
        private File file;

        EncryptFile(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            Log.w("com.example.", "encrypting File" + file.getName());
            encryptFile();
        }

        private void encryptFile() {

            try {
                if (!crypto.isAvailable()) {
                    return;
                }
                File tmp = createTempFile(file);
                OutputStream out = getEncryptionStream(tmp);
                FileInputStream in = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int x;
                while ((x = in.read(buffer)) != -1) {
                    out.write(buffer, 0, x);
                }
                out.close();
                in.close();
                FileUtils.copyFile(tmp, file);
                tmp.delete();
            } catch (IOException e) {
                Log.w("com.example.", "File" + file.getName() + "Encryption Failed");

                return;
            } catch (KeyChainException e) {
                e.printStackTrace();
            } catch (CryptoInitializationException e) {
                e.printStackTrace();
            }

        }
    }


    private class DecryptFile implements Runnable {
        private File file;

        DecryptFile(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            Log.w("com.example.", "decrypting File" + file.getName());
            decryptFile();
        }

        private void decryptFile() {
            try {
                if (!crypto.isAvailable()) {
                    return;
                }
                File tmp = createTempFile(file);
                InputStream in = getDecryptionStream(file);
                byte[] buffer = new byte[1024];
                FileOutputStream out = new FileOutputStream(tmp);
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.close();
                in.close();
                FileUtils.copyFile(tmp, file);

                Log.w("com.example.", "File" + file.getName() + "Decryption Complete");

            } catch (IOException ex) {
                Log.w("com.example.", "File" + file.getName() + "Decryption Failed");

            } catch (KeyChainException e) {
                e.printStackTrace();
                Log.w("com.example.", "File" + file.getName() + "Decryption Failed");

            } catch (CryptoInitializationException e) {
                e.printStackTrace();
                Log.w("com.example.", "File" + file.getName() + "Decryption Failed");

            }

        }
    }


}

