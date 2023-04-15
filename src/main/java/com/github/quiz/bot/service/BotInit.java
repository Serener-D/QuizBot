package com.github.quiz.bot.service;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.callback.Callback;
import com.github.quiz.bot.service.callback.CallbackHandler;
import com.github.quiz.bot.service.command.Command;
import com.github.quiz.bot.service.command.CommandHandler;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class BotInit extends TelegramLongPollingBot {

    private static final String DEFAULT_RESPONSE = "Something went wrong";

    private final ConversationStateHolder conversationStateHolder;
    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final FlashCardDao flashCardDao;


    public BotInit(String botToken, CommandHandler commandHandler, CallbackHandler callbackHandler, ConversationStateHolder conversationStateHolder, FlashCardDao flashCardDao) {
        super(botToken);
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.conversationStateHolder = conversationStateHolder;
        this.flashCardDao = flashCardDao;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (isCommand(update)) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            Command command = Command.valueOf(text.toUpperCase().substring(1));
            Response response = commandHandler.handle(command, chatId);
            executeResponse(response, chatId);
        } else if (isCallback(update)) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String[] data = update.getCallbackQuery().getData().split(" ");
            Response response = callbackHandler.handle(Callback.valueOf(data[0]), chatId, data[1]);
            executeResponse(response, chatId);
        } else {
            handleMessage(update);
        }
    }

    @SneakyThrows
    private void executeResponse(Response response, Long chatId) {
        execute(SendMessage.builder().chatId(chatId).text(response.getMessage()).replyMarkup(response.getReplyMarkup()).build());
    }

    @SneakyThrows
    private void handleMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String response = DEFAULT_RESPONSE;
        if (isNewCardReceived(update)) {
            saveCardAnswerQuestionToContext(update);
            response = "Enter card answer";
        } else if (isWaitingAnswerNewCard(chatId)) {
            saveCardAnswerToContext(update);
            response = "Enter category";
        } else if (isWaitingCategoryForNewCard(chatId)) {
            saveCardToDatabase(update);
            response = "FlashCard saved";
        }
        execute(SendMessage.builder().chatId(chatId).text(response).build());
    }

    private void saveCardAnswerQuestionToContext(Update update) {
        Long chatId = update.getMessage().getChatId();
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.WAITING_CARD_ANSWER);
        Queue<FlashCard> flashCardQueue = new LinkedList<>();
        flashCardQueue.add(FlashCard.builder().chatId(chatId).question(update.getMessage().getText()).build());
        conversationStateHolder.putCardQueue(chatId, flashCardQueue);
    }

    private void saveCardAnswerToContext(Update update) {
        Long chatId = update.getMessage().getChatId();
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.WAITING_CATEGORY);
        conversationStateHolder.getCardQueue(chatId).element().setAnswer(update.getMessage().getText());
    }

    private void saveCardToDatabase(Update update) {
        Long chatId = update.getMessage().getChatId();
        FlashCard flashCard = conversationStateHolder.getCardQueue(chatId).poll();
        flashCard.setCategory(update.getMessage().getText());
        flashCardDao.save(flashCard);
        conversationStateHolder.clearState(chatId);
        conversationStateHolder.clearCardQueue(chatId);
    }

    private boolean isCommand(Update update) {
        return Optional.ofNullable(update.getMessage()).map(Message::isCommand).orElse(false);
    }

    private boolean isCallback(Update update) {
        return update.getCallbackQuery() != null;
    }

    private boolean isNewCardReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        return !update.getMessage().isCommand() && (conversationStateHolder.getState(chatId) == null
                || conversationStateHolder.getState(chatId) == ConversationStateHolder.ConversationState.IDLE);
    }

    private boolean isWaitingCategoryForNewCard(Long chatId) {
        return conversationStateHolder.getState(chatId) == ConversationStateHolder.ConversationState.WAITING_CATEGORY;
    }

    private boolean isWaitingAnswerNewCard(Long chatId) {
        return conversationStateHolder.getState(chatId) == ConversationStateHolder.ConversationState.WAITING_CARD_ANSWER;
    }

    @Override
    public String getBotUsername() {
        return "learn_quiz_bot";
    }

}
