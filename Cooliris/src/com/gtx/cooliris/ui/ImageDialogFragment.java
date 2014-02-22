package com.gtx.cooliris.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ImageDialogFragment extends DialogFragment
{
    private int mNum;
    
    static ImageDialogFragment newInstance(int num)
    {
        ImageDialogFragment dlgFragment = new ImageDialogFragment();
        
        Bundle args = new Bundle();
        args.putInt("num", num);
        dlgFragment.setArguments(args);
        
        return dlgFragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mNum = getArguments().getInt("num");
        
        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        switch ((mNum - 1 ) % 6) {
            case 1: style = DialogFragment.STYLE_NO_TITLE; break;
            case 2: style = DialogFragment.STYLE_NO_FRAME; break;
            case 3: style = DialogFragment.STYLE_NO_INPUT; break;
            case 4: style = DialogFragment.STYLE_NORMAL; break;
            case 5: style = DialogFragment.STYLE_NORMAL; break;
            case 6: style = DialogFragment.STYLE_NO_TITLE; break;
            case 7: style = DialogFragment.STYLE_NO_FRAME; break;
            case 8: style = DialogFragment.STYLE_NORMAL; break;
        }
        switch ((mNum-1)%6) {
            case 4: theme = android.R.style.Theme_Holo; break;
            case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
            case 6: theme = android.R.style.Theme_Holo_Light; break;
            case 7: theme = android.R.style.Theme_Holo_Light_Panel; break;
            case 8: theme = android.R.style.Theme_Holo_Light; break;
        }
        setStyle(style, theme);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
//        View v = inflater.inflate(com.gtx.cooliris.R.layout.fragement_dialog, container, false);
//        TextView tv = (TextView) v.findViewById(com.gtx.cooliris.R.id.text);
//        tv.setText("Dialog #" + mNum + ": using style ??");
//        
//        // Watch for button clicks.
//        Button button = (Button)v.findViewById(com.gtx.cooliris.R.id.show);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // When button is clicked, call up to owning activity.
//            }
//        });
//        
//        return v;
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dlg = null;
        switch (mNum)
        {
        case 1:
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Loading");
            
            dlg = progressDialog;
            break;

        default:
            break;
        }
        return dlg;
        //return super.onCreateDialog(savedInstanceState);
    }
}
