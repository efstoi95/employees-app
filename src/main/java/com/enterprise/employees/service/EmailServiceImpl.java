package com.enterprise.employees.service;

import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;


@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;


    /**
     * Sends an email to the specified recipient with the given subject and message.
     *
     * @param  toEmail       the email address of the recipient
     * @param  subject  the subject of the email
     */
    @Override
            mailSender.send(mailMessage);





    }





}
