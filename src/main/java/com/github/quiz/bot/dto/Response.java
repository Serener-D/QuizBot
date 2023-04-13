package com.github.quiz.bot.dto;

import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Data
@Builder
public class Response {

    private String message;
    private ReplyKeyboard replyMarkup;

}
