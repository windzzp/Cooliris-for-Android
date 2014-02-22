package com.gtx.cooliris.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.gtx.cooliris.R;
import com.gtx.cooliris.constant.GAConstant;
import com.gtx.cooliris.imagecache.Utils;
import com.gtx.cooliris.utils.MailHelper;

public class FeedbackActivity extends Activity
{
    private static final int DIALOG_SUBMITTING = 1;
    
    private static final int MSG_SUBMITTING_SUCCEED = 1;
    private static final int MSG_SUBMITTING_FAILED = 2; 
    
    private RadioGroup  mRadioGroup      = null;
    private EditText    mFeedbackContent = null;
    private EditText    mFeedbackContact = null;
    private Button      mSubmitBtn       = null;
    
    private static final String MAIL_FROM       = "d295YW93ZW56aUAxMjYuY29t";
    private static final String MAIL_FROM_PWD   = "MzM2MDIyNw==";
    
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case MSG_SUBMITTING_SUCCEED:
                dismissDialog(DIALOG_SUBMITTING);
                Toast.makeText(FeedbackActivity.this, R.string.toast_submit_succeed, Toast.LENGTH_LONG).show();
                break;
                
            case MSG_SUBMITTING_FAILED:
                dismissDialog(DIALOG_SUBMITTING);
                Toast.makeText(FeedbackActivity.this, R.string.toast_submit_failed, Toast.LENGTH_LONG).show();
                break;

            default:
                break;
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_activity);
        
        EasyTracker.getTracker().sendView(GAConstant.SCREEN_SETTINGS_FEEDBACK);
        
        // Add a action bar on top
        if (Utils.hasHoneycomb()) {
            final ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.preference_feedback);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        
        mRadioGroup = (RadioGroup) findViewById(R.id.feedback_container);
        mFeedbackContent = (EditText) findViewById(R.id.feedback_content);
        mFeedbackContact = (EditText) findViewById(R.id.feedback_contact);
        mSubmitBtn = (Button) findViewById(R.id.feedback_submit);
        
        mFeedbackContent.setFocusable(true);
        
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                case R.id.feedback_bug_report:
                    mFeedbackContent.setHint(R.string.feedback_bug_report_default);
                    break;
                    
                case R.id.feedback_improvement:
                    mFeedbackContent.setHint(R.string.feedback_improvement_default);
                    break;
                    
                case R.id.feedback_others:
                    mFeedbackContent.setHint(R.string.feedback_others_default);
                    break;

                default:
                    break;
                }
            }
        });
        
        mSubmitBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {               
                final String content = mFeedbackContent.getText().toString();
                
                if (TextUtils.isEmpty(content))
                {
                    Toast.makeText(FeedbackActivity.this, R.string.toast_submit_no_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Prepare mail data
                RadioButton radioBtn = (RadioButton) findViewById(mRadioGroup.getCheckedRadioButtonId());
                final String feedbackType = radioBtn.getText().toString();
                final String contact = mFeedbackContact.getText().toString();
                
                // Set mail subject: appname_feedbackType
                final String subject = getString(R.string.app_name) + "_" + feedbackType;
                // Should use HTML format
                //final String mailBody = feedbackType + "\n" + content + "\n" + contact;
                                
                // Show submitting dialog
                showDialog(DIALOG_SUBMITTING);
                
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // Get date
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateTime = sdf.format(date);
                        
                        // Fill body
                        String body = "Submitted on: " + dateTime + "<br>" + 
                                      "Contact Info: "+ contact + "<br>" + 
                                      "Content: " + content;

                        // Get body string
                        final String mailBody = Html.fromHtml(body).toString();
                        
                        // Get the mail from/to user name and password
                        final String fromMail = new String(Base64.decode(MAIL_FROM, Base64.DEFAULT));
                        final String fromMailPwd = new String(Base64.decode(MAIL_FROM_PWD, Base64.DEFAULT));
                        //final String toMail = new String(Base64.decode(MAIL_TO, Base64.DEFAULT));
                        
                        MailHelper m = new MailHelper(fromMail, fromMailPwd, MailHelper.HOST_126);
                        String[] toArr = {/*toMail*/fromMail};
                        m.setMailTo(toArr);
                        m.setMailFrom(fromMail);
                        m.setSubject(subject);
                        m.setBody(mailBody);

                        try
                        {
                            if (m.send())
                            {
                                mHandler.sendEmptyMessage(MSG_SUBMITTING_SUCCEED);
                            }
                            else
                            {
                                mHandler.sendEmptyMessage(MSG_SUBMITTING_FAILED);
                            }
                        }
                        catch (Exception e)
                        {
                            mHandler.sendEmptyMessage(MSG_SUBMITTING_FAILED);
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
        case DIALOG_SUBMITTING:
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.msg_feedback_submitting));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setProgressStyle(android.R.attr.progressBarStyleInverse);
            return dialog;

        default:
            break;
        }
        return super.onCreateDialog(id);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            //NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }   
    
    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.exit_anim);
    }    
}
