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
 * Sends various email notifications via Gmail SMTP using Jakarta Mail API.
 * This class is responsible for configuring the mail session and sending
 * predefined types of messages such as password reset, parking code retrieval,
 * and towing notifications.
 */
public class EmailSender {

    private final Session session;

    /**
     * Constructs an {@code EmailSender} and initializes the mail session
     * using Gmail SMTP with TLS authentication.
     */
    public EmailSender() {
        System.out.println("[EmailSender] <init> called (common)");  

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

        session.setDebug(true);  // JavaMail Debug  
    }

    /**
     * Sends a password reset email to a subscriber.
     *
     * @param toEmail  The recipient's email address.
     * @param password The password to include in the email body.
     * @throws MessagingException             If the message cannot be sent.
     * @throws UnsupportedEncodingException   If encoding the sender name fails.
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

            System.out.println("[EmailSender] → Transport.send()");   
            Transport.send(msg);
            System.out.println("[EmailSender] Email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[EmailSender] Failed to send email to " + toEmail);
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Sends a parking code retrieval email to a subscriber.
     *
     * @param toEmail     The recipient's email address.
     * @param parkingCode The parking code to include in the email body.
     * @throws MessagingException             If the message cannot be sent.
     * @throws UnsupportedEncodingException   If encoding the sender name fails.
     */
    public void sendParkingCodeEmail(String toEmail, String parkingCode) throws MessagingException, UnsupportedEncodingException {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SendMailConfig.USERNAME, SendMailConfig.SENDER_NAME));
            msg.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail, false)
            );
            msg.setSubject("BPARK Parking Code Retrieval");
            msg.setText(
                "Hello,\n\n" +
                "Your parking code is: " + parkingCode + "\n\n" +
                "Best regards,\n" +
                SendMailConfig.SENDER_NAME
            );

            System.out.println("[EmailSender] → Transport.send() (ParkingCode)");
            Transport.send(msg);
            System.out.println("[EmailSender] Parking code email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[EmailSender] Failed to send parking code email to " + toEmail);
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Sends a towing notification email to a subscriber whose vehicle was towed.
     *
     * @param toEmail       The recipient's email address.
     * @param vehicleNumber The license plate number of the towed vehicle.
     * @param spotNumber    The parking spot number the vehicle was towed from.
     * @throws MessagingException             If the message cannot be sent.
     * @throws UnsupportedEncodingException   If encoding the sender name fails.
     */
    public void sendTowingNoticeEmail(String toEmail, String vehicleNumber, int spotNumber) 
            throws MessagingException, UnsupportedEncodingException {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SendMailConfig.USERNAME, SendMailConfig.SENDER_NAME));
            msg.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail, false)
            );
            msg.setSubject("Vehicle Towed and Late Charge Notification from BPARK");

            msg.setText(
                "Hello,\n\n" +
                "Your vehicle with license plate " + vehicleNumber +
                " was towed from parking spot #" + spotNumber +
                " after exceeding the maximum allowed parking duration.\n\n" +
                "Additionally, this is your third late incident. As per BPARK policy, " +
                "an extra late charge has been applied to your account.\n\n" +
                "To retrieve your vehicle, please contact our service center.\n\n" +
                "Best regards,\n" +
                SendMailConfig.SENDER_NAME
            );

            System.out.println("[EmailSender] → Transport.send() (TowingNotice)");
            Transport.send(msg);
            System.out.println("[EmailSender] Towing email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[EmailSender] Failed to send towing notice email to " + toEmail);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sends a combined towing and late charge notification email to a subscriber.
     * This email is triggered when a vehicle is towed and it's the subscriber's third late occurrence.
     *
     * @param toEmail       The recipient's email address.
     * @param vehicleNumber The license plate number of the towed vehicle.
     * @param spotNumber    The parking spot number the vehicle was towed from.
     * @throws MessagingException           If the message cannot be sent.
     * @throws UnsupportedEncodingException If encoding the sender name fails.
     */
    public void sendTowingWithLateChargeEmail(String toEmail, String vehicleNumber, int spotNumber)
            throws MessagingException, UnsupportedEncodingException {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SendMailConfig.USERNAME, SendMailConfig.SENDER_NAME));
            msg.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail, false)
            );
            msg.setSubject("Vehicle Towed & Late Charge Notification from BPARK");

            msg.setText(
                "Hello,\n\n" +
                "Your vehicle with license plate " + vehicleNumber +
                " was towed from parking spot #" + spotNumber +
                " after exceeding the maximum allowed parking duration.\n\n" +
                "Additionally, since this is your third late occurrence, a late fee has been applied to your account.\n\n" +
                "Please make sure to exit on time in the future to avoid towing and charges.\n\n" +
                "Best regards,\n" +
                SendMailConfig.SENDER_NAME
            );

            System.out.println("[EmailSender] → Transport.send() (TowingWithLateCharge)");
            Transport.send(msg);
            System.out.println("[EmailSender] Combined towing + late charge email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[EmailSender] Failed to send towing + late charge email to " + toEmail);
            e.printStackTrace();
            throw e;
        }
    }

    
}
