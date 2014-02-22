package com.gtx.cooliris.ui;

import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.gtx.cooliris.BuildConfig;
import com.gtx.cooliris.R;
import com.gtx.cooliris.bl.ImageDataProvider;
import com.gtx.cooliris.constant.GAConstant;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.entity.ImageGroupCategory;
import com.gtx.cooliris.imagecache.ImageCache.ImageCacheParams;
import com.gtx.cooliris.imagecache.ImageFetcher;
import com.gtx.cooliris.imagecache.ImageWorker.ImageWorkerAdapter;
import com.gtx.cooliris.imagecache.Utils;
import com.gtx.cooliris.utils.LogUtil;
import com.gtx.cooliris.utils.NetworkHelper;
import com.gtx.cooliris.widget.ImageGroupView;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly as the user rotates the device.
 */
public class ImageGridFragment extends Fragment 
                               implements AdapterView.OnItemClickListener, 
                               OnItemSelectedListener, 
                               ImageDataProvider.IDataLoadListener
{
    private static final String TAG = "ImageGridFragment";
    
    // Default disk cache size
    private static final String IMAGE_CACHE_DIR 	   = "thumbs";
    private static final int 	THUMBS_DISK_CACHE_SIZE = 1024 * 1024 * 30;
    // Default load data count one time
    private static final int 	DEFAULT_REQUEST_COUNT  = 50;
    private static final int	SCROLL_DURATION_FOR_ROW= 150;
    private static final int	WIFI_INVALIDE_TOAST_DELAY = 3000;
    private static final int	REQUEST_CODE_REFRESH_FAVORITE = 1;
    
    
    private int 			 mImageThumbSize;
    private int 			 mImageThumbSpacing;
    private ImageGridAdapter mImageGridAdapter;
    private ImageFetcher 	 mImageFetcher;
    
    private GridView 		 mGridView;
    private Spinner  		 mSpinner;
    private RelativeLayout 	 mBackgroundLayout;
    
    private int				 mCurrentCategory = ImageGroupCategory.CATEGORY_ALL;
    private boolean 		 mIsLoadingMoreData = false;
    
    private Tracker 		 mGaTracker;
    
    /*private int m_oldImagesPos = 0;
    private int m_oldImagesY = 0;
    private int m_oldFavsPos = 0;
    private int m_oldFavsY = 0;*/

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //setRetainInstance(true);
        mGaTracker = EasyTracker.getTracker();
        mGaTracker.sendView(GAConstant.SCREEN_HOME_IMAGEGROUPS);
        
        // Load image group data asynchronously
        ImageDataProvider.getInstance().loadImageGroupsAsync(
        		mCurrentCategory, DEFAULT_REQUEST_COUNT, true, this);
        
        // Initialize image cache
        initImageCache();
    }

	@Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        mBackgroundLayout = (RelativeLayout) v.findViewById(R.id.backgroundLayout);
        mGridView.setAdapter(mImageGridAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(mScrollListener);
                
		// Initialize the gridview's columns 
        initGridViewColumns();
        
        // Initialize action bar
        initActionBar();
        
        return v;
    }
    
    @Override
    public void onLoadImageGroupsFinished(boolean hasMoreData) {
    	dismissDialog();
		if (NetworkHelper.isMobileNetwork(getActivity())) {
			mGridView.postDelayed(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), 
							getString(R.string.toast_no_wifi), Toast.LENGTH_LONG).show();
				}
			}, WIFI_INVALIDE_TOAST_DELAY);
		}
        
        // Reset the grid view's adapter
        mBackgroundLayout.setVisibility(View.INVISIBLE);
        
        //m_gridView.setAdapter(mAdapter);
        
        if (ImageGroupCategory.CATEGORY_FAVOTITE == mCurrentCategory) {
        	mImageFetcher.setAdapter(ImageDataProvider.getInstance().getImagesAdapter(mCurrentCategory));
		} else {
			mImageFetcher.setAdapter(ImageDataProvider.getInstance().getImageGroupsAdapter(mCurrentCategory));
		}
        mImageGridAdapter.notifyDataSetChanged();
        
        //m_gridView.smoothScrollToPosition(0);
        /*if (m_currentCategory != 0) {
        	scrollGirlsToTop();
		}*/
        
        mIsLoadingMoreData = false;
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Reset the columns count, that will make the grid view re-layout
		initGridViewColumns();

		LogUtil.e(TAG, "onConfigurationChanged...");
	}

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        
        // Sometimes, it isn't refresh the UI, so we have to refresh manually 
        mImageGridAdapter.notifyDataSetChanged();        
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
		case REQUEST_CODE_REFRESH_FAVORITE:
			// If current category collection is favorite, when resume from Detail Activity, 
	        // we need to refresh the data set because the favorite image collection may changed.
	        if (ImageGroupCategory.CATEGORY_FAVOTITE == mCurrentCategory) {
	        	ImageDataProvider.getInstance().recollectFavoriteImages();
			}
			break;

		default:
			break;
		}
    }
	
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		if (v instanceof ImageGroupView) {
			ImageGroupView imageViewEx = (ImageGroupView) v;
			Object tagData = imageViewEx.getDataSource();
			
			if (tagData instanceof ImageGroup
					&& ImageGroupCategory.CATEGORY_FAVOTITE != mCurrentCategory) {
				onClickGirlGroup(v, id, (ImageGroup) tagData);
			} else {
				onClickFavImage(v, id);
			}
		}
    }

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_girls, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
                
            case R.id.menu_to_top:
                scrollToTop(SCROLL_DURATION_FOR_ROW);
                break;
                
            case R.id.menu_to_bottom:
                scrollToBottom(SCROLL_DURATION_FOR_ROW);
                break;
                
            case R.id.menu_slideshow:
                onSlideShow();
            	break;
            	
            case R.id.menu_settings:
                final Intent intent = new Intent(getActivity(), ImagePreferenceActivity.class);
                startActivity(intent);
                /*return true;*/
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
    
    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     *
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    @Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
	{
        LogUtil.d(TAG, "Spinner1: position=" + position + " id=" + id);
        
        int selectCategory = (int)id;
        if (selectCategory != mCurrentCategory) {
        	// Set the current category
        	mCurrentCategory = selectCategory;
        	
        	// Get the data adapter
        	ImageWorkerAdapter dataAdapter = null;
            if (ImageGroupCategory.CATEGORY_FAVOTITE == mCurrentCategory) {
            	if (ImageDataProvider.getInstance().hasInitialize(mCurrentCategory)) {
            		ImageDataProvider.getInstance().recollectFavoriteImages();
				}
            	
            	dataAdapter = ImageDataProvider.getInstance().getImagesAdapter(mCurrentCategory);
    		} else {
    			dataAdapter = ImageDataProvider.getInstance().getImageGroupsAdapter(mCurrentCategory);
    		}
        	
        	// If the data is not prepare, show a loading dialog,
        	// Else, reset UI.
        	if (null == dataAdapter) {
        		showDialog();
                ImageDataProvider.getInstance().loadImageGroupsAsync(
                		mCurrentCategory, DEFAULT_REQUEST_COUNT, true, this);
			} else {
	        	mImageFetcher.setAdapter(dataAdapter);
				mImageGridAdapter.notifyDataSetChanged();	
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		LogUtil.d(TAG, "Spinner1: unselected");
	}

	private void initImageCache() {
		// Initialize the image grid layout info
		mImageGridAdapter 	= new ImageGridAdapter(getActivity());
		mImageThumbSize 	= getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
	    mImageThumbSpacing 	= getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
	    LogUtil.d(TAG, "mImageThumbSize = " + mImageThumbSize + ", mImageThumbSpacing = " + mImageThumbSpacing);
	
	    // Initialize image cache
	    ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
	    // Set memory cache to 25% of app memory
	    cacheParams.setMemCacheSizePercent(0.25f);
	    cacheParams.diskCacheSize = THUMBS_DISK_CACHE_SIZE;
	
	    // The ImageFetcher takes care of loading images into our ImageView children asynchronously
	    mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
	    // Set empty adapter when initialize
	    mImageFetcher.setAdapter(ImageDataProvider.EMPTY_ADAPTER);
	    //mImageFetcher.setLoadingImage(R.drawable.empty_photo);
	    mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
	}

	private void initGridViewColumns() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int widthPixels = dm.widthPixels;
        LogUtil.d("DisplayMetrics", "DisplayMetrics = " + dm.toString());
        
		mImageThumbSize 	= getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
	    mImageThumbSpacing 	= getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        final int numColumns = (int) Math.floor(widthPixels / (mImageThumbSize + mImageThumbSpacing));
        final int curColumns = mImageGridAdapter.getNumColumns();
        mImageGridAdapter.resetActionBarHeight();
        
        if (curColumns == 0 || curColumns != numColumns) {
            
            LogUtil.d(TAG, 
                    "numColumns = " + numColumns + 
                    ", mGridView.getWidth() = " + widthPixels + 
                    ", mImageThumbSize = " + mImageThumbSize + 
                    ", mImageThumbSpacing = " + mImageThumbSpacing);
            
            if (numColumns > 0) {
                final int columnWidth = (widthPixels / numColumns) - mImageThumbSpacing;
                mImageGridAdapter.setNumColumns(numColumns);
                mImageGridAdapter.setItemHeight(columnWidth);
                mImageThumbSize = columnWidth;
                LogUtil.d(TAG, "numColumns = " + numColumns + ", columnWidth = " + columnWidth);
                
                if (BuildConfig.DEBUG) {
                    LogUtil.d(TAG, "onCreateView - numColumns set to " + numColumns);
                }
            }
        }
    }

    private void initActionBar() {
		// Add a action bar on left
	    if (Utils.hasHoneycomb()) {
	        final ActionBar actionBar = getActivity().getActionBar();
	        actionBar.setTitle("");
	        
	        RelativeLayout relative = new RelativeLayout(getActivity());
	        mSpinner = new Spinner(getActivity());
	        mSpinner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
	        //spinner.setGravity(Gravity.LEFT);
	        relative.addView(mSpinner);
	        actionBar.setDisplayShowCustomEnabled(true);
	        actionBar.setCustomView(relative);
	        
	        // Set adapter and listener
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), 
	        		R.array.category, /*android.R.layout.simple_spinner_item*/R.layout.spinner_item);
	        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item/*android.R.layout.simple_spinner_dropdown_item*/);
	        mSpinner.setAdapter(adapter);
	        mSpinner.setOnItemSelectedListener(this);
	    }
	}

	private OnScrollListener mScrollListener = new OnScrollListener()
	{
	    @Override
	    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
	        // Pause fetcher to ensure smoother scrolling when flinging
	        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
	            mImageFetcher.setPauseWork(true);
	        } else {
	            mImageFetcher.setPauseWork(false);
	        }
	        
	        if (OnScrollListener.SCROLL_STATE_IDLE == scrollState)
	        {
	            int firstVisiblePos = absListView.getFirstVisiblePosition();
	            int lastVisiblePos = absListView.getLastVisiblePosition();
	            View lastVisibleView = absListView.getChildAt(lastVisiblePos - firstVisiblePos);
	            if (lastVisibleView instanceof FooterView)
	            {
	                boolean hasMoreData = ImageDataProvider.getInstance().hasMoreData(mCurrentCategory);
	                if (!mIsLoadingMoreData && hasMoreData)
	                {
	                	LogUtil.i(TAG, "begin to load more data");
	                	
	                    mIsLoadingMoreData = true;
	                    ImageDataProvider.getInstance().loadImageGroupsAsync(
	                    		mCurrentCategory, DEFAULT_REQUEST_COUNT, true, ImageGridFragment.this);
	                }
	            }
	        }
	    }
	
	    @Override
	    public void onScroll(AbsListView absListView, int firstVisibleItem,
	            int visibleItemCount, int totalItemCount) {
	    }
	};

	private void onClickGirlGroup(View v, long id, ImageGroup girl)
	{
		final int groupIx = (int)id;
		
		//if (girl.hasInited())
		{
		    // Start the ImageDetailActivity
		    final Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
		    //intent.putExtra(ImageDetailActivity2.EXTRA_GIRL_ID, girlId);
		    intent.putExtra(ImageDetailActivity.EXTRA_GROUP_IX, groupIx);
		    intent.putExtra(ImageDetailActivity.EXTRA_CATEGORY, mCurrentCategory);
		    //LogUtil.d(TAG, "Send Girl id = " + girlId);
			
		    if (Utils.hasJellyBean()) {
		        // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
		        // show plus the thumbnail image in GridView is cropped. so using
		        // makeScaleUpAnimation() instead.
		        ActivityOptions options =
		                ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
		        /*Drawable drawable = ((ImageView)v).getBackground();
		        ActivityOptions options =
		        		ActivityOptions.makeThumbnailScaleUpAnimation(v, ((BitmapDrawable)drawable).getBitmap(), 0, 0);*/
		        //getActivity().startActivity(intent, options.toBundle());
		        getActivity().startActivityForResult(
		        		intent, REQUEST_CODE_REFRESH_FAVORITE, options.toBundle());
		        
		    } else {
		        //startActivity(intent);
		        startActivityForResult(intent, REQUEST_CODE_REFRESH_FAVORITE);
		    }
		}
	}

	private void onClickFavImage(View v, long id)
	{
		// Start the ImageDetailActivity
		int imageIndex = (int)id;
		final Intent intent = new Intent(getActivity(), FavoriteImageActivity.class);
		intent.putExtra(FavoriteImageActivity.EXTRA_GROUP_IX, imageIndex);
		intent.putExtra(FavoriteImageActivity.EXTRA_CATEGORY, mCurrentCategory);
		LogUtil.d(TAG, "Send Image id = " + imageIndex);
		
		if (Utils.hasJellyBean()) {
		    // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
		    // show plus the thumbnail image in GridView is cropped. so using
		    // makeScaleUpAnimation() instead.
		    ActivityOptions options =
		            ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
		    //getActivity().startActivity(intent, options.toBundle());
		    getActivity().startActivityForResult(intent, REQUEST_CODE_REFRESH_FAVORITE, options.toBundle());
		} else {
		    //startActivity(intent);
			startActivityForResult(intent, REQUEST_CODE_REFRESH_FAVORITE);
		}
	}
	
	private void onSlideShow()
	{
		if (mImageFetcher.getAdapter().getSize() > 0) {
			Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
			if (ImageGroupCategory.CATEGORY_FAVOTITE == mCurrentCategory) {
				intent = new Intent(getActivity(), FavoriteImageActivity.class);
				intent.putExtra(FavoriteImageActivity.EXTRA_CATEGORY, mCurrentCategory);
				intent.putExtra(FavoriteImageActivity.EXTRA_GROUP_IX, 0);
				intent.putExtra(FavoriteImageActivity.EXTRA_ENTER_MODE, true);
			} else {
				intent.putExtra(ImageDetailActivity.EXTRA_GROUP_IX, 0);
				intent.putExtra(ImageDetailActivity.EXTRA_CATEGORY, mCurrentCategory);
				intent.putExtra(ImageDetailActivity.EXTRA_ENTER_MODE, true);
			}
			//startActivity(intent);
			startActivityForResult(intent, REQUEST_CODE_REFRESH_FAVORITE);
		}
	}

	private void scrollToBottom(int duration)
    {
        final int bottom = mGridView.getBottom();
        final int firstVisPos = mGridView.getFirstVisiblePosition();
        final int lastVisPos = mGridView.getLastVisiblePosition();
        final int itemBottom = mGridView.getChildAt(lastVisPos - firstVisPos).getBottom();
        final int totalSize = mImageGridAdapter.getCount()/* - mAdapter.getNumColumns()*/;
        if (totalSize == lastVisPos + 1 && itemBottom == bottom)
        {
            Toast.makeText(getActivity(), R.string.toast_bottom_of_page, Toast.LENGTH_SHORT).show();
            return;
        }
        
        final int colCount = mImageGridAdapter.getNumColumns();
        final int remainSize = totalSize - lastVisPos;       
        final int remainRows = (0 == remainSize % colCount) ? (remainSize / colCount) : (remainSize / colCount + 1);
        final int scrollH = remainRows * (mImageThumbSize/*Width=Height*/ + mImageThumbSpacing) + (itemBottom - bottom);
        mGridView.smoothScrollBy(scrollH, duration * remainRows);
    }
    
    private void scrollToTop(int duration)
    {
        // Note: If the first visible position is 0, that means we have scroll to top.
        // Because we have the action bar. It's not like the normal grid view.
        final int startItemPos = mImageGridAdapter.getNumColumns();
        final int firstVisPos = mGridView.getFirstVisiblePosition();
        final int top = mGridView.getTop();
        final int itemTop = mGridView.getChildAt(0).getTop();
        if (/*startItemPos == firstVisPos && itemTop == top*/0 == firstVisPos)
        {
            Toast.makeText(getActivity(), R.string.toast_top_of_page, Toast.LENGTH_SHORT).show();
            return;
        }
        
        final int colCount = mImageGridAdapter.getNumColumns();
        final int remainSize = firstVisPos - startItemPos;
        final int remainRows = remainSize / colCount;
        final int scrollH = remainRows * (mImageThumbSize/*Width=Height*/ + mImageThumbSpacing) + (top - itemTop) + mImageGridAdapter.getActionBarHeight();
        mGridView.smoothScrollBy(-scrollH, duration * remainRows);
    }
    
    @SuppressWarnings("unused")
	private void scrollGirlsToOldPos()
    {
        // TODO:
        /*final int firstVisPos = m_gridView.getFirstVisiblePosition();
        final int firstVisTop = m_gridView.getChildAt(0).getTop();
        
        m_oldFavsPos = firstVisPos;
        m_oldFavsY = firstVisTop;
        
        m_gridView.smoothScrollToPositionFromTop(m_oldImagesPos, m_oldImagesY, 400);
        LogUtil.i(TAG, "Scroll to Girls (1): " +
        		"m_oldFavsPos = " + m_oldFavsPos + 
                ", m_oldFavsY = " + m_oldFavsY + 
                ", m_oldImagesPos = " + m_oldImagesPos + 
                ", m_oldImagesY = " + m_oldImagesY);*/
        
        /*if (0 == firstVisPos)
        {
            return;
        }
        
        final int startItemPos = mAdapter.getNumColumns();
        final int colCount = mAdapter.getNumColumns();
        final int remainSize = m_oldImagesPos - startItemPos;
        final int remainRows = remainSize / colCount;
        final int scrollH = remainRows * (mImageThumbSize Width=Height + mImageThumbSpacing)
                - m_oldImagesY + mAdapter.getActionBarHeight();        
        
        m_gridView.scrollTo(0, scrollH);*/
    }
    
    @SuppressWarnings("unused")
	private void scrollFavsToOldPos()
    {
        // TODO:
        /*final int firstVisPos = m_gridView.getFirstVisiblePosition();
        final int firstVisTop = m_gridView.getChildAt(0).getTop();
        
        m_oldImagesPos = firstVisPos;
        m_oldImagesY = firstVisTop;
        m_gridView.smoothScrollToPositionFromTop(m_oldFavsPos, m_oldFavsY, 400);
        LogUtil.i(TAG, "Scroll to Favourite (2): " +
        		"m_oldFavsPos = " + m_oldFavsPos + 
                ", m_oldFavsY = " + m_oldFavsY + 
                ", m_oldImagesPos = " + m_oldImagesPos + 
                ", m_oldImagesY = " + m_oldImagesY);*/
        
        /*if (0 == firstVisPos)
        {
            return;
        }
        
        final int startItemPos = mAdapter.getNumColumns();
        final int colCount = mAdapter.getNumColumns();
        final int remainSize = m_oldFavsPos - startItemPos;
        final int remainRows = remainSize / colCount;
        final int scrollH = remainRows * (mImageThumbSize Width=Height + mImageThumbSpacing)
                - m_oldFavsY + mAdapter.getActionBarHeight();        
        
        m_gridView.scrollTo(0, scrollH);*/
    }
    
    private int obtainActionBarHeight()
    {
    	TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(
                android.R.attr.actionBarSize, tv, true)) {
            int height = TypedValue.complexToDimensionPixelSize(
                    tv.data, getActivity().getResources().getDisplayMetrics());
            
            LogUtil.d("DisplayMetrics", "Get actionBar height = " + height);
            return height;
        }
        
        return getActivity().getActionBar().getHeight();
    }
    
    /**
     * The main adapter that backs the GridView. This is fairly standard except the number of
     * columns in the GridView is used to create a fake top row of empty views as we use a
     * transparent ActionBar and don't want the real top row of images to start off covered by it.
     */
    private class ImageGridAdapter extends BaseAdapter {

        private static final int ITEM_TYPE_TOP_HEADER_VIEW 	= 0;
        private static final int ITEM_TYPE_IMAGEVIEW 		= 1;
        private static final int ITEM_TYPE_END_EMPTY_VIEW 	= 2;
        private static final int ITEM_TYPE_END_FOOTER_VIEW 	= 3;
        private static final int ITEM_TYPE_COUNT 			= 4;
    	
        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private int mActionBarHeight = 0;
        private GridView.LayoutParams mImageViewLayoutParams;

        public ImageGridAdapter(Context context) {
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            
            mActionBarHeight = obtainActionBarHeight();
        }

        @Override
        public int getCount() {
            // Size of adapter + number of columns for top empty row
            //return mImageFetcher.getAdapter().getSize() + mNumColumns;
            return getTotalCount();
        }

        @Override
        public Object getItem(int position) {
            if (position < mNumColumns || position >= getLastEmptyStartPos()) {
                return null;
            } else {
                return mImageFetcher.getAdapter().getItem(position - mNumColumns);
            }
            /*return position < mNumColumns ?
                    null : mImageFetcher.getAdapter().getItem(position - mNumColumns);*/
        }

        @Override
        public long getItemId(int position) {
            //return position < mNumColumns ? 0 : position - mNumColumns;
            
            return (position < mNumColumns || position >= getLastEmptyStartPos()) ? 
                    0 : position - mNumColumns;
        }

        @Override
        public int getViewTypeCount() {
            // Two types of views, the normal ImageView and the top row of empty views
            return ITEM_TYPE_COUNT;
        }        
        
        @Override
        public int getItemViewType(int position) {
            //return (position < mNumColumns) ? 1 : 0;
            
            if (position < mNumColumns) {
                return ITEM_TYPE_TOP_HEADER_VIEW;   // top empty view type
            } else if (position < getLastEmptyStartPos()) {
                return ITEM_TYPE_IMAGEVIEW;   		// ImageView
            } else if (position < getLastRowStartPos()) {
                return ITEM_TYPE_END_EMPTY_VIEW;   	// empty View
            } else {
                return ITEM_TYPE_END_FOOTER_VIEW;   // ProgressBar
            }
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            LogUtil.i(TAG, "pos = " + position + ", mNumColumns = " + mNumColumns);
            
            int type = getItemViewType(position);
            
            // 1. check if this is the top row
            if (ITEM_TYPE_TOP_HEADER_VIEW == type) {
                
                //LogUtil.d(TAG, "getLastEmptyStartPos = " + getLastEmptyStartPos() + ", getLastRowStartPos = " + getLastRowStartPos());
                LogUtil.i(TAG, "STEP 1: Show header view");
                if (convertView == null) {
                    convertView = new View(mContext);
                }
                // Set empty view with height of ActionBar
                convertView.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
                
                return convertView;
            }
            
            // 2. check if the last empty row (May show only one image)
            if (ITEM_TYPE_END_EMPTY_VIEW == type)
            {
                LogUtil.i(TAG, "STEP 2: Show empty view");
                
                // Or we can use the 
                if (convertView == null) {
                    convertView = new View(mContext);
                }
                // Set empty view with height of ActionBar
                convertView.setLayoutParams(new AbsListView.LayoutParams(
                        mItemHeight, mItemHeight));
                
                return convertView;
            }
            
            // 3. check if the last row
            if (ITEM_TYPE_END_FOOTER_VIEW == type)
            {
                LogUtil.i(TAG, "STEP 3: Show ProgressBar");
                LogUtil.d(TAG, "getLastRowStartPos, pos = " + position);
                /*mHasMoreData*/
                
                /*if (convertView == null) {
                	View view = new View(mContext);
                	
                    ProgressBar progressBar = new ProgressBar(mContext);
                    convertView = progressBar;
                }
                // Set empty view with height of ActionBar
                if (position == getLastRowStartPos()) {
                    convertView.setVisibility(View.VISIBLE);
                    convertView.setLayoutParams(new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT
                            mGridView.getWidth(), // Set full width of GridView
                            mActionBarHeight));
                } else {
                    convertView.setVisibility(View.INVISIBLE);
                    convertView.setLayoutParams(new AbsListView.LayoutParams(
                            10,                     // Set a small width for a progress bar
                            mActionBarHeight));
                }*/
                
                FooterView footerView = null;
                if (null == convertView) {
                	footerView = new FooterView(mContext);
				} else {
					footerView = (FooterView) convertView;
				}
                
                if (position == getLastRowStartPos()) {
                	boolean hasMoreData = ImageDataProvider.getInstance().hasMoreData(mCurrentCategory);
					footerView.setMode(hasMoreData ? FooterView.PROGRESS_MODE : FooterView.TEXT_MODE);
				} else {
					footerView.setMode(FooterView.CLEAN_MODE);
				}
                
                footerView.setLayoutParams(new AbsListView.LayoutParams(
                            mGridView.getWidth(),
                            mActionBarHeight));
                
                convertView = footerView;
                
                return convertView;
            }

            // NOTE: To avoid a bug: (Current I don't know why)
            // java.lang.ClassCastException: android.widget.ProgressBar cannot be cast to 
            // com.gtx.cooliris.widget.ImageGroupView
            // TODO:
            
            if (convertView != null && !(convertView instanceof ImageGroupView))
            {
                convertView = null;
            }
            
            // 4. create ImageView
            // Now handle the main ImageView thumbnails
            ImageGroupView imageView;
            if (convertView == null) { // if it's not recycled, instantiate and initialize
                imageView = new ImageGroupView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(mImageViewLayoutParams);
            } else { // Otherwise re-use the converted view
                imageView = (ImageGroupView) convertView;
            }

            // Check the height matches our calculated column width
            if (imageView.getLayoutParams().height != mItemHeight) {
                imageView.setLayoutParams(mImageViewLayoutParams);
            }

            // Set random background color
            int w = (int)(Math.random() * 40 + 10);
            imageView.setBackgroundColor(Color.argb(255, w, w, w));
            
            //Set the image view's data source
            imageView.setDataSource(mImageFetcher.getAdapter().getOriginalItem(position - mNumColumns));

            // Finally load the image asynchronously into the ImageView, this also takes care of
            // setting a placeholder image while the background thread runs
            mImageFetcher.loadImage(position - mNumColumns, imageView);

            return imageView;
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams =
                    new GridView.LayoutParams(/*LayoutParams.MATCH_PARENT*/mItemHeight, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
            
            LogUtil.d(TAG, "notifyDataSetChanged");
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

		public int getNumColumns() {
			return mNumColumns;
		}

		public void resetActionBarHeight() {
			mActionBarHeight = obtainActionBarHeight();
		}
		
		public int getActionBarHeight() {
			return mActionBarHeight;
		}

		public int getTotalCount() {
			return getRowCount() * mNumColumns;
		}
        
        public int getRowCount() {
            int size = mImageFetcher.getAdapter().getSize();
            if (mNumColumns != 0 && size != 0) {
                return (0 == size % mNumColumns) ? (size / mNumColumns + 2) : (size / mNumColumns + 3);
            } else {
                return 0;
            }
        }
        
		public int getLastRowStartPos() {
			return (getRowCount() - 1) * mNumColumns;
		}

		public int getLastEmptyStartPos() {
			return mNumColumns + mImageFetcher.getAdapter().getSize();
		}
    }
    
	public static class ProgressDlgFrag extends DialogFragment {
        private static String TITLE_KEY = "title";
        private boolean m_isCanceled = false;
        
        static ProgressDlgFrag newInstance(String title) {
            ProgressDlgFrag dlgFrag = new ProgressDlgFrag();
            Bundle args = new Bundle();
            args.putString(TITLE_KEY, title);
            dlgFrag.setArguments(args);
            
            return dlgFrag;
        }
        
        public boolean isCanceled()
        {
            return m_isCanceled;
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            // Initialize dialog
            String title = getArguments().getString(TITLE_KEY);
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(title);
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            //dialog.setFeatureDrawable(featureId, drawable)
            dialog.setProgressStyle(android.R.attr.progressBarStyleInverse);
            
            return dialog;
        }
        
        @Override
        public void onCancel(DialogInterface dialog)
        {
            LogUtil.d(TAG, "onCancel..........");
            m_isCanceled = true;
            
            // Do nothing
            super.onCancel(dialog);
        }
    }
    
    private void showDialog()
    {
        // TODO: Show a dialog that embedded in UI
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        
        // Create a new fragment and show
        DialogFragment dlgFrag = ProgressDlgFrag.newInstance(
                getActivity().getResources().getString(R.string.toast_loading_data));
        dlgFrag.show(getFragmentManager(), "dialog"); 
    }
    
    private void dismissDialog()
    {
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (null != prev && prev instanceof ProgressDlgFrag)
        {
            DialogFragment progressDlgFrag = (ProgressDlgFrag) prev;
            progressDlgFrag.dismiss();
        }
    }
    
    private class FooterView extends FrameLayout {
    	
    	public static final int CLEAN_MODE 		= 0;
    	public static final int PROGRESS_MODE 	= 1;
    	public static final int TEXT_MODE 		= 2;
    	
    	private Context 	mContext 	 = null;
    	private ProgressBar mProgressBar = null;
    	private TextView 	mTextView 	 = null;
    	private int			mCurrentMode = CLEAN_MODE;
    	
    	
		public FooterView(Context context) {
			this(context, null);
		}
    	
		public FooterView(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}
    	
		public FooterView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			
			initialize(context);
		}
		
		void initialize(Context context) {
			mContext = context;
			
	    	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	inflater.inflate(R.layout.image_grid_footer, this);
			
	    	mProgressBar = (ProgressBar) findViewById(R.id.footer_loading_more_progressbar);
	    	mTextView = (TextView) findViewById(R.id.footer_nomore_data_textview);
		}
		
		public void setMode(int mode) {
			mCurrentMode = mode;
			
			switch (mCurrentMode) {
			case PROGRESS_MODE:
				mProgressBar.setVisibility(View.VISIBLE);
				mTextView.setVisibility(View.INVISIBLE);
				break;
				
			case TEXT_MODE:
				mProgressBar.setVisibility(View.INVISIBLE);
				mTextView.setVisibility(View.VISIBLE);			
				break;
				
			case CLEAN_MODE:
			default:
				mProgressBar.setVisibility(View.INVISIBLE);
				mTextView.setVisibility(View.INVISIBLE);	
				break;
			}
		}
    }
}
