package info.blockchain.wallet.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.SystemClock;

import info.blockchain.wallet.MainActivity;
import info.blockchain.wallet.payload.PayloadFactory;

import org.bitcoinj.core.bip44.WalletFactory;

import java.io.File;
import java.security.Security;

import piuk.blockchain.android.R;

public class AppUtil {

    private static final String REGEX_UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private static AppUtil instance = null;
    private static Context context = null;

    private static boolean DEBUG = false;

    private static final long LOGOUT_TIMEOUT = 1000L * 30L; // 30 seconds in milliseconds
    public static final String LOGOUT_ACTION = "info.blockchain.wallet.LOGOUT";
    private static PendingIntent logoutPendingIntent;

    private static String strReceiveQRFilename = null;

    private static boolean newlyCreated = false;

    private AppUtil() {
        // Singleton
    }

    public static AppUtil getInstance(Context ctx) {
        context = ctx;

        if (instance == null) {
            strReceiveQRFilename = context.getExternalCacheDir() + File.separator + "qr.png";
            instance = new AppUtil();

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setAction(LOGOUT_ACTION);
            logoutPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        }

        return instance;
    }

    public void clearCredentialsAndRestart() {
        WalletFactory.getInstance().set(null);
        PayloadFactory.getInstance().wipe();
        PrefsUtil.getInstance(context).clear();
        restartApp();
    }

    public void restartApp() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void restartApp(String name, boolean value) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (name != null) {
            intent.putExtra(name, value);
        }
        context.startActivity(intent);
    }

    public boolean isDEBUG() {
        return DEBUG;
    }

    public void setDEBUG(boolean debug) {
        DEBUG = debug;
    }

    public String getReceiveQRFilename() {
        return strReceiveQRFilename;
    }

    public void deleteQR() {
        File file = new File(strReceiveQRFilename);
        if (file.exists()) {
            file.delete();
        }
    }

    public void setUpgradeReminder(long ts) {
        PrefsUtil.getInstance(context).setValue(PrefsUtil.KEY_HD_UPGRADED_LAST_REMINDER, ts);
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }

    public void setNewlyCreated(boolean newlyCreated) {
        this.newlyCreated = newlyCreated;
    }

    public boolean isSane() {
        String guid = PrefsUtil.getInstance(context).getValue(PrefsUtil.KEY_GUID, "");

        if (!guid.matches(REGEX_UUID)) {
            return false;
        }

        String encryptedPassword = PrefsUtil.getInstance(context).getValue(PrefsUtil.KEY_ENCRYPTED_PASSWORD, "");
        String pinID = PrefsUtil.getInstance(context).getValue(PrefsUtil.KEY_PIN_IDENTIFIER, "");

        if (encryptedPassword.length() == 0 || pinID.length() == 0) {
            return false;
        }

        return true;
    }

    public boolean isCameraOpen() {
        Camera camera = null;

        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) {
                camera.release();
            }
        }

        return false;
    }

    public String getSharedKey() {
        return PrefsUtil.getInstance(context).getValue(PrefsUtil.KEY_SHARED_KEY, "");
    }

    public void setSharedKey(String sharedKey) {
        PrefsUtil.getInstance(context).setValue(PrefsUtil.KEY_SHARED_KEY, sharedKey);
    }

    public boolean isNotUpgraded() {
        return PayloadFactory.getInstance().get() != null && !PayloadFactory.getInstance().get().isUpgraded();
    }

    /**
     * Called from all activities' onPause
     */
    public void startLogoutTimer() {
        System.out.println("Starting logout timer...");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + LOGOUT_TIMEOUT, logoutPendingIntent);
    }

    /**
     * Called from all activities' onResume
     */
    public void stopLogoutTimer() {
        System.out.println("Stopping logout timer...");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(logoutPendingIntent);
    }

    public static void logout() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(LOGOUT_ACTION);
        context.startActivity(intent);
    }

    public void applyPRNGFixes() {
        try {
            PRNGFixes.apply();
        } catch (Exception e0) {
            //
            // some Android 4.0 devices throw an exception when PRNGFixes is re-applied
            // removing provider before apply() is a workaround
            //
            Security.removeProvider("LinuxPRNG");
            try {
                PRNGFixes.apply();
            } catch (Exception e1) {
                ToastCustom.makeText(context, context.getString(R.string.cannot_launch_app), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                AppUtil.logout();
            }
        }
    }
}
