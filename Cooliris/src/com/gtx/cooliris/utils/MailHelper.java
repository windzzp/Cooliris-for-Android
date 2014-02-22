package com.gtx.cooliris.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.annotation.SuppressLint;
import android.text.TextUtils;

public class MailHelper extends Authenticator
{
    public static final String HOST_GMAIL  = "host_gmail";
    public static final String HOST_163    = "host_163";
    public static final String HOST_126    = "host_126";
    
    public static class HostInfo
    {
        private String mType;
        private String mHost;
        private String mPort;
        private String mSport;
        
        public HostInfo(String type, String host, String port, String sport)
        {
            mType  = type;
            mHost  = host;
            mPort  = port;
            mSport = sport;
        }
        
        @Override
        public String toString()
        {
            return "  mType = " + mType + 
                   ", mHost = " + mHost + 
                   ", mPort = " + mPort +
                   ", mSport = " + mSport;
        }
    }
    
    @SuppressLint("UseSparseArrays")
    private static HashMap<String, HostInfo> s_defaultHostInfos = new HashMap<String, HostInfo>();
    
    static
    {
        s_defaultHostInfos.put(HOST_GMAIL, new HostInfo(HOST_GMAIL, "smtp.gmail.com", "465", "465"));
        s_defaultHostInfos.put(HOST_163,   new HostInfo(HOST_163, "smtp.163.com",     "465", "465"));
        s_defaultHostInfos.put(HOST_126,   new HostInfo(HOST_126, "smtp.126.com",     "25",  "25"));
    }
    
    private String      mAuthUser   = "";
    private String      mAuthPWD    = "";

    private HostInfo    mHostInfo   = null;

    private String      mMailFrom   = "";
    private String[]    mMailTo     = null;

    private String      mSubject    = "";
    private String      mBody       = "";
    private Multipart   mMultipart  = new MimeMultipart();

    private boolean     mNeedAuth   = true;
    private boolean     mNeedDebug  = false;

    public MailHelper()
    {       
        /*mHost = "smtp.163.com";
        // SMTP Port
        mPort = "465";
        // Socket factory Port
        mSport = "465"; */         

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public MailHelper(String user, String pass, String hostType)
    {
        this();
        mAuthUser = user;
        mAuthPWD = pass;
        
        mHostInfo = s_defaultHostInfos.get(hostType);
        if (null == mHostInfo)
        {
            mHostInfo = s_defaultHostInfos.get(HOST_GMAIL);
        }
    }

    public boolean send() throws Exception
    {
        Properties props = setProperties();

        if (!TextUtils.isEmpty(mAuthUser) && 
            !TextUtils.isEmpty(mAuthPWD) && 
            !TextUtils.isEmpty(mMailFrom) && 
            null != mMailTo && 
            mMailTo.length > 0 && 
            !TextUtils.isEmpty(mSubject) && 
            !TextUtils.isEmpty(mBody))        
        {
            // Get session
            Session session = Session.getInstance(props, this);
            MimeMessage msg = new MimeMessage(session);
            // Set mail from
            msg.setFrom(new InternetAddress(mMailFrom));
            
            // Set mail to
            InternetAddress[] addressTo = new InternetAddress[mMailTo.length];
            for (int i = 0; i < mMailTo.length; i++)
            {
                addressTo[i] = new InternetAddress(mMailTo[i]);
            }
            msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);
            
            // Set mail subject and date
            msg.setSubject(mSubject);
            msg.setSentDate(new Date());
            
            // Set mail body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(mBody);
            mMultipart.addBodyPart(messageBodyPart);
            msg.setContent(mMultipart);
            
            // Send mail
            Transport.send(msg);
            
            return true;
        }
        else
        {
            return false;
        }
    }

    public void addAttachment(String filename) throws Exception
    {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        mMultipart.addBodyPart(messageBodyPart);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(mAuthUser, mAuthPWD);
    }

    private Properties setProperties()
    {
        Properties props = new Properties();

        props.put("mail.smtp.host", mHostInfo.mHost);

        if (mNeedDebug)
        {
            props.put("mail.debug", "true");
        }

        if (mNeedAuth)
        {
            props.put("mail.smtp.auth", "true");
        }

        // For smtp.126.com, if we use the following code, we'll receive:
        // javax.mail.MessagingException: Could not connect to SMTP host: smtp.126.com, port: 25;
        // That's because port 25 is the non-SSL (plain text) port. Assuming your server 
        // is set up for SSL, use the connect method that doesn't require you to specify a port 
        // number and let JavaMail use the default port.
        // For more detail, please see http://stackoverflow.com/questions/11963587/sslexception-in-using-javamail-api
        if (!"25".equals(mHostInfo.mPort))
        {
            props.put("mail.smtp.port", mHostInfo.mPort);
            props.put("mail.smtp.socketFactory.port", mHostInfo.mSport);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");   
        }

        return props;
    }

    public String getUser()
    {
        return mAuthUser;
    }

    public void setUser(String authUser)
    {
        this.mAuthUser = authUser;
    }

    public String getPassword()
    {
        return mAuthPWD;
    }

    public void setPassword(String password)
    {
        this.mAuthPWD = password;
    }


    public HostInfo getHostInfo()
    {
        return mHostInfo;
    }
    
    public void setHostInfo(HostInfo hostInfo)
    {
        mHostInfo = hostInfo;
    }
    
    public String getMailFrom()
    {
        return mMailFrom;
    }

    public void setMailFrom(String mailFrom)
    {
        this.mMailFrom = mailFrom;
    }

    public String[] getMailTo()
    {
        return mMailTo;
    }

    public void setMailTo(String[] mailTo)
    {
        this.mMailTo = mailTo;
    }

    public String getSubject()
    {
        return mSubject;
    }

    public void setSubject(String subject)
    {
        this.mSubject = subject;
    }

    public String getBody()
    {
        return mBody;
    }

    public void setBody(String body)
    {
        this.mBody = body;
    }

    public Multipart getMultipart()
    {
        return mMultipart;
    }

    public void setMultipart(Multipart multipart)
    {
        this.mMultipart = multipart;
    }

    public boolean needAuth()
    {
        return mNeedAuth;
    }

    public void setNeedAuth(boolean needAuth)
    {
        this.mNeedAuth = needAuth;
    }

    public boolean needDebug()
    {
        return mNeedDebug;
    }

    public void setNeedDebug(boolean needDebug)
    {
        this.mNeedDebug = needDebug;
    }
}