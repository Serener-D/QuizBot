package com.github.quiz.bot.service;

import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.service.callback.Callback;
import com.github.quiz.bot.service.callback.CallbackHandler;
import com.github.quiz.bot.service.command.Command;
import com.github.quiz.bot.service.command.CommandHandler;
import com.github.quiz.bot.service.message.MessageHandler;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

public class Bot extends TelegramLongPollingBot {

    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final MessageHandler messageHandler;

    public Bot(String botToken, CommandHandler commandHandler, CallbackHandler callbackHandler, MessageHandler messageHandler) {
        super(botToken);
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Response response;
        Long chatId;
        if (isCommand(update)) {
            chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            Command command = Command.valueOf(text.toUpperCase().substring(1));
            response = commandHandler.handle(command, chatId);
        } else if (isCallback(update)) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            String[] data = update.getCallbackQuery().getData().split(" ");
            String arguments = data.length > 1 ? data[1] : null;
            response = callbackHandler.handle(Callback.valueOf(data[0]), chatId, arguments);
        } else {
            chatId = update.getMessage().getChatId();
            response = messageHandler.handle(update);
        }
        executeResponse(response, chatId);
    }

    @SneakyThrows
    private void executeResponse(Response response, Long chatId) {
        execute(SendMessage.builder().chatId(chatId).text(response.getMessage()).replyMarkup(response.getReplyMarkup()).build());
    }

    private boolean isCommand(Update update) {
        return Optional.ofNullable(update.getMessage()).map(Message::isCommand).orElse(false);
    }

    private boolean isCallback(Update update) {
        return update.getCallbackQuery() != null;
    }

    @Override
    public String getBotUsername() {
        return "learn_quiz_bot";
    }

}
