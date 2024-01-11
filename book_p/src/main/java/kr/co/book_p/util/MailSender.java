package kr.co.book_p.util;

import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import java.util.Properties;

@Component
public class MailSender {

    @Value("${email.pw}")
    private String mailpasswd;

    @Value("${email.id}")
    private String FROM;
    private String FROMNAME = "TEST";

    @Value("${email_id}")
    private String SMTP_USERNAME;
    private String HOST = "smtp.gmail.com";
    private int PORT = 587;

    public void sender(String To, String Subject, Object Body) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getDefaultInstance(props);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(To));
        msg.setSubject(Subject);

        if (Body instanceof MimeMultipart) {
            msg.setContent((Multipart) Body);
        } else {
            msg.setContent((String) Body, "text/html;charset=utf-8");
        }

        Transport transport = session.getTransport();
        try {
            transport.connect(HOST, SMTP_USERNAME, mailpasswd);
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            transport.close();
        }
    }

}
