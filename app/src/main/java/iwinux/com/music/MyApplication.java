package iwinux.com.music;

import android.app.Application;
import android.support.multidex.MultiDex;

import org.solovyev.android.checkout.Billing;

public class MyApplication extends Application {

    private static MyApplication sInstance;
    String key ="-";


    public MyApplication() {
        sInstance = this;

    }

    public static MyApplication get() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(MyApplication.this);
    }
    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @Override
        public String getPublicKey() {
            return  key;
        }
    });


    public Billing getBilling() {
        return mBilling;
    }
}
