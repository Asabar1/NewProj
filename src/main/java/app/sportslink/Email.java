// sportslink.mail@gmail.com    Group1Pass
// https://myaccount.google.com/apppasswords    SportsLink  sportslink.mail@gmail.com  hcqsrnyqmugvgnin

package app.sportslink;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;

public class Email {
    private Email(){}

    public static class CFG{
        private CFG(){}
        public static final String FROM_USER = "sportslink.mail@gmail.com";
        public static final String FROM_PASS ="hcqsrnyqmugvgnin";
        public static final String FROM_ADDRESS = "SportsLink Mail<sportslink.mail@gmail.com>";
        public static final String CONTENT_TYPE = "text/html; charset=utf-8";
    }

    private static Properties _prop = null;

    private static Properties getProp(){
        if (_prop == null) {
            _prop = new Properties();
            _prop.put("mail.smtp.auth", "true");
            _prop.put("mail.smtp.starttls.enable", "true");
            _prop.put("mail.smtp.host", "smtp.gmail.com");
            _prop.put("mail.smtp.port", "587");
        }
        return _prop;
    }

    private static boolean send(String to, String subject, String body){
        try {
            Session session = Session.getInstance(getProp(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(CFG.FROM_USER, CFG.FROM_PASS);
                }});
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(CFG.FROM_ADDRESS));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(body, CFG.CONTENT_TYPE);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);
            Transport.send(message);
            return true;
        }
        catch (jakarta.mail.MessagingException me){
            return false;
        }
    }

    public static boolean sendAccountCreated(User user){
        String subject = "Welcome to SportsLink App";
        String body = STR."Here's your confirmation code: \{user.getConfirmCode()}";
        return send(user.getEmail(), subject, body);
    }
}
