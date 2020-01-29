package iwinux.com.music;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.Objects.SettingsUser;
import iwinux.com.music.utils.Adapters.AdapterWelcome;
import iwinux.com.music.utils.UserManager;
import me.relex.circleindicator.CircleIndicator;

public class WelcomActivity extends AppCompatActivity {
    ViewPager viewPager;
    ImageView bg;


    TextView back,vpered;
    int[] res = new int[]{R.raw.sounds,R.raw.infinity,R.raw.download,R.raw.import_vk};
    CircleIndicator indicator;
    UserManager userManager;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    ArgbEvaluator textEvaluator = new ArgbEvaluator();
    List<String> items = new ArrayList<>(  );
    public static ActivityCheckout mCheckout;
    Inventory mInventory;
    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(Purchase purchase) {
            // here you can process the loaded purchase
            Log.i( "dsdsds", "onSuccess: " );

                SettingsUser settingsUser = userManager.getsettings();
                settingsUser.setPremium( true );
                settingsUser.setPay( true );
                userManager.createSettings( settingsUser );
                Intent intent = new Intent( getApplicationContext(), MainActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );


        }

        @Override
        public void onError(int response, Exception e) {
            // handle errors here
        }
    }
    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            // your code here
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_welcom );
        userManager = new UserManager( this );
        mCheckout = Checkout.forActivity( this, MyApplication.get().getBilling());
        mCheckout.start();
        mCheckout.createPurchaseFlow(new WelcomActivity.PurchaseListener());
        mInventory = mCheckout.makeInventory();
        mInventory.load(
                Inventory.Request.create().loadAllPurchases()
                        .loadSkus( ProductTypes.IN_APP,getResources().getString( R.string.no_ad_id )),
                new WelcomActivity.InventoryCallback());
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        content();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
    }
    private void content() {


        back = findViewById( R.id.back );
        vpered = findViewById( R.id.vpered );
        viewPager = findViewById( R.id.viewPager );
        indicator = findViewById( R.id.indicator );

        bg = findViewById( R.id.bg );
        items.add( "fdfdfdfdf" );
        items.add( "dssddssd" );
        items.add( "DDDDDD" );
        items.add( "dds" );
        items.add( "dds" );
        vpered.setOnClickListener( v -> viewPager.setCurrentItem(viewPager.getCurrentItem()+1, true) );
        back.setOnClickListener( v -> viewPager.setCurrentItem(viewPager.getCurrentItem()-1, true) );
        final Integer[] colors = new Integer[]{getResources().getColor( R.color.blue ),getResources().getColor( R.color.blue_light )};

        final Integer[] colors1 = new Integer[]{getResources().getColor( R.color.orange ),getResources().getColor( R.color.colorPrimary )};
        viewPager.setAdapter( new AdapterWelcome( getSupportFragmentManager(),items, res ));
        indicator.setViewPager( viewPager );
        bg.setBackground( getDrawable( colors1[0],colors1[1] ) );
        viewPager.setOnPageChangeListener( new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == items.size()-2){
                    back.setAlpha( 1-positionOffset );
                    vpered.setAlpha( 1-positionOffset );
                    indicator.setAlpha( 1-positionOffset );

                }
                if(position == 2){
                       int color1 = (int) argbEvaluator.evaluate( positionOffset,colors1[1],colors[1] );
                    int color = (int) argbEvaluator.evaluate( positionOffset,colors1[0],colors[0] );
                    bg.setBackground( getDrawable( color,color1 )  );
                }

                if(position == 0){
                    back.setAlpha( positionOffset );
                }
                if(position == 0 || position == items.size()-1){
//                     back.setVisibility( View.GONE );
                }else back.setVisibility( View.VISIBLE );
                if(position == items.size()-1){
                    vpered.setVisibility( View.GONE );

                }else vpered.setVisibility( View.VISIBLE );

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        } );
    }



    Drawable getDrawable(int color, int colo1){
        int[] colors = {color,colo1};
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR, colors);

        gd.setCornerRadius(0f);
        return gd;
    }

}
