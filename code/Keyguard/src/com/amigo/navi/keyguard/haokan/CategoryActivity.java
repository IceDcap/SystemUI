package com.amigo.navi.keyguard.haokan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.everydayphoto.NavilSettings;
import com.amigo.navi.keyguard.haokan.db.CategoryDB;
import com.amigo.navi.keyguard.haokan.entity.Category;
import com.amigo.navi.keyguard.network.local.ReadAndWriteFileFromSD;
import com.amigo.navi.keyguard.network.local.LocalBitmapOperation;
import com.amigo.navi.keyguard.network.local.utils.DiskUtils;
import com.amigo.navi.keyguard.settings.KeyguardWallpaper;

import java.io.File;
import java.util.List;

import org.w3c.dom.Text;

import com.android.keyguard.R;

public class CategoryActivity extends Activity{
    
    private static final String TAG = "HKCategoryActivity";
    
    private static final int PERSONAL_ID = 10;
    private static final int FAVORITE_ID = 11;
    
    private GridView mGridView;

    private List<Category> list = null;

    private CategoryAdapter mCategoryAdapter;
    private Bitmap mWindowBackgroud;
    
    private TextView mTextView;
    private static final String PATH = "category_pics";
    private Handler mHandler = new Handler(){

         @Override
         public void handleMessage(Message msg) {
             onFillUI();
         }
     };
     
     
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.haokan_category_layout);
 
        UIController.getInstance().setCategoryActivity(this);
        if(Build.VERSION.SDK_INT  >=  21){
            this.getWindow().getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            this.getWindow().setStatusBarColor(Color.TRANSPARENT);
            this.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        
        
        onInitUI();
        setBlurBackground();
       LocalBitmapOperation localFileOperation = new LocalBitmapOperation(this);
       final ReadAndWriteFileFromSD dealWithFileFromLocal = new ReadAndWriteFileFromSD(this, DiskUtils.CATEGORY_BITMAP_FOLDER,
                DiskUtils.getCachePath(this), localFileOperation);
        new Thread(new Runnable(){

            @Override
            public void run() {
                
                list = CategoryDB.getInstance(getApplicationContext()).queryCategorys();
                for (Category category : list) {
                    String url = category.getTypeIconUrl();
                    DebugLog.d(TAG,"category onCreate url:" + url);
                    if(!TextUtils.isEmpty(url)){
                    	if(Category.IMAGE_FROM_FIXED_FOLDER == category.getType()){
                    		String path = PATH + File.separator + url + ".png";
                    		Bitmap bitmap = DiskUtils.getImageFromAssetsFile(getApplicationContext(), path);
                            category.setIcon(bitmap);
                    	}else{
                            String key = DiskUtils.constructFileNameByUrl(url);
                            DebugLog.d(TAG,"category onCreate key:" + key);
                            Bitmap bmp = dealWithFileFromLocal.readFromLocal(key);
                            category.setIcon(bmp);
                    	}
                    }
                }
                
                mHandler.sendMessage(mHandler.obtainMessage());
            }
        }).start();
    }    
    
   
    
    private void onInitUI() {
        mCategoryAdapter = new CategoryAdapter(this);
        mTextView = (TextView)findViewById(R.id.TextView);
        mTextView.setVisibility(View.GONE);
        
        mGridView = (GridView) findViewById(R.id.haokan_gridview);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
 
    }
   
    
    private void setBlurBackground() {
        this.getWindow().setBackgroundDrawable(null);
        Bitmap bitmap = UIController.getInstance().getCurrentWallpaperBitmap(this);
        mWindowBackgroud = KeyguardWallpaper.getBlurBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), 5.0f);
        if (mWindowBackgroud == null) {
            return;
        }
        Drawable drawable = new BitmapDrawable(getResources(), mWindowBackgroud);

        if (drawable != null) {
            ColorMatrix cm = new ColorMatrix();
            float mBlind = 0.6f;
            cm.set(new float[] { mBlind, 0, 0, 0, 0, 0, mBlind, 0, 0, 0, 0, 0,
                    mBlind, 0, 0, 0, 0, 0, 1, 0 });
            drawable.setColorFilter(new ColorMatrixColorFilter(cm));
            this.getWindow().setBackgroundDrawable(drawable);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        RequestNicePicturesFromInternet.getInstance(this.getApplicationContext()).registerData(false);
        if (mWindowBackgroud != null && !mWindowBackgroud.isRecycled()) {
            mWindowBackgroud.recycle();
        }
        for (Category category : list) {
        	Bitmap bitmap = category.getIcon();
        	if(bitmap != null && !bitmap.isRecycled()){
        		bitmap.recycle();
        		category.setIcon(null);
        	}
        }
    }
    
    private void onItemClick(int position) {
        Category category = list.get(position);
        category.setFavorite(!category.isFavorite());
        mCategoryAdapter.notifyDataSetChanged();
        if (category.getTypeId() == PERSONAL_ID) {
            NavilSettings.setBooleanSharedConfig(getApplicationContext(),
                    NavilSettings.CATEGORY_PERSONAL, category.isFavorite());
        } else if (category.getTypeId() == FAVORITE_ID) {
            NavilSettings.setBooleanSharedConfig(getApplicationContext(),
                    NavilSettings.CATEGORY_FAVORITE, category.isFavorite());
        } else {
            CategoryDB.getInstance(getApplicationContext())
                    .updateFavorite(category);
        }
    }
 

    public void onFillUI() {

        mHandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                mGridView.setLayoutAnimation(getAnimationController());
                mGridView.setAdapter(mCategoryAdapter);
                mCategoryAdapter.notifyDataSetChanged();
                startTextViewAnimation();
            }
        }, 100);
    }
    
    protected LayoutAnimationController getAnimationController() {  
        AnimationSet set = new AnimationSet(true);  
  
        Animation animation = new AlphaAnimation(0.0f, 1.0f);  
        animation.setDuration(300);  
        set.addAnimation(animation);  
        animation = new TranslateAnimation(0, 0, 300, 0); 
        animation.setDuration(300);  
        set.addAnimation(animation);  
        set.setInterpolator(new DecelerateInterpolator());
 
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.2f);  
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);  
        return controller;  
    }  
    
    private void startTextViewAnimation() {
        AnimationSet textViewSet = new AnimationSet(true);  
        Animation animation = new AlphaAnimation(0.0f, 1.0f);  
        animation.setDuration(300);  
        textViewSet.addAnimation(animation);  
        animation = new TranslateAnimation(0, 0, 300, 0); 
        animation.setDuration(300);  
        textViewSet.addAnimation(animation);  
        textViewSet.setInterpolator(new DecelerateInterpolator());
        mTextView.setVisibility(View.VISIBLE);
        mTextView.startAnimation(textViewSet);
    }
    

    class CategoryAdapter extends BaseAdapter {
        
        private LayoutInflater mInflater;

        public CategoryAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }
        
        public void chiceState(int arg0) {
           
        }

        @Override
        public Object getItem(int arg0) {
            return list.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            DebugLog.d(TAG,"getView");
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.haokan_category_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.haokan_category_item_text);
                holder.favorite = (ImageView) convertView.findViewById(R.id.haokan_category_item_flag);
                holder.image = (CircleImageView) convertView.findViewById(R.id.haokan_category_item_image);
                convertView.setTag(holder); 
            }else {
                holder = (ViewHolder) convertView.getTag(); 
            }
            final Category category = list.get(position);
