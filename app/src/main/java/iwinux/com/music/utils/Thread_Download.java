package iwinux.com.music.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import iwinux.com.music.Objects.Audio;
import iwinux.com.music.R;

import static com.android.volley.VolleyLog.TAG;

public class Thread_Download extends Thread{
    private Context context;
    private String url;
    String folder;
    String fileName;
    Audio a;
    public Thread_Download(Context context,String url,Audio audio){
        this.context = context;
        this.url = url;
        this.a = audio;
    }

    @Override
    public void run() {
        super.run();
        int count;
        try{
            URL url = new URL(this.url);
            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lengthOfFile = connection.getContentLength();


            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            //External directory path to save file


            folder = Environment.getExternalStorageDirectory().toString()+"/Music"+"/"+context.getResources()
                    .getString( R.string.app_name )+"/";
            fileName = a.getTitle()+"-"+a.getArtist() + ".mp3";
            //Create androiddeft folder if it does not exist
            File directory = new File(folder);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Output stream to write file
            OutputStream output = new FileOutputStream(folder + fileName);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            StorageUtil storageUtil = new StorageUtil( context );
            a.setPath( folder + fileName );
            storageUtil.deletedDownloadAudio( a );
            storageUtil.addDownloadAudio( a );
            Log.i( TAG, " Track Download name: "+fileName );


        }catch (Exception e){

        }
    }
}
