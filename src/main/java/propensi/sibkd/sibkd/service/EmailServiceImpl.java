package propensi.sibkd.sibkd.service;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.transaction.Transactional;
import java.util.Properties;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {
    @Override
    public void sendEmail(String destinationEmail, String msg, String subject) {
        //modified from https://www.javacodemonk.com/spring-boot-send-email-with-gmail-smtp-5caea8f3
        String to = destinationEmail; 
        final String from = "sistem.sibkd@gmail.com"; 
        final String password = "PropensiD012022"; 

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", true);
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);


            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);


            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Mail successfully sent..");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}