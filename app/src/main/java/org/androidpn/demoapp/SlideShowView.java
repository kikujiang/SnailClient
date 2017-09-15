package org.androidpn.demoapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

  
/**
 * 
 * 类的描述：主界面图片轮播功能
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
  
public class SlideShowView extends FrameLayout {  
      
    // 使用universal-image-loader插件读取网络图片，需要工程导入universal-image-loader-1.8.6-with-sources.jar  
    //private ImageLoader imageLoader = ImageLoader.getInstance();  
  
    //轮播图图片数量  
    private final static int IMAGE_COUNT = 5;  
    //自动轮播的时间间隔  
    private final static int TIME_INTERVAL = 5;  
    //自动轮播启用开关  
    private final static boolean isAutoPlay = true;   
      
    //自定义轮播图的资源  
   // private String[] imageUrls;  
    //放轮播图片的ImageView 的list  
    private List<ImageView> imageViewsList;  
    //放圆点的View的list  
    private List<View> dotViewsList;  
      
    private ViewPager viewPager;  
    //当前轮播页  
    private int currentItem  = 0;  
    //定时任务  
    private ScheduledExecutorService scheduledExecutorService;  
      
    private Context context;  
      
    //Handler  
    private Handler handler = new Handler(){  
  
        @Override  
        public void handleMessage(Message msg) {  
            // TODO Auto-generated method stub  
            super.handleMessage(msg);  
            viewPager.setCurrentItem(currentItem);  
        }  
          
    };  
      
    public SlideShowView(Context context) {  
        this(context,null);  
        // TODO Auto-generated constructor stub  
    }  
    public SlideShowView(Context context, AttributeSet attrs) {  
        this(context, attrs, 0);  
        // TODO Auto-generated constructor stub  
    }  
    public SlideShowView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        this.context = context;  
        imageViewsList = new ArrayList<ImageView>();  
        dotViewsList = new ArrayList<View>();  
        initUI(context); 
        if(isAutoPlay){  
            startPlay();  
        }  
          
    }  
    /** 
     * 开始轮播图切换 
     */  
    private void startPlay(){  
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();  
        scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 1, 4, TimeUnit.SECONDS);  
    }  
    /** 
     * 停止轮播图切换 
     */  
    private void stopPlay(){  
        scheduledExecutorService.shutdown();  
    }  

    /** 
     * 初始化Views等UI 
     */  
    private void initUI(Context context){  
        LayoutInflater.from(context).inflate(R.layout.layout_slideshow, this, true);  
          
        LinearLayout dotLayout = (LinearLayout)findViewById(R.id.dotLayout);  
        dotLayout.removeAllViews();  
          
        // 热点个数与图片特殊相等  
//        
            ImageView view =  new ImageView(context);  
            ImageView view1 =  new ImageView(context);  
            ImageView view2=  new ImageView(context);  
            ImageView view3 =  new ImageView(context);  
            ImageView view4 =  new ImageView(context);  
            view.setImageResource(R.drawable.page0);
            view1.setImageResource(R.drawable.page1);
            view2.setImageResource(R.drawable.page2);
            view3.setImageResource(R.drawable.page3);
            view4.setImageResource(R.drawable.page4);
//            view.setTag(imageUrls[i]);  
//            if(i==0)//给一个默认图  
//                view.setBackgroundResource(R.drawable.appmain_subject_1);  
           view.setScaleType(ScaleType.FIT_XY); 
           view1.setScaleType(ScaleType.FIT_XY);
           view2.setScaleType(ScaleType.FIT_XY);
           view3.setScaleType(ScaleType.FIT_XY);
           view4.setScaleType(ScaleType.FIT_XY);
           imageViewsList.add(view);  
           imageViewsList.add(view1);  
           imageViewsList.add(view2);  
           imageViewsList.add(view3);  
           imageViewsList.add(view4);  
           for (int i = 0; i <5; i++) {    
            ImageView dotView =  new ImageView(context);  
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);  
            params.leftMargin = 4;  
            params.rightMargin = 4;  
            dotLayout.addView(dotView, params);  
            dotViewsList.add(dotView);  
        }  
          
        viewPager = (ViewPager) findViewById(R.id.viewPager);  
        viewPager.setFocusable(true);  
          
        viewPager.setAdapter(new MyPagerAdapter());  
        viewPager.setOnPageChangeListener(new MyPageChangeListener());  
    }  
      
    /** 
     * 填充ViewPager的页面适配器 
     *  
     */  
    private class MyPagerAdapter  extends PagerAdapter{  
  
        @Override  
        public void destroyItem(View container, int position, Object object) {  
            // TODO Auto-generated method stub  
            //((ViewPag.er)container).removeView((View)object);  
            ((ViewPager)container).removeView(imageViewsList.get(position));  
        }  
  
        @Override  
        public Object instantiateItem(View container, int position) {  
            ImageView imageView = imageViewsList.get(position);  
              
            ((ViewPager)container).addView(imageViewsList.get(position));  
            return imageViewsList.get(position);  
        }  
  
        @Override  
        public int getCount() {  
            // TODO Auto-generated method stub  
            return imageViewsList.size();  
        }  
  
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            // TODO Auto-generated method stub  
            return arg0 == arg1;  
        }  
        @Override  
        public void restoreState(Parcelable arg0, ClassLoader arg1) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public Parcelable saveState() {  
            // TODO Auto-generated method stub  
            return null;  
        }  
  
        @Override  
        public void startUpdate(View arg0) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public void finishUpdate(View arg0) {  
            // TODO Auto-generated method stub  
              
        }  
          
    }  
    /** 
     * ViewPager的监听器 
     * 当ViewPager中页面的状态发生改变时调用 
     *  
     */  
    private class MyPageChangeListener implements OnPageChangeListener{  
  
        boolean isAutoPlay = false;  
  
        public void onPageScrollStateChanged(int arg0) {  
            // TODO Auto-generated method stub  
            switch (arg0) {  
            case 1:// 手势滑动，空闲中  
                isAutoPlay = false;  
                break;  
            case 2:// 界面切换中  
                isAutoPlay = true;  
                break;  
            case 0:// 滑动结束，即切换完毕或者加载完毕  
                // 当前为最后一张，此时从右向左滑，则切换到第一张  
                if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {  
                    viewPager.setCurrentItem(0);  
                }  
                // 当前为第一张，此时从左向右滑，则切换到最后一张  
                else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {  
                    viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);  
                }  
                break;  
        }  
        }  
  
        public void onPageScrolled(int arg0, float arg1, int arg2) {  
            // TODO Auto-generated method stub  
              
        }  
  
        public void onPageSelected(int pos) {  
            // TODO Auto-generated method stub  
              
            currentItem = pos;  
            for(int i=0;i < dotViewsList.size();i++){  
                if(i == pos){  
                    ((View)dotViewsList.get(pos)).setBackgroundResource(R.drawable.b);  
                }else {  
                    ((View)dotViewsList.get(i)).setBackgroundResource(R.drawable.w);  
                }  
            }  
        }  
          
    }  
      
    /** 
     *执行轮播图切换任务 
     * 
     */  
    private class SlideShowTask implements Runnable{  
  
        public void run() {  
            // TODO Auto-generated method stub  
            synchronized (viewPager) {  
                currentItem = (currentItem+1)%imageViewsList.size();  
                handler.obtainMessage().sendToTarget();  
            }  
        }  
          
    }  
      
    /** 
     * 销毁ImageView资源，回收内存 
     *  
     */  
    private void destoryBitmaps() {  
  
        for (int i = 0; i < IMAGE_COUNT; i++) {  
            ImageView imageView = imageViewsList.get(i);  
            Drawable drawable = imageView.getDrawable();  
            if (drawable != null) {  
                //解除drawable对view的引用  
                drawable.setCallback(null);  
            }  
        }  
    }  
}
   
  
  