package common;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * שולח מייל דרך Gmail SMTP.
 */
public class EmailSender {

    private final Session session;

    public EmailSender() {
        System.out.println("[EmailSender] <init> called (common)");  // שורת Debug

        String host = "smtp.gmail.com";
        String port = "587";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    SendMailConfig.USERNAME,
                    SendMailConfig.APP_PASSWORD
                );
            }
        });

        session.setDebug(true);  // מפעיל Debug של JavaMail
    }

    /**
     * שולח מייל שמכיל את הסיסמה.
     *
     * @param toEmail  הכתובת לקבלת המייל
     * @param password הסיסמה שתישלח בגוף ההודעה
     * @throws MessagingException אם משהו נכשל בשליחה
     * @throws UnsupportedEncodingException 
     */
    public void sendPasswordEmail(String toEmail, String password) throws MessagingException, UnsupportedEncodingException {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SendMailConfig.USERNAME, SendMailConfig.SENDER_NAME));
            msg.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail, false)
            );
            msg.setSubject("BPARK Password Reset");
            msg.setText(
                "Hello,\n\n" +
                "Your password is: " + password + "\n\n" +
                "Best regards,\n" +
                SendMailConfig.SENDER_NAME
            );

            System.out.println("[EmailSender] → Transport.send()");  // Debug לפני השליחה
            Transport.send(msg);
            System.out.println("[EmailSender] Email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[EmailSender] Failed to send email to " + toEmail);
            e.printStackTrace();
            throw e;
        }
    }
}
