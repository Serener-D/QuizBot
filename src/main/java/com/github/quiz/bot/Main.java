package com.github.quiz.bot;


import com.github.quiz.bot.service.BotInit;
import com.github.quiz.bot.service.callback.CallbackHandler;
import com.github.quiz.bot.service.command.CommandHandler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        BotInit botInit = new BotInit("6098897440:AAFiAazmAHe_ud-blP21M3LAsMOejrbH4hQ", new CommandHandler(), new CallbackHandler());
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(botInit);
        System.out.println("Hello world!");
    }
}