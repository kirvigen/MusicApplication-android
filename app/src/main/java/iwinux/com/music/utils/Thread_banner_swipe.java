package iwinux.com.music.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class Thread_banner_swipe  extends Thread{

    int duration;
    int size;
    Boolean end = false;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    public Thread_banner_swipe(LinearLayoutManager linearLayoutManager, RecyclerView recyclerView, int duration, int size){

        this.duration = duration;
        this.size = size;
        this.linearLayoutManager = linearLayoutManager;
        this.recyclerView = recyclerView;
    }

    @Override
    public void run() {
        super.run();
        int position = 0;
        while (true){
            position = linearLayoutManager.findLastVisibleItemPosition();
            if(position ==size -1){
                end = true;
            } else if (position == 0) {
                end = false;
            }
            if(!end){
                position+=1;
            } else {
                position=0;
            }
            recyclerView.smoothScrollToPosition( position );
            try {
                Thread.sleep( duration );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
