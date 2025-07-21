package com.tj.tjp.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 메일 발송 서비스
 * - JavaMailSender를 이용하여 SMTP 기반 단순 텍스트 이메일 발송을 담당
 * - 인증메일, 알림메일 등 다양한 용도로 사용 가능
 */
@Service
@RequiredArgsConstructor
public class MailSenderService {
    /**
     * 스프링 부트의 메일 발송 컴포넌트(JavaMailSender)
     * - application.yml의 spring.mail.* 설정을 기반으로 동작
     */
    private final JavaMailSender javaMailSender;

    /**
     * 단순 텍스트 메일 발송
     * @param to      수신자 이메일 주소
     * @param subject 메일 제목
     * @param text    메일 본문(텍스트)
     */
    public void send(String to, String subject, String text, boolean isHtml) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, isHtml); // 여기서 true면 HTML로 발송
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 실패", e);
        }
    }

    // 기존 텍스트 메일용 send() 오버로드도 같이 둘 수 있음
    public void send(String to, String subject, String text) {
        send(to, subject, text, false);
    }
}
