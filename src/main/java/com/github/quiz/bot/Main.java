package com.github.quiz.bot;


import com.github.quiz.bot.config.DatabaseConfig;
import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.service.Bot;
import com.github.quiz.bot.service.ConversationStateHolder;
import com.github.quiz.bot.service.KeyboardCreator;
import com.github.quiz.bot.service.callback.CallbackHandler;
import com.github.quiz.bot.service.command.CommandHandler;
import com.github.quiz.bot.service.message.MessageHandler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        FlashCardDao flashCardDao = new FlashCardDao(DatabaseConfig.getSessionFactory());

        ConversationStateHolder conversationStateHolder = new ConversationStateHolder();
        KeyboardCreator keyboardCreator = new KeyboardCreator(conversationStateHolder);

        Bot bot = new Bot("6098897440:AAFiAazmAHe_ud-blP21M3LAsMOejrbH4hQ",
                new CommandHandler(conversationStateHolder, keyboardCreator, flashCardDao),
                new CallbackHandler(conversationStateHolder, keyboardCreator, flashCardDao),
                new MessageHandler(conversationStateHolder, flashCardDao));

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);

        System.out.println("Hello world!");
    }
}