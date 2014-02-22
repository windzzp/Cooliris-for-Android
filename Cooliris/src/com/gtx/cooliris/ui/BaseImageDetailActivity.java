package com.gtx.cooliris.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.gtx.cooliris.BuildConfig;
import com.gtx.cooliris.R;
import com.gtx.cooliris.app.CoolirisApplication;
import com.gtx.cooliris.bl.ImageDataProvider;
import com.gtx.cooliris.constant.GAConstant;
import com.gtx.cooliris.db.ImagesTableBL;
import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.entity.ImageGroupCategory;
import com.gtx.cooliris.imagecache.ImageCache;
import com.gtx.cooliris.imagecache.ImageFetcher;
import com.gtx.cooliris.imagecache.ImageWorker.ImageWorkerAdapter;
import com.gtx.cooliris.imagecache.Utils;
import com.gtx.cooliris.utils.DownloadHelper;
import com.gtx.cooliris.utils.LogUtil;
import com.gtx.cooliris.utils.SettingsPreferenceMgr;
import com.gtx.cooliris.widget.ImageViewEx;

public class BaseImageDetailActivity extends FragmentActivity 
                                     implements OnClickListener,
                                                ViewPager.OnPageChangeListener {
    public static final String EXTRA_CATEGORY               = "category";
    public static final String EXTRA_GROUP_IX               = "group_ix";
    public static final String EXTRA_ENTER_MODE             = "enter_mode";
    
    protected static final String TAG_IMAGE_DETAIL         = "image_detail_activity";
    protected static final String IMAGE_CACHE_DIR          = "images";
    protected static final int IMAGE_DISK_CACHE_SIZE       = 1024 * 1024 * 60;
    protected static final int SLIDESHOW_DELAY_TIME        = 1000;
    protected static final int ANIM_DURATION_LR            = 300;
    protected static final int ANIM_DURATION_IN_CENTER     = 800;
    
    protected final static int MSG_SWITCH_NEXT             = 1;
    
    protected final static int MENU_SET_WALLPAPER          = 1;
    protected final static int MENU_SET_FAVORITE           = 2;
    protected final static int MENU_DOWNLOAD_SINGLE_IMAGE  = 3;
    protected final static int MENU_DOWNLOAD_GROUP_IMAGES  = 4;
    
    protected ItemPosInfo          	m_imagePosInfo         = null;
    protected ArrayList<ImageGroup>	m_allGroups            = null;
    protected ArrayList<Image> 		m_allImages            = null;
    
    // UI View
    protected ViewPager            	m_pager                = null;
    protected ImageSwitcher        	m_imageSwitcher        = null;

    // Title layout
    protected LinearLayout          m_infoLayout            = null;
    protected TextView              m_titleView             = null;
    protected TextView              m_descriptionView       = null;
    protected TextView              m_timeView              = null;
    protected TextView              m_categoryView          = null;
    protected TextView              m_tagView               = null;

    // Prompt info layout
    protected RelativeLayout        m_promptInfoLayout      = null;
    protected TextView              m_promptInfoNext        = null;
    protected TextView              m_promptInfoTitle       = null;

    protected ImagePagerAdapter     mPagerAdapter           = null;
    protected ImageFetcher          mImageFetcher           = null;
    protected ShareActionProvider   m_actionProvider        = null;
    protected int                   m_currentCategory       = ImageGroupCategory.CATEGORY_ALL;

    // Slide show task
    protected SlideShowTask         m_slideShowTask         = new SlideShowTask();
    protected boolean               m_isSlideShowMode       = false;

    // Next image group AnimatorSet
    private AnimatorSet mPromptNextAnim     = null;
    // Previous image group AnimatorSet
    private AnimatorSet mPromptPreviousAnim = null;
    // Rotate animator
    private AnimatorSet m_rotateAnim        = null;

    // Switch animation transformer 
    protected ZoomOutPageTransformer m_zoomoutPtf      = new ZoomOutPageTransformer();
    protected DepthPageTransformer   m_depthPtf        = new DepthPageTransformer();
    protected PageTransformer        m_currentPtf      = null;

    protected Tracker mGaTracker;

    class ItemPosInfo {
        int itemIndex   = 0;
        int startPos    = 0;
        int endPos      = 0;
        int relativePos = 0;
        int absolutePos = 0;

        @Override
        public String toString() {
            return String.format("mItemIndex = %d, mStartPos = %d, mEndPos = %d, mRelativePos = %d, mAbsolutePos = %d", 
                    itemIndex, startPos, endPos, relativePos, absolutePos);
        }
    }

    @TargetApi(11)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);

        mGaTracker = EasyTracker.getTracker();
        mGaTracker.sendView(GAConstant.SCREEN_DETAIL_IMAGEGROUP);
        
        /*if (Utils.hasActionBar()) {
            //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        }*/
        setContentView(R.layout.image_detail_pager);

        onPrepareData();
        onPrepareCache();
        onInitView();
    }

    protected void onPrepareData() {
        final int groupIx = getIntent().getIntExtra(EXTRA_GROUP_IX, 0);
        m_currentCategory = getIntent().getIntExtra(EXTRA_CATEGORY, ImageGroupCategory.CATEGORY_ALL);

        m_allGroups = ImageDataProvider.getInstance().getImageGroups(m_currentCategory);
        m_allImages = ImageDataProvider.getInstance().getImages(m_currentCategory);

        /*m_imagePosInfo = getPosInfoByGirlIndex(groupIx, m_allGroups);
        m_currentGroup = m_allGroups.get(groupIx);
        m_currentImage = m_currentGroup.getImageList().get(0);
        LogUtil.d(TAG_IMAGE_DETAIL, "Girl ix = " + groupIx + ", Pos info = " + m_imagePosInfo.toString());*/
    }

    protected void onPrepareCache() {
        // Fetch screen height and width, to use as our max size when loading images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our images. As the
        // image scaling ensures the image is larger than this, we should be left with a
        // resolution that is appropriate for both portrait and landscape. For best image quality
        // we shouldn't divide by 2, but this will use more memory and require a larger memory
        // cache.
        final int longest = (height > width ? height : width) / 2;
    
        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.20f); // Set memory cache to 25% of app memory
        cacheParams.diskCacheSize = IMAGE_DISK_CACHE_SIZE;

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, longest);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(true);
        mImageFetcher.setAdapter(ImageDataProvider.getInstance().getImagesAdapter(m_currentCategory));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void onInitView() {
        // Set up ViewPager and backing adapter
        mPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mImageFetcher.getAdapter().getSize());
        m_pager = (ViewPager) findViewById(R.id.pager);
        m_pager.setAdapter(mPagerAdapter);
        // NOTE: We set the page margin into 0 to avoid a bug that display incorrect when rotate screen. 
        m_pager.setPageMargin(0);
        //m_pager.setPageMargin((int) getResources().getDimension(R.dimen.image_detail_pager_margin));
        m_pager.setOffscreenPageLimit(2);
        //m_pager.setCurrentItem(m_girlPosInfo.startPos);
        m_pager.setOnPageChangeListener(this);
        
        // Set abstract title and description
        m_infoLayout      = (LinearLayout) findViewById(R.id.imagegroup_info);
        m_titleView       = (TextView) findViewById(R.id.imagegroup_title);
        m_descriptionView = (TextView) findViewById(R.id.imagegroup_description);
        m_timeView        = (TextView) findViewById(R.id.clock_text);
        m_categoryView    = (TextView) findViewById(R.id.category_text);
        m_tagView         = (TextView) findViewById(R.id.tags_text);
        
        // m_promptInfoLayout
        m_promptInfoLayout= (RelativeLayout) findViewById(R.id.switch_prompt);
        m_promptInfoNext  = (TextView) findViewById(R.id.prompt_info_next);
        m_promptInfoTitle = (TextView) findViewById(R.id.prompt_info_title);
        
        m_imageSwitcher = (ImageSwitcher) findViewById(R.id.image_switcher);
        m_imageSwitcher.setVisibility(View.INVISIBLE);
        m_imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, Utils.hasHoneycomb() ? R.anim.fade_in_anim : R.anim.fade_in_anim_10));
        m_imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, Utils.hasHoneycomb() ? R.anim.fade_out_anim : R.anim.fade_out_anim_10));
        m_imageSwitcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                exitSlideShowMode();
                return true;
            }
        });

        m_imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageViewEx imageView = new ImageViewEx(BaseImageDetailActivity.this);
                imageView.setLayoutParams(new FrameLayout.LayoutParams
                        (LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                return imageView;
            }
        });

        // Reset title view layout and data
        resetTitleLayout();
        resetInfoView();

        final boolean bSlideShowMode = getIntent().getBooleanExtra(EXTRA_ENTER_MODE, false);

        // Enable some additional newer visibility and ActionBar features to create a more
        // immersive photo viewing experience
        if (Utils.hasHoneycomb()) {
            final ActionBar actionBar = getActionBar();

            // Hide title text and set home as up
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

            // Hide and show the ActionBar as the visibility changes
            m_pager.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int vis) {
                            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                                
                                LogUtil.e(TAG_IMAGE_DETAIL, "LOW_PROFILE, hide+++1");
                                showInfoView(false);
                                actionBar.hide();
                            } else {
                                LogUtil.e(TAG_IMAGE_DETAIL, "NOT LOW_PROFILE, show---1");
                                showInfoView(true);
                                actionBar.show();
                                //setNavVisibility(true);
                                //exitSlideShowMode();
                            }
                        }
                    });

            if (bSlideShowMode) {
                // Set up activity to go full screen
                //getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
                // Start low profile mode and hide ActionBar
                m_pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                showInfoView(false);
                actionBar.hide();
            } else {
                // Default show action bar and detail info
                m_pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                showInfoView(true);
                actionBar.show();
            }
            /*setNavVisibility(false);*/
            //{@sample development/samples/ApiDemos/src/com/example/android/apis/view/VideoPlayerActivity.java }
            //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
            //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        }
        
        //mPager.setPageTransformer(true, new DepthPageTransformer());
        //setViewPagerTransformer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onResume() {
        if (m_isSlideShowMode) {
            entrySlideShowMode();
        }
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);

        // If need, reset the view pager transformer
        setViewPagerTransformer();
    }

    @Override
    protected void onPause() {
        if (m_isSlideShowMode) {
            exitSlideShowMode();
        }
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);

        mImageFetcher.closeCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // mImageFetcher.closeCache();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetTitleLayout();
    }

    @Override
    public void onBackPressed() {
        if (m_isSlideShowMode) {
            exitSlideShowMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            break;

        case R.id.menu_favorite:
            onFavoriteButtonClick();
            break;

        case R.id.menu_previous:
            onPreviousButtonClick();
            // showPromptInfo(false);
            break;

        case R.id.menu_next:
            onNextButtonClick();
            break;

        case R.id.menu_slideshow:
            entrySlideShowMode();
            break;

        case R.id.menu_download_single_image:
            // We should use Download Provider
            new MenuOperationAsyncTask().execute(MENU_DOWNLOAD_SINGLE_IMAGE);
            break;

        case R.id.menu_download_group_images:
            new MenuOperationAsyncTask().execute(MENU_DOWNLOAD_GROUP_IMAGES);
            break;

        case R.id.menu_rotate_left:
            rotate(false);
            break;

        case R.id.menu_rotate_right:
            rotate(true);
            break;

        case R.id.menu_set_wallpaper:
            // onSetWallpaper();
            new MenuOperationAsyncTask().execute(MENU_SET_WALLPAPER);
            break;

        case R.id.menu_share_with:
            onShareWith();
            break;

        case R.id.menu_settings:
            // getWindow().clearFlags(LayoutParams.FLAG_FULLSCREEN);
            final Intent intent = new Intent(this, ImagePreferenceActivity.class);
            startActivity(intent);
            break;

        default:
            break;
        }

        /*mGaTracker.sendEvent(
                GAConstant.EVENT_CATEGORY_UI_ACTION, 
                GAConstant.EVENT_ACTION_PRESS_MENU, 
                item.getTitle().toString(), 0L);*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_girl_images, menu);

        // Initialize action provider
        initActionProvider(menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isFavorite = getCurrentImage().isFavorite();

        MenuItem item = menu.getItem(0);
        item.setIcon(isFavorite ? R.drawable.ic_rating_important : R.drawable.ic_rating_not_important);

        // return true;
        return super.onPrepareOptionsMenu(menu);
    }
    
    /**
     * Set on the ImageView in the ViewPager children fragments, to enable/disable low profile mode
     * when the ImageView is touched.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onClick(View v) {
        final int vis = m_pager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
            m_pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            /*mPager.setSystemUiVisibility( 
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | 
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                    View.SYSTEM_UI_FLAG_LOW_PROFILE | 
                    View.SYSTEM_UI_FLAG_FULLSCREEN | 
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
        } else {
            m_pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        // exitSlideShowMode();
    }

    @Override
    public void onPageSelected(int position) {
        /** NOTE: 
         * It seems the invalidateOptionsMenu(...) 'll take too much time.
         * We move it into onPageScrollStateChanged(...)
         */
        //LogUtil.e(TAG_IMAGE_DETAIL, "onPageSelected, pos = " + position);
        /*m_infoLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
                resetInfoView();
            }
        }, 500);*/

        /*invalidateOptionsMenu();
        resetInfoView();*/
        /*if (position >= m_imagePosInfo.startPos 
                && position <= m_imagePosInfo.endPos) {
            refreshTitle(getCurrentGroup());
        }*/
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        LogUtil.d(TAG_IMAGE_DETAIL, "onPageScrolled" +
                ", position = " + position + 
                ", positionOffset = " + positionOffset + 
                ", positionOffsetPixels" + positionOffsetPixels);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        LogUtil.d(TAG_IMAGE_DETAIL, "onPageScrollStateChanged, state = " + state);
        
        // SCROLL_STATE_IDLE = 0
        // SCROLL_STATE_DRAGGING = 1
        // SCROLL_STATE_SETTLING = 2

        // Normal drag: if not to end
        // 1. onPageScrollStateChanged, state = 1
        // 2. onPageScrolled, position = 10, positionOffset = 0.005051033, positionOffsetPixels7
        // 3. onPageScrollStateChanged, state = 2
        // 4. onPageSelected, position = 11
        // 5. onPageScrolled, position = 10, positionOffset = 0.3730164, positionOffsetPixels517
        // 6. onPageScrollStateChanged: onPageScrollStateChanged, state = 0
        
        // Edge drag: if to end or begin (DO NOT trigger onPageSelected(...))
        // 1. onPageScrollStateChanged, state = 1
        // 2. onPageScrolled, position = 10, positionOffset = 0.005051033, positionOffsetPixels7
        // 3. onPageScrollStateChanged: onPageScrollStateChanged, state = 0

        if (ViewPager.SCROLL_STATE_IDLE == state) {
            final int index = m_pager.getCurrentItem();

            // For favorite group, the m_imagePosInfo is null
            if (null != m_imagePosInfo) {
                if (index < m_imagePosInfo.startPos) {
                    onGuesture(false);
                } else if (index > m_imagePosInfo.endPos) {
                    onGuesture(true);
                }
            }

            invalidateOptionsMenu();
            resetInfoView();
        }
    }

    /**
     * Called by the ViewPager child fragments to load images via the one ImageFetcher
     */
    public ImageFetcher getImageFetcher() {
        return mImageFetcher;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void initActionProvider(Menu menu) {
        // TODO: Add the Search view and share action.
        /*SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);*/
    
        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_share_with);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(getFileStreamPath("ic_launcher.png"));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        actionProvider.setShareIntent(shareIntent);
        m_actionProvider = actionProvider;
    }

    /**
     * Please see {@link #Using ViewPager for Screen Slides}
     * {@link #http://developer.android.com/intl/zh-CN/training/animation/screen-slide.html#zoom-out}
     */
    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            // LogUtil.d(TAG_IMAGE_DETAIL, "View = " + view + ", pos = " + position);

            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                        (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
    
    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
        	
        	//LogUtil.d(TAG_IMAGE_DETAIL, "View = " + view + ", pos = " + position);
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
    
    /**
     * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
     * could be a large number of items in the ViewPager and we don't want to retain them all in
     * memory at once but create/destroy them on the fly.
     */
    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final int mSize;
        private ImageDetailFragment mCurrentView = null;

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }
        
        /*public void reset(int size) {
            mSize = size;
        }*/

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            // return ImageDetailFragment.newInstance((String)
            // mImageFetcher.getAdapter().getItem(position));
            return ImageDetailFragment.newInstance(position);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentView = (ImageDetailFragment) object;
            super.setPrimaryItem(container, position, object);
        }

        public ImageDetailFragment getCurrentView() {
            return mCurrentView;
        }
    }

    protected Image getCurrentImage() {
        int index = m_pager.getCurrentItem();
        Image image = m_allImages.get(index);

        return image;
    }

    protected ImageGroup getCurrentGroup() {
        Image image = getCurrentImage();
        return image.getParent();
    }

    protected void onFavoriteButtonClick() {
        Image currentImage = getCurrentImage();
        currentImage.setIsFavorite(!currentImage.isFavorite());
        new MenuOperationAsyncTask().execute(MENU_SET_FAVORITE);
        invalidateOptionsMenu();
    }

    protected void onPreviousButtonClick() {
        scrollToNextInternal(false, true);
    }

    protected void onNextButtonClick() {
        scrollToNextInternal(true, true);
    }

    protected void onGuesture(boolean bNext) {
        // It's trigger by gesture, We do NOT reset the current item
        scrollToNextInternal(bNext, false);
    }

    protected void onSlideShowNext() {
        int nextImageIx = getNextSlideShowIndex();

        // Slide show to next item.
        m_pager.setCurrentItem(nextImageIx, false);

        ImageViewEx imageView = (ImageViewEx) m_imageSwitcher.getNextView();
        Image image = (Image) mImageFetcher.getAdapter().getOriginalItem(nextImageIx);
        imageView.setDataSource(image);
        mImageFetcher.loadImage(image.getImageUrl(), imageView);
        m_imageSwitcher.showNext();
    }

    /**
     * Abstract method.
     * @return
     */
    protected int getNextSlideShowIndex() {
        // Reset girl info
        final int totalSize = mImageFetcher.getAdapter().getSize();
        int nextImageIx = m_pager.getCurrentItem() + 1;
        nextImageIx = (nextImageIx < totalSize) ? nextImageIx : 0;

        final ImageGroup currentGroup = getCurrentGroup();
        final ArrayList<ImageGroup> girls = m_allGroups;
        final int curGirlIx = m_imagePosInfo.itemIndex;
        int nextGirlIx = m_imagePosInfo.itemIndex;
        if (nextImageIx > m_imagePosInfo.endPos) {
            nextGirlIx = curGirlIx + 1;
        } else if (0 == nextImageIx) {
            nextGirlIx = 0;
        }

        if (nextGirlIx != m_imagePosInfo.itemIndex) {
            final ImageGroup nextGirl = girls.get(nextGirlIx);
            m_imagePosInfo.itemIndex = nextGirlIx;
            
            m_imagePosInfo.startPos = (0 == nextGirlIx) ? 0 : (m_imagePosInfo.startPos + currentGroup.getImageList().size());
            m_imagePosInfo.endPos = m_imagePosInfo.startPos + nextGirl.getImageList().size() - 1;
        }

        return nextImageIx;
    }

    protected void onShareWith() {
        if (null != m_actionProvider) {
            Image currentImage = getCurrentImage();
            if (null != currentImage) {
                String filePath = currentImage.getImageDownloadPath();
                File imageFile = new File(filePath);
                if (null != imageFile && imageFile.exists()) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/*");

                    Uri uri = Uri.fromFile(imageFile);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    m_actionProvider.setShareIntent(shareIntent);
                    LogUtil.i(TAG_IMAGE_DETAIL, "Uri = " + uri + ", filePath = " + filePath);

//                    Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//                    shareIntent.setType("image/*;text/plain");
//                    // shareIntent.setType("*/*");
//                    // shareIntent.setType("application/octet-stream");
//
//                    Uri uri = Uri.fromFile(imageFile);
//                    ArrayList<Uri> uris = new ArrayList<Uri>();
//                    uris.add(uri);
//                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
//
//                    ArrayList<String> extra_text = new ArrayList<String>();
//                    extra_text.add("See attached CSV files.");
//                    shareIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extra_text);
//                    /*ArrayList<CharSequence> texts = new ArrayList<CharSequence>(); 
//                     texts.add("ABC");
//                     shareIntent.putCharSequenceArrayListExtra(Intent.EXTRA_TEXT, texts);*/
//
//                    m_actionProvider.setShareIntent(shareIntent);
//                    LogUtil.i(TAG_IMAGE_DETAIL, "Uri = " + uri + ", filePath = " + filePath);
                }
            }
        }
    }

    protected void onDownloadSingleImage() {
        ImageGroup currentGroup = getCurrentGroup();
        final int ix = m_pager.getCurrentItem() - m_imagePosInfo.startPos;
        LogUtil.d(TAG_IMAGE_DETAIL, "download image ix = " + ix + ", title = " + currentGroup.getTitle());

        DownloadHelper.downloadGirlImage(currentGroup, ix);
    }

    protected void onDownloadGroupImages() {
        ImageGroup currentGroup = getCurrentGroup();
        final int ix = m_pager.getCurrentItem() - m_imagePosInfo.startPos;
        LogUtil.d(TAG_IMAGE_DETAIL, "download image ix = " + ix + ", title = " + currentGroup.getTitle());

        DownloadHelper.downloadGirlImagesInGroup(currentGroup);
    }

    @SuppressLint("ServiceCast")
    private void onSetWallpaper() {
        try {
            long start = System.currentTimeMillis();

            // Find current display image view
            final ImageView v = mPagerAdapter.getCurrentView().getImageView();
            final Drawable drawable = v.getDrawable();
            Bitmap bmp = null;

            if (drawable instanceof BitmapDrawable) {
                bmp = ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof TransitionDrawable) {
                BitmapDrawable bmpDrawable = (BitmapDrawable) ((TransitionDrawable) drawable).getDrawable(1);
                bmp = ((BitmapDrawable) bmpDrawable).getBitmap();
            }

            WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
            wpm.setBitmap(bmp);

            LogUtil.d(TAG_IMAGE_DETAIL, "takes time = " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            LogUtil.e(TAG_IMAGE_DETAIL, "Failed to set wallpaper: " + e);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void rotate(boolean rotateToRight)
    {
        // Currently, we do NOT re-use the m_rotateAnim, because if we frequently rotate the ImageView, 
        // it'll become slowly. That because the animator MAP in m_rotateAnim is very large, it
        // influence the performance of the animation.
        // We can get the same use from: http://developer.android.com/intl/zh-CN/training/animation/zoom.html
        // Also, we can use another way to do it, re-use the ObjectAnimator, such as: 
        //      ObjectAnimator anim = ObjectAnimator.ofFloat(v, View.SCALE_X, curScaleX, destScaleX)
        //      anim.setFloatValues(curScaleX, destScaleX);
        if (null != m_rotateAnim) {
            m_rotateAnim.end();
        }

        final ImageViewEx v = (ImageViewEx) mPagerAdapter.getCurrentView().getImageView();
        if (v.getRotation() % 360 == 0) {
            v.setRotation(0);
        }

        Bitmap bmp = null;
        final Drawable drawable = v.getDrawable();
        if (null == drawable) {
            return;
        }

        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof TransitionDrawable) {
            BitmapDrawable bmpDrawable = (BitmapDrawable) ((TransitionDrawable) drawable).getDrawable(1);
            bmp = ((BitmapDrawable) bmpDrawable).getBitmap();
        }

        float[] imageDimensions = { 1, 1 };
        float imageAspectRatio = 1.0f;
        if (null != bmp) {
            imageDimensions[0] = bmp.getWidth();
            imageDimensions[1] = bmp.getHeight();
            imageAspectRatio = imageDimensions[0] / imageDimensions[1];
        }

        final float viewPortW = m_pager.getWidth();
        final float viewPortH = m_pager.getHeight();
        final float bmpW = imageDimensions[0];
        final float bmpH = imageDimensions[1];
        final float aspectRatio = (bmpW * viewPortH > bmpH * viewPortW) ? imageAspectRatio : viewPortW / viewPortH;
        LogUtil.i(TAG_IMAGE_DETAIL, "Dimension = (" + imageDimensions[0] + ", " + imageDimensions[1] + ")" + 
                "View width = (" + viewPortW + ", " + viewPortH + ") too Tall ? = " + !(bmpW * viewPortH > bmpH * viewPortW)
                + ", aspectRation = " + aspectRatio);

        final float curRotation = v.getRotation();
        final float destRotation = rotateToRight ? (curRotation + 90) : (curRotation - 90);
        final float curScaleX = v.getScaleX();
        final float curScaleY = v.getScaleY();
        final float destScaleX = (1 == curScaleX) ? aspectRatio : 1;
        final float destScaleY = (1 == curScaleY) ? aspectRatio : 1;

        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                m_rotateAnim = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                m_rotateAnim = null;
            }
        });
        set.play(ObjectAnimator.ofFloat(v, View.SCALE_X, curScaleX, destScaleX))
                .with(ObjectAnimator.ofFloat(v, View.SCALE_Y, curScaleY, destScaleY))
                .with(ObjectAnimator.ofFloat(v, View.ROTATION, curRotation, destRotation));
        set.start();
        m_rotateAnim = set;
    }
    
    protected int getActionBarHeight()
    {
        if (Utils.hasHoneycomb())
        {
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (this.getTheme().resolveAttribute(
                    android.R.attr.actionBarSize, tv, true)) {
                return TypedValue.complexToDimensionPixelSize(
                        tv.data, this.getResources().getDisplayMetrics());
            }
        }
        
        return 0;
    }

    protected void setViewPagerTransformer() {
        if (null == m_pager) {
            return;
        }

        int type = CoolirisApplication.getSlideShowType();
        PageTransformer pageTransformer = null;
        switch (type) {
        case SettingsPreferenceMgr.SWITCH_EFFECT_DEFAULT:
            pageTransformer = null;
            break;

        case SettingsPreferenceMgr.SWITCH_EFFECT_ZOOMOUT:
            pageTransformer = m_zoomoutPtf;
            break;

        case SettingsPreferenceMgr.SWITCH_EFFECT_DEPTH:
            pageTransformer = m_depthPtf;
            break;

        default:
            break;
        }

        if (m_currentPtf != pageTransformer) {
            m_currentPtf = pageTransformer;
            m_pager.setPageTransformer(true, m_currentPtf);
        }
    }

    protected void scrollToNextInternal(boolean bNext, boolean bResetItem) {
        final ImageGroup currentGroup = getCurrentGroup();
        final ArrayList<ImageGroup> groups = m_allGroups;
        final int size = groups.size();
        final int curGroupIx = m_imagePosInfo.itemIndex;
        final int nextGroupIx = bNext ? (curGroupIx + 1) : (curGroupIx - 1);
        if (nextGroupIx < 0 || nextGroupIx >= size) {
            Toast.makeText(this, R.string.toast_no_more_images, Toast.LENGTH_SHORT).show();
            return;
        }

        final ImageGroup nextGroup = groups.get(nextGroupIx);
        m_imagePosInfo.itemIndex = nextGroupIx;
        m_imagePosInfo.startPos = bNext ? 
                (m_imagePosInfo.startPos + currentGroup.getImageList().size()) : 
                (m_imagePosInfo.startPos - nextGroup.getImageList().size());
        m_imagePosInfo.endPos = m_imagePosInfo.startPos + nextGroup.getImageList().size() - 1;

        if (bResetItem) {
            m_pager.setCurrentItem(m_imagePosInfo.startPos, false);
        }

        // Refresh title info and invalidate options Menu state
        resetInfoView();
        invalidateOptionsMenu();

        Toast.makeText(this, bNext ? R.string.menu_next : R.string.menu_previous, Toast.LENGTH_SHORT).show();
    }

    protected ItemPosInfo getPosInfoBySelctedIndex(int pos, ArrayList<ImageGroup> girls) {
        ItemPosInfo posInfo = new ItemPosInfo();
        if (null == girls) {
            return posInfo;
        }

        final int size = girls.size();
        int offset = 0;
        int imageSize = 0;
        for (int ix = 0; ix < size; ix++) {
            imageSize = girls.get(ix).getImageList().size();
            offset += imageSize;
            if (offset >= pos + 1) {
                posInfo.startPos    = offset - imageSize;
                posInfo.endPos      = offset - 1;
                posInfo.relativePos = pos - posInfo.startPos;
                posInfo.absolutePos = pos;
                posInfo.itemIndex   = ix;

                return posInfo;
            }
        }

        return posInfo;
    }

    protected ItemPosInfo getPosInfoByGroupIndex(int girlIx, ArrayList<ImageGroup> groups) {
        ItemPosInfo posInfo = new ItemPosInfo();
        if (null == groups) {
            return posInfo;
        }

        final int size = groups.size();
        if (girlIx < 0 || girlIx > size - 1) {
            LogUtil.e(TAG_IMAGE_DETAIL, "index error!");
            return posInfo;
        }

        int offset = 0;
        int imageSize = 0;
        for (int ix = 0; ix <= girlIx; ix++) {
            imageSize = groups.get(ix).getImageList().size();
            offset += imageSize;
            if (ix == girlIx) {
                posInfo.startPos    = offset - imageSize;
                posInfo.endPos      = offset - 1;
                posInfo.relativePos = 0;
                posInfo.absolutePos = posInfo.startPos;
                posInfo.itemIndex   = girlIx;

                return posInfo;
            }
        }

        return posInfo;
    }

    protected void showInfoView(boolean bShow) {
        boolean isVisiable = (m_infoLayout.getVisibility() == View.VISIBLE);
        if (isVisiable != bShow) {
            m_infoLayout.setVisibility(bShow ? View.VISIBLE : View.INVISIBLE);

            if (bShow) {
                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(500);
                m_infoLayout.startAnimation(animation);
            } else {
                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(500);
                m_infoLayout.startAnimation(animation);
            }
        }
    }

    protected void resetTitleLayout() {
        // If API level >= 11, we can use {android:layout_marginTop="?android:attr/actionBarSize"}
        // to set the LinearLayout margin in layout xml.
        if (Utils.hasHoneycomb()) {
            ((FrameLayout.LayoutParams) m_infoLayout.getLayoutParams()).topMargin = getActionBarHeight();
        }
    }

    protected void resetInfoView() {
        int index = m_pager.getCurrentItem();
        Image image = m_allImages.get(index);
        ImageGroup group = image.getParent();

        Object tag = m_infoLayout.getTag();
        if (null == tag || (tag instanceof ImageGroup && tag != group)) {
            m_infoLayout.setTag(group);

            refreshTitle(group);
            refreshDescription(group);
        }
    }

    protected void refreshDescription(ImageGroup group) {
        if (null != group) {
            m_timeView.setText(group.getDate());
            m_categoryView.setText(group.getCategory().trim());
            m_tagView.setText(group.getTag());
            m_descriptionView.setText(group.getDescription());
        } else {
            LogUtil.d(TAG_IMAGE_DETAIL, "The image's favorite is null!!");
        }
    }

    protected void refreshTitle(ImageGroup group) {
        if (null != group) {
            final String absTitle = group.getTitle();

            if (!TextUtils.isEmpty(absTitle)) {
                m_titleView.setText(absTitle);
            }
        } else {
            LogUtil.d(TAG_IMAGE_DETAIL, "The image's favorite is null!!");
        }
    }

    private void showPromptInfo(boolean next) {

        if (null == mPromptNextAnim || null == mPromptPreviousAnim) {
            initPromptInfoAnim(false);
        }

        if (mPromptNextAnim.isStarted() || mPromptPreviousAnim.isStarted()) {
            return;
        }

        String nextInfo = getString(next ? R.string.msg_next_image_group : R.string.msg_previous_image_group);
        String title = getCurrentGroup().getTitle();

        m_promptInfoNext.setText(nextInfo);
        m_promptInfoTitle.setText(title);
        m_promptInfoLayout.setVisibility(View.VISIBLE);

        AnimatorSet animSet = next ? mPromptNextAnim : mPromptPreviousAnim;
        animSet.start();
    }

    private void initPromptInfoAnim(boolean needReset) {

        int width = m_promptInfoLayout.getWidth();

        if (null == mPromptNextAnim || needReset) {
            ValueAnimator R2C = ObjectAnimator.ofFloat(m_promptInfoLayout, "x", width, 0);
            R2C.setDuration(ANIM_DURATION_LR);
            R2C.setInterpolator(new AccelerateDecelerateInterpolator());

            ValueAnimator center = ObjectAnimator.ofFloat(m_promptInfoLayout, "x", 0, 0);
            center.setDuration(ANIM_DURATION_IN_CENTER);

            ValueAnimator C2L = ObjectAnimator.ofFloat(m_promptInfoLayout, "x", 0, -width);
            C2L.setDuration(ANIM_DURATION_LR);
            C2L.setInterpolator(new AccelerateDecelerateInterpolator());

            mPromptNextAnim = new AnimatorSet();
            mPromptNextAnim.play(center).after(R2C);
            mPromptNextAnim.play(C2L).after(center);

            mPromptNextAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_pager.setCurrentItem(m_pager.getCurrentItem() + 1);
                    m_promptInfoLayout.setVisibility(View.INVISIBLE);
                }
            });
        }

        if (null == mPromptPreviousAnim || needReset) {
            ValueAnimator L2C = ObjectAnimator.ofFloat(m_promptInfoLayout, "x", -width, 0);
            L2C.setDuration(ANIM_DURATION_LR);
            L2C.setInterpolator(new AccelerateDecelerateInterpolator());

            ValueAnimator center = ObjectAnimator.ofFloat(m_promptInfoLayout, "x", 0, 0);
            center.setDuration(ANIM_DURATION_IN_CENTER);

            ValueAnimator C2R = ObjectAnimator.ofFloat(m_promptInfoLayout, "x", 0, width);
            C2R.setDuration(ANIM_DURATION_LR);
            C2R.setInterpolator(new AccelerateDecelerateInterpolator());

            mPromptPreviousAnim = new AnimatorSet();
            mPromptPreviousAnim.play(center).after(L2C);
            mPromptPreviousAnim.play(C2R).after(center);

            mPromptPreviousAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_promptInfoLayout.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @SuppressLint("InlinedApi")
    protected void entrySlideShowMode() {
        if (!m_isSlideShowMode) {
            m_isSlideShowMode = true;
            int period = new SettingsPreferenceMgr(this).getSlideshowInterval() * 1000;
            m_slideShowTask.startSchedule(period, period);

            m_pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            Toast.makeText(this, R.string.toast_entry_slideshow_mode, Toast.LENGTH_LONG).show();

            // Set image into image switcher
            final ImageView v = mPagerAdapter.getCurrentView().getImageView();
            final Drawable drawable = v.getDrawable();
            final ImageViewEx imageView = (ImageViewEx) m_imageSwitcher.getCurrentView();
            imageView.setImageDrawable(drawable);
            m_imageSwitcher.setVisibility(View.VISIBLE);

            // It needs to set view pager invisible with delay time.
            // Otherwise, the SYSTEM_UI_FLAG_LOW_PROFILE cannot work.
            /*m_pager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_pager.setVisibility(View.INVISIBLE);
                }
            }, 1000);*/

            // Keep screen on when slide show.
            keepScreenState(true);
        }
    }

    protected void exitSlideShowMode() {
        if (m_isSlideShowMode) {
            m_isSlideShowMode = false;
            m_slideShowTask.cancelSchedule();

            resetInfoView();
            Toast.makeText(this, R.string.toast_exit_slideshow_mode, Toast.LENGTH_LONG).show();

            m_imageSwitcher.setVisibility(View.INVISIBLE);
            m_pager.setVisibility(View.VISIBLE);
            m_pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            // Remove screen on state
            keepScreenState(false);
        }
    }

    private void keepScreenState(boolean screenOn) {
        if (screenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected Handler m_handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SWITCH_NEXT:
                onSlideShowNext();
                break;

            default:
                break;
            }
        }
    };

    protected class SlideShowTask {
        private static final int DELAY_TIME = 1000 * 5;
        private static final int PERIOD_TIME = 1000 * 5;

        private Timer m_timer = null;
        private int m_count = 0;

        public void startSchedule() {
            startSchedule(DELAY_TIME, PERIOD_TIME);
        }

        public void startSchedule(long delay, long period) {
            cancelSchedule();

            m_timer = new Timer();
            m_timer.schedule(new SlideShowSchedule(), delay, period);
        }

        public void cancelSchedule() {
            if (null != m_timer) {
                m_timer.cancel();
                m_timer.purge();
                m_timer = null;
            }

            m_count = 0;
        }

        class SlideShowSchedule extends TimerTask {
            @Override
            public void run() {
                m_handler.obtainMessage(MSG_SWITCH_NEXT).sendToTarget();

                m_count++;
                LogUtil.e("SlideShow", "Count = " + m_count);
            }
        }
    }

    private class MenuOperationAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            int messageId = params[0];

            switch (messageId) {
            case MENU_SET_WALLPAPER:
                onSetWallpaper();
                break;

            case MENU_SET_FAVORITE:
                ImagesTableBL.getInstance().updateFavorite(getCurrentImage());
                break;

            case MENU_DOWNLOAD_SINGLE_IMAGE:
                onDownloadSingleImage();
                break;

            case MENU_DOWNLOAD_GROUP_IMAGES:
                onDownloadGroupImages();
                break;

            default:
                break;
            }

            return messageId;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            String toastInfo = "";
            switch (result) {
            case MENU_SET_WALLPAPER:
                toastInfo = getString(R.string.toast_set_wallpaper);
                break;

            case MENU_SET_FAVORITE:
                toastInfo = getString(getCurrentImage().isFavorite() ? 
                        R.string.toast_add_to_favorite : R.string.toast_remove_from_favorite);
                break;

            case MENU_DOWNLOAD_SINGLE_IMAGE:
                toastInfo = getString(R.string.toast_download_single_image);
                break;

            case MENU_DOWNLOAD_GROUP_IMAGES:
                toastInfo = getString(R.string.toast_download_group_images);
                break;

            default:
                break;
            }

            if (!TextUtils.isEmpty(toastInfo)) {
                Toast.makeText(BaseImageDetailActivity.this, toastInfo, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
