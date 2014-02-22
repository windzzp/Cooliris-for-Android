/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gtx.cooliris.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gtx.cooliris.R;

/**
 * This fragment will populate the children of the ViewPager from {@link FavoriteImageActivity}.
 */
public class SettingFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "resId";


    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageNum The image number within the parent adapter to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static SettingFragment newInstance(int imageNum) {
        final SettingFragment f = new SettingFragment();

        final Bundle args = new Bundle();
        args.putInt(IMAGE_DATA_EXTRA, imageNum);
        f.setArguments(args);

        return f;
    }
    
    /**
     * Empty constructor as per the Fragment documentation
     */
    public SettingFragment() {}

    /**
     * Populate image number from extra, use the convenience factory method
     * {@link SettingFragment#newInstance(int)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        //mImageView = (ImageViewEx) v.findViewById(R.id.imageView);
        return v;
    }
}
