package com.gtx.cooliris.ui;

import android.view.Menu;
import android.view.MenuInflater;

import com.gtx.cooliris.R;

public class FavoriteImageActivity extends BaseImageDetailActivity
{
    @SuppressWarnings("unused")
    private static final String TAG_FAVORITE = "FavoriteImageActivity";
    
    @Override
    protected void onInitView()
    {
        super.onInitView();
        
        final int groupIx = getIntent().getIntExtra(EXTRA_GROUP_IX, 0);
        final boolean bSlideShowMode = getIntent().getBooleanExtra(EXTRA_ENTER_MODE, false);
        //m_currentImage = m_allImages.get(girlIx);
        
        // Set the current item based on the extra passed in to this activity
        final int extraCurrentItem = groupIx;
        if (extraCurrentItem != -1) {
            m_pager.setCurrentItem(extraCurrentItem);
        }
        
        // If start with slide show mode
        if (bSlideShowMode)
        {
            // To let the activity have time to initialize the UI.
            m_handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    entrySlideShowMode();
                }
            }, 1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_girl_favorite_images, menu);
        
        // Initialize action provider
        initActionProvider(menu);
        
        return true;
    }
    
    @Override
    protected int getNextSlideShowIndex()
    {
        // Reset girl info
        final int totalSize = mImageFetcher.getAdapter().getSize();
        int nextImageIx = m_pager.getCurrentItem() + 1;
        nextImageIx = (nextImageIx < totalSize) ? nextImageIx : 0;

        return nextImageIx;
    }
}