//            holder.favorite.setVisibility(category.isFavorite() ? View.VISIBLE : View.GONE);
            
            holder.favorite
                    .setBackgroundResource(category.isFavorite() ? R.drawable.haokan_category_favorite
                            : R.drawable.haokan_category_favorite_cancel);

            DebugLog.d(TAG,"getView category.getIcon():" + category.getIcon());
            if (category.getIcon() != null) {
                holder.image.setImageBitmap(category.getIcon());
            }else{
            	holder.image.setImageResource(R.drawable.category_loading);
            }
//            holder.image.setImageDrawable(getDrawable(R.drawable.haokan_life));
            
            
//            if (category.getTypeNameResId() != 0) {
//                holder.title.setText(category.getTypeNameResId());
//            }else {
//                holder.title.setText(category.getTypeName());
//            }
            holder.title.setText("");
            if(!TextUtils.isEmpty(category.getNameID())){
            	try {
                	holder.title.setText(getResId(category.getNameID()));
				} catch (Exception e) {
		            DebugLog.d(TAG,"getView setText error:" + e.getStackTrace());
				}
            }else{
            	holder.title.setText(category.getTypeName());
            }
            
            holder.image.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
//                    holder.image.bindClickAnimator();
//                    startAlphaAnim(holder.favorite, !category.isFavorite());
                    onItemClick(position);
                }
            });
            
            return convertView;
        }
       
    }
    
    private void startAlphaAnim(final View view, final boolean visibility) {
        
        float fromAlpha = 1f;
        float toAlpha = 0f;
        if (visibility) {
            fromAlpha = 0f;
            toAlpha = 1f;
        }
        
        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setDuration(100);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation arg0) {
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onAnimationRepeat(Animation arg0) {
                
            }
            
            @Override
            public void onAnimationEnd(Animation arg0) {
                view.setVisibility(visibility ? View.VISIBLE : View.GONE);
            }
        });
        view.startAnimation(alphaAnimation);
        
    }
    

    private final class ViewHolder {
        public TextView title;
        public ImageView favorite;
        public CircleImageView image;
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DebugLog.d(TAG, "onBackPressed");
        finish();
    }
    

    public int getResId(String name) {
    	String defType = "string";
        String packageName = this.getApplicationInfo().packageName;

        return this.getResources().getIdentifier(name, defType, packageName);

    }

    
}
