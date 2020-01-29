package iwinux.com.music;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import iwinux.com.music.utils.UserManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        UserManager userManager = new UserManager( this );
        if(userManager.getFirstOpen()){
            Intent intent = new Intent( this,MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        }else{
            Intent intent = new Intent( this,WelcomActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        }
        finish();
    }
}
