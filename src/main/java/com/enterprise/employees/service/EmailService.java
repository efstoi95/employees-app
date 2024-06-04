package com.enterprise.employees.service;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {

void sendEmail(String to, String subject, String message) throws UnsupportedEncodingException, MessagingException;

}
