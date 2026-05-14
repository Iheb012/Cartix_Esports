package services;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    private final String host     = "smtp-relay.brevo.com";
    private final int    port     = 587;
    private final String username = "..............";   // ← replace this
    private final String password = "..................";      // ← replace this
    private final String sender   = "ouerfelli.roua98@gmail.com";

    public String sendResetCode(String toEmail) throws MessagingException, UnsupportedEncodingException {
        String code = String.format("%06d", new Random().nextInt(999999));

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            host);
        props.put("mail.smtp.port",            String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender, "Cartix"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Cartix — Reset Your Password");

        // HTML email body
        String html =
                "<!DOCTYPE html>" +
                        "<html><head><meta charset='UTF-8'>" +
                        "<style>" +
                        "  body { margin:0; padding:0; background:#0d1117;" +
                        "         font-family:'Segoe UI',sans-serif; }" +
                        "  .wrap { max-width:480px; margin:40px auto; background:rgba(22,26,46,0.98);" +
                        "          border:1px solid rgba(77,120,255,0.2); border-radius:16px;" +
                        "          overflow:hidden; }" +
                        "  .header { background:linear-gradient(135deg,#4d78ff,#7c4dff);" +
                        "            padding:32px; text-align:center; }" +
                        "  .logo { font-size:26px; font-weight:800; color:white; letter-spacing:2px; }" +
                        "  .logo span { color:#b3c8ff; }" +
                        "  .body { padding:36px 32px; }" +
                        "  h2 { color:white; font-size:20px; margin:0 0 12px; }" +
                        "  p { color:#9aa3c7; font-size:14px; line-height:1.7; margin:0 0 20px; }" +
                        "  .code-box { background:#0d1117; border:1px solid rgba(77,120,255,0.3);" +
                        "              border-radius:10px; padding:20px; text-align:center;" +
                        "              margin:24px 0; }" +
                        "  .code { font-size:36px; font-weight:800; letter-spacing:10px;" +
                        "          color:#4d78ff; }" +
                        "  .footer { padding:20px 32px; border-top:1px solid rgba(255,255,255,0.06);" +
                        "            text-align:center; }" +
                        "  .footer p { color:#4a5070; font-size:12px; margin:0; }" +
                        "</style></head><body>" +
                        "<div class='wrap'>" +
                        "  <div class='header'><div class='logo'>CAR<span>TIX</span></div></div>" +
                        "  <div class='body'>" +
                        "    <h2>Password Reset Request</h2>" +
                        "    <p>We received a request to reset the password for your Cartix account." +
                        "       Use the code below to continue. It expires in <strong style='color:white'>10 minutes</strong>.</p>" +
                        "    <div class='code-box'><div class='code'>" + code + "</div></div>" +
                        "    <p>If you did not request a password reset, you can safely ignore this email." +
                        "       Your password will not be changed.</p>" +
                        "  </div>" +
                        "  <div class='footer'>" +
                        "    <p>This email was sent by Cartix · donotreply@cartix.com</p>" +
                        "    <p>© 2026 Cartix. All rights reserved.</p>" +
                        "  </div>" +
                        "</div>" +
                        "</body></html>";

        // Send as multipart so both HTML and plain text are included
        MimeMultipart multipart = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(
                "Cartix — Password Reset\n\n" +
                        "Your reset code is: " + code + "\n\n" +
                        "This code expires in 10 minutes.\n\n" +
                        "If you did not request this, ignore this email.\n\n" +
                        "— The Cartix Team"
        );

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=UTF-8");

        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        Transport.send(message);
        return code;
    }
}