package com.miqdigital.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.miqdigital.dto.EmailDto;
import com.miqdigital.dto.ResultDto;

public class EmailUtil {

  private static final String ATTACHMENT_FILE_NAME = "./target/FailedTestsInfo.txt";

  /**
   * Creating an email body and sending the email.
   * @param emailDto
   * @param resultDto
   * @throws MessagingException
   * @throws IOException
   */
  public static void sendEmail(EmailDto emailDto, ResultDto resultDto)
      throws MessagingException, IOException {
    String msg = resultDto.testExecutionInfo.toString();

    Properties prop = new Properties();
    prop.put("mail.smtp.auth", true);
    prop.put("mail.smtp.starttls.enable", "true");
    prop.put("mail.smtp.host", emailDto.getEmailHost());
    prop.put("mail.smtp.port", emailDto.getEmailHost());
    prop.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");

    Session session = Session.getInstance(prop, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(emailDto.getSmtpUsername(), emailDto.getSmtpPassword());
      }
    });
    final String generatedFileName = Files.write(Paths.get(ATTACHMENT_FILE_NAME),
        resultDto.failedTestDescription.toString().getBytes()).normalize().toAbsolutePath()
        .toString();
    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(emailDto.getEmailFrom()));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDto.getEmailTo()));
    message.setSubject(emailDto.getSubject());


    MimeBodyPart attachmentBodyPart = new MimeBodyPart();
    attachmentBodyPart.attachFile(new File(generatedFileName));
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(msg, "text/html");

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(mimeBodyPart);
    multipart.addBodyPart(attachmentBodyPart);

    message.setContent(multipart);

    Transport.send(message);

  }

}
