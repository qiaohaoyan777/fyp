package com.cmt322.usmsecondhand.service.impl;

import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class GmailEmailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 优化：直接读取 yml 里的密码配置，防止诊断日志报错
    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.application.name:USM Campus Swap}")
    private String appName;

    public boolean sendVerificationCode(String toEmail, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, appName));
            helper.setTo(toEmail);
            helper.setSubject("USM Campus Swap - Email Verification Code");

            // 优化：发送美化后的 HTML 格式邮件
            String htmlContent = buildSimpleHtmlContent(verificationCode);
            // 注意：这里的 true 非常关键，它告诉邮件客户端解析 HTML 标签
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("✅ Verification email successfully sent to: {}", toEmail);
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}", toEmail, e);
            return false;
        }
    }

    /**
     * 构建优化的 HTML 邮件内容
     */
    private String buildSimpleHtmlContent(String code) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;'>" +
                "<h2 style='color: #333;'>USM Campus Swap Email Verification</h2>" +
                "<p style='color: #555; font-size: 16px;'>Your verification code is:</p>" +
                "<div style='background-color: #f4f8ff; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;'>" +
                "<h3 style='color: #4285F4; font-size: 32px; letter-spacing: 5px; margin: 0;'>" + code + "</h3>" +
                "</div>" +
                "<p style='color: #555; font-size: 14px;'>This code will expire in <b>5 minutes</b>.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;' />" +
                "<p style='color: #999; font-size: 12px;'>If you didn't request this, please ignore this email.</p>" +
                "</div>";
    }

    /**
     * 测试 Gmail SMTP 连接
     */
    public boolean testConnection() {
        try {
            log.info("Testing Gmail SMTP connection...");

            // 尝试发送测试邮件到自己的邮箱
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, appName));
            helper.setTo(fromEmail); // 发送给自己
            helper.setSubject("✅ Gmail SMTP Test - " + new Date());
            helper.setText("<h3 style='color: green;'>Gmail SMTP connection is working properly!</h3>", true);

            mailSender.send(message);

            log.info("✅ Gmail SMTP test successful!");
            return true;
        } catch (Exception e) {
            log.error("❌ Gmail SMTP test failed: {}", e.getMessage());

            // 提供诊断信息
            if (e.getMessage() != null && e.getMessage().contains("535")) {
                log.error("""

                         ⚠️  COMMON GMAIL ISSUES:

                         1. TWO-FACTOR AUTHENTICATION REQUIRED
                            • Enable 2FA: https://myaccount.google.com/security
                            • Generate app password: https://myaccount.google.com/apppasswords

                         2. APP PASSWORD VS LOGIN PASSWORD
                            • ❌ Don't use your Gmail login password
                            • ✅ Use 16-character app password

                         Current config:
                         • Host: smtp.gmail.com
                         • Port: 587
                         • Username: {}
                         • Password length: {}
                         """,
                        fromEmail,
                        mailPassword != null ? mailPassword.length() : 0);
            }

            return false;
        }
    }
}