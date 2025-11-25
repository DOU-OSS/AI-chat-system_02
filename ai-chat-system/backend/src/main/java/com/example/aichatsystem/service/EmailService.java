package com.example.aichatsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@aichatsystem.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    @Value("${email.debug-mode:false}")
    private boolean debugMode;
    
    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("重置您的AI聊天系统密码");
            
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String htmlContent = buildPasswordResetEmailContent(resetLink);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", to, e);
            logger.info("Password reset link would be: {}/reset-password?token={}", frontendUrl, token);
        } catch (Exception e) {
            logger.error("Email service not configured. Reset token: {}", token);
        }
    }
    
    private String buildPasswordResetEmailContent(String link) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #333;'>重置您的密码</h2>" +
                "<p>您请求重置AI聊天系统的密码。请点击下面的链接设置新密码：</p>" +
                "<a href='" + link + "' style='display: inline-block; padding: 10px 20px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0;'>重置密码</a>" +
                "<p>或者复制以下链接到浏览器：</p>" +
                "<p style='word-break: break-all; color: #666;'>" + link + "</p>" +
                "<p>此链接将在24小时后失效。</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
                "<p style='color: #999; font-size: 12px;'>如果您没有请求重置密码，请忽略此邮件，您的密码不会被更改。</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    public void sendRegistrationVerificationCode(String to, String code) {
        // 开发模式下直接打印到控制台
        if (debugMode) {
            logger.warn("=".repeat(60));
            logger.warn("开发模式 - 邮件验证码");
            logger.warn("收件人: {}", to);
            logger.warn("验证码: {}", code);
            logger.warn("=".repeat(60));
            return;
        }
        
        if (mailSender == null) {
            logger.warn("Email service not configured. Verification code for {}: {}", to, code);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("AI聊天系统 - 注册验证码");
            
            String htmlContent = buildRegistrationVerificationEmailContent(code);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Registration verification code sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send registration verification email to: {}", to, e);
            logger.info("Verification code: {}", code);
        } catch (Exception e) {
            logger.error("Email service error. Verification code: {}", code, e);
        }
    }
    
    private String buildRegistrationVerificationEmailContent(String code) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #333;'>欢迎注册AI聊天系统！</h2>" +
                "<p>您的注册验证码是：</p>" +
                "<div style='background-color: #f0f0f0; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px; margin: 20px 0;'>" +
                code +
                "</div>" +
                "<p>请在10分钟内输入此验证码完成注册。</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
                "<p style='color: #999; font-size: 12px;'>如果您没有注册AI聊天系统，请忽略此邮件。</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
