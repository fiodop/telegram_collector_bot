package telegrambot.collector_bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegrambot.collector_bot.config.BotConfig;
import telegrambot.collector_bot.entity.Debt;
import telegrambot.collector_bot.entity.DebtOwner;
import telegrambot.collector_bot.repository.BotMessagesEN;
import telegrambot.collector_bot.repository.BotMessagesRU;
import telegrambot.collector_bot.repository.ChatState;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class TelegramBot extends TelegramLongPollingBot {
    private static boolean isEnglish;


    @Autowired
    private DebtService debtService;
    @Autowired
    private DebtOwnerService debtOwnerService;
    private Map<Long, DebtOwner> debtOwnerCache = new HashMap<>();
    private Map<Long, Debt> debtCache = new HashMap<>();
    private Map<Long, ChatState> chatStates = new HashMap<>();
    private BotMessagesRU botMessagesRU;

    private final BotConfig botConfig;

    @Autowired
    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    /**
     *  метод, которые обрабатывает пришедшие сообщения боту
     *  если это простая команда в начале которой стоит "/", вызывает методы соответсвующие этим командам
     *  если сообщение не начинается на "/", то тогда он вызывает метод handleStatefulMessage(), который обрабатывает такие сообщения
     * @param update обновление состояния, полученное от Telegram-API
     */

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if(chatStates.containsKey(chatId)){
                handleStatefulMessage(update);
            }
            else {
                switch (messageText) {
                    case "/start":
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        break;

                    case "Добавить долг", "Add debt":
                        chatStates.put(chatId, ChatState.ADD_NEW_DEBT);
                        handleStatefulMessage(update);
                        break;

                    case "Список долгов", "List of debts":
                        chatStates.put(chatId, ChatState.ALL_DEBTS);
                        handleStatefulMessage(update);
                        break;

                    case "Пропустить", "Skip":
                        Debt cachedDebt = debtCache.get(chatId);
                        cachedDebt.setDescription(null);

                        debtCache.put(chatId, cachedDebt);

                        chatStates.put(chatId, ChatState.SAVING_DEBT);
                        handleStatefulMessage(update);
                        break;
                    case "голанг говно":
                        golangIsShit(chatId);
                        break;

                    default:
                        sendMessage(chatId, "Данная команда не поддерживается");
                        break;
                }
            }
        } else if (update.hasCallbackQuery()){
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long debtId = 0;
            if(callbackData.startsWith("DELETE_DEBT_")){
                 debtId = Long.parseLong(callbackData.substring(12));

                callbackData = "DELETE_DEBT";
            }
            if(callbackData.startsWith("CHANGE_DEBT_SUM")){
                debtId = Long.parseLong(callbackData.substring(15));

                callbackData = "CHANGE_DEBT_SUM";
            }
            if (callbackData.startsWith("CHANGE_DEBT_DESCRIPTION")){
                debtId = Long.parseLong(callbackData.substring(23));

                callbackData = "CHANGE_DEBT_DESCRIPTION";
            }
            if(callbackData.startsWith("DISABLE_NOTIFICATIONS")){
                debtId = Long.parseLong(callbackData.substring(21));

                callbackData = "DISABLE_NOTIFICATIONS";
            }


            switch (callbackData){
                case "RUSSIAN_BUTTON":
                    buttonRussianPressed(chatId, messageId);
                    break;

                case "ENGLISH_BUTTON":
                    buttonEnglishPressed(chatId, messageId);
                    break;
                case "DELETE_DEBT":
                    buttonDeletePressed(chatId, messageId, debtId);
                    break;

                case "CHANGE_DEBT_SUM":
                    buttonChangeDebtSumPressed(chatId, debtId);
                    break;

                case "CHANGE_DEBT_DESCRIPTION":
                    buttonChangeDebtDescriptionPressed(chatId, debtId);
                    break;
                case "DISABLE_NOTIFICATIONS":
                    buttonDisableNotificationsPressed(chatId,debtId);
                    break;
                case "CANCEL":
                    buttonCancelPressed(chatId);
                    break;
            }
        }
    }

    private void buttonCancelPressed(long chatId) {
        debtCache.remove(chatId);
        chatStates.remove(chatId);
        firstKeyboardMessage(chatId);
    }

    private void golangIsShit(long chatId) {
       sendMessage(chatId, "Total users: " + debtOwnerService.countAllUsers());
       firstKeyboardMessage(chatId);
    }


    private void buttonDisableNotificationsPressed(long chatId, long debtId) {
        Debt debt = debtService.getDebtById(debtId);

        debt.setNotify(!debt.isNotify());
        if(isEnglish){
            if(debt.isNotify()){
                sendMessage(chatId, BotMessagesEN.NOTIFICATIONS_ABLED);
            } else {
                sendMessage(chatId, BotMessagesEN.NOTIFICATIONS_DISABLED);
            }
        }
        else {
            if(debt.isNotify()){
                sendMessage(chatId, BotMessagesRU.NOTIFICATIONS_ABLED);
            } else {
                sendMessage(chatId, BotMessagesRU.NOTIFICATIONS_DISABLED);
            }
        }
        debtService.addNewDebt(debt);

        firstKeyboardMessage(chatId);
    }

    private void buttonChangeDebtDescriptionPressed(long chatId, long debtId) {
        if(isEnglish){
            sendMessage(chatId, BotMessagesEN.SEND_DEBT_DESCRIBTION);
        }
        else {
            sendMessage(chatId, BotMessagesRU.SEND_DEBT_DESCRIBTION);
        }

        Debt debtToChange = debtService.getDebtById(debtId);
        debtCache.put(chatId, debtToChange);
        chatStates.put(chatId, ChatState.CHANGE_DEBT_DESCRIPTION);
    }

    private void buttonChangeDebtSumPressed(long chatId, long debtId) {
        if(isEnglish){
            sendMessage(chatId, BotMessagesEN.SEND_DEBT_SUM);
        }
        else {
            sendMessage(chatId, BotMessagesRU.SEND_DEBT_SUM);
        }

        Debt debtToChange = debtService.getDebtById(debtId);

        debtCache.put(chatId, debtToChange);
        chatStates.put(chatId, ChatState.CHANGE_DEBT_SUM);
    }

    private void buttonDeletePressed(long chatId, long messageId, long debtId) {
        debtService.deleteDebtById(debtId);

        EditMessageText editedMessage = new EditMessageText();
        editedMessage.setChatId(String.valueOf(chatId));
        editedMessage.setMessageId((int) messageId);

        if(isEnglish){
            editedMessage.setText(BotMessagesEN.DEBT_DELETED);
        }
        else {
            editedMessage.setText(BotMessagesRU.DEBT_DELETED);
        }
        try {
            execute(editedMessage);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }

    }


    /**
     * метод, который обрабатывает сообщения которые не являются командами
     */
    private void handleStatefulMessage(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        ChatState chatState = chatStates.get(chatId);
        String username = update.getMessage().getChat().getUserName();

        switch (chatState) {
            //Добавление нового долга
            case AWAITING_COMMAND -> {
                if (messageText.equals("Добавить долг") || messageText.equals("Add debt")) {
                    chatStates.put(chatId, ChatState.ADD_NEW_DEBT);
                    handleStatefulMessage(update);
                } else if (messageText.equals("Список долгов") || messageText.equals("List of debts")) {
                    chatStates.put(chatId, ChatState.ALL_DEBTS);
                    handleStatefulMessage(update);
                }
            }
            case ADD_NEW_DEBT -> {
                if (debtOwnerService.findByUsername(username) == null) {
                    DebtOwner debtOwner = new DebtOwner(username);
                    debtOwnerService.addNewDebtOwner(debtOwner);
                    debtOwner.setIsEnglish(isEnglish);
                }
                DebtOwner debtOwner = debtOwnerService.findByUsername(username);
                debtOwnerCache.put(chatId, debtOwner);
                Debt cachedDebt = new Debt();
                cachedDebt.setDebtOwner(debtOwner);

                debtCache.put(chatId, cachedDebt);

                chatStates.put(chatId, ChatState.AWAITING_DEBTOR_USERNAME);
                String textMessage;
                if (isEnglish) {
                    textMessage = BotMessagesEN.SEND_USERNAME;
                } else {
                    textMessage = BotMessagesRU.SEND_USERNAME;
                }

                SendMessage message = addCancelButton(chatId, textMessage);

                executeMessage(message);

            }
            case AWAITING_DEBTOR_USERNAME -> {
                Debt cachedDebt = debtCache.get(chatId);
                cachedDebt.setUsername(messageText);


                debtCache.put(chatId, cachedDebt);
                chatStates.put(chatId, ChatState.AWAITING_DEBT_SUM);
                String textMessage;
                if (isEnglish) {
                    textMessage = BotMessagesEN.SEND_DEBT_SUM;
                } else {
                    textMessage = BotMessagesRU.SEND_DEBT_SUM;
                }
                SendMessage message = addCancelButton(chatId, textMessage);

                executeMessage(message);

            }
            case AWAITING_DEBT_SUM -> {
                int debtSum;
                String textMessage;
                try {
                    debtSum = Integer.parseInt(messageText);
                } catch (NumberFormatException e) {

                    if (isEnglish) {
                        textMessage = BotMessagesEN.ENTER_SUM_OF_DEBT_AS_A_NUMBER;
                    } else {
                        textMessage = BotMessagesRU.ENTER_SUM_OF_DEBT_AS_A_NUMBER;
                    }
                    SendMessage message = addCancelButton(chatId, textMessage);
                    executeMessage(message);
                    break;

                }
                Debt cachedDebt = debtCache.get(chatId);
                cachedDebt.setDebtAmount(debtSum);

                debtCache.put(chatId, cachedDebt);
                chatStates.put(chatId, ChatState.AWAITING_DEBT_CURRENCY);
                List<String> buttons = new ArrayList<>();
                buttons.add("$");
                buttons.add("₽");
                ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup(buttons, 2);

                SendMessage message = new SendMessage();
                message.setReplyMarkup(replyKeyboardMarkup);
                if (isEnglish) {
                    message.setText(BotMessagesEN.CHOOSE_CURRENCY);
                } else {
                    message.setText(BotMessagesRU.CHOOSE_CURRENCY);
                }
                message.setChatId(String.valueOf(chatId));


                executeMessage(message);
            }

            case AWAITING_DEBT_CURRENCY -> {
                Debt cachedDebt = debtCache.get(chatId);
                if (messageText.equals("$") || messageText.equals("₽")) {
                    cachedDebt.setCurrency(messageText);
                    debtCache.put(chatId, cachedDebt);
                    chatStates.put(chatId, ChatState.AWAITING_DEBT_DESCRIPTION);

                    SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(chatId));

                    List<String> buttons = new ArrayList<>();
                    if (isEnglish) {
                        buttons.add("Skip");
                        message.setText(BotMessagesEN.SEND_DEBT_DESCRIBTION);
                    } else {
                        buttons.add("Пропустить");
                        message.setText(BotMessagesRU.SEND_DEBT_DESCRIBTION);
                    }
                    ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup(buttons, 1);
                    message.setReplyMarkup(replyKeyboardMarkup);

                    executeMessage(message);


                } else {
                    if (isEnglish) {
                        sendMessage(chatId, BotMessagesEN.CHOOSE_CURRENCY_FROM_BUTTONS);
                    } else {
                        sendMessage(chatId, BotMessagesRU.CHOOSE_CURRENCY_FROM_BUTTONS);
                    }
                }
            }

            case AWAITING_DEBT_DESCRIPTION -> {
                Debt cachedDebt = debtCache.get(chatId);
                if (messageText.equals("Пропустить") || messageText.equals("Skip")) {
                    cachedDebt.setDescription(null);
                } else {
                    cachedDebt.setDescription(messageText);
                }

                debtCache.put(chatId, cachedDebt);
                chatStates.put(chatId, ChatState.SAVING_DEBT);
                handleStatefulMessage(update);
            }
            case SAVING_DEBT -> {
                Debt cachedDebt = debtCache.get(chatId);
                cachedDebt.setDateTime(ZonedDateTime.now());
                debtService.addNewDebt(cachedDebt);
                if (isEnglish) {
                    sendMessage(chatId, BotMessagesEN.ADDED_DEBT);
                } else {
                    sendMessage(chatId, BotMessagesRU.ADDED_DEBT);
                }
                firstKeyboardMessage(chatId);
                chatStates.remove(chatId);

                debtCache.remove(chatId);
            }

            // Просмотр всех долгов
            case ALL_DEBTS -> {
                DebtOwner debtOwner = debtOwnerCache.get(chatId);
                if (debtOwner == null) {
                    if (isEnglish) {
                        sendMessage(chatId, BotMessagesEN.ADD_DEBT);
                    } else {
                        sendMessage(chatId, BotMessagesRU.ADD_DEBT);
                    }
                    chatStates.remove(chatId);
                    break;
                }

                List<Debt> allDebts = debtService.getAllDebts(debtOwner);
                if(allDebts.isEmpty()){
                    if (isEnglish) {
                        sendMessage(chatId, BotMessagesEN.ADD_DEBT);
                    } else {
                        sendMessage(chatId, BotMessagesRU.ADD_DEBT);
                    }
                    chatStates.put(chatId, ChatState.AWAITING_COMMAND);
                    break;
                }

                for (int i = 0; i < allDebts.size(); i++) {
                    Debt debt = allDebts.get(i);
                    String debtorUsername = debt.getUsername();
                    int sum = debt.getDebtAmount();
                    String currency = debt.getCurrency();
                    String description = debt.getDescription();


                    StringBuilder message;
                    if (isEnglish) {
                        String notifications;
                        if(debt.isNotify()){
                            notifications = "able";
                        } else {
                            notifications = "disabled";
                        }

                        message = new StringBuilder(String.format(BotMessagesEN.INFO_ABOUT_DEBT, i + 1,
                                debtorUsername, sum, currency, description != null ? description : "-", notifications));
                    } else {
                        String notifications;
                        if(debt.isNotify()){
                            notifications = "включены";
                        } else {
                            notifications = "выключены";
                        }
                        message = new StringBuilder(String.format(BotMessagesRU.INFO_ABOUT_DEBT, i + 1,
                                debtorUsername, sum, currency, description != null ? description : "-", notifications));
                    }
                        HashMap<String, String> buttons = new HashMap<>();
                        if (isEnglish) {
                            buttons.put("Delete", "DELETE_DEBT_" + debt.getId());
                            buttons.put("Edit debt sum", "CHANGE_DEBT_SUM" + debt.getId());
                            buttons.put("Edit debt describtion", "CHANGE_DEBT_DESCRIPTION" + debt.getId());
                            if (debt.isNotify()){
                                buttons.put("Disable notifications", "DISABLE_NOTIFICATIONS" + debt.getId());
                            }
                            else {
                                buttons.put("Able notifications", "DISABLE_NOTIFICATIONS" + debt.getId());
                            }
                        } else {
                            buttons.put("Удалить", "DELETE_DEBT_" + debt.getId());
                            buttons.put("Изменить сумму долга", "CHANGE_DEBT_SUM" + debt.getId());
                            buttons.put("Изменить описание", "CHANGE_DEBT_DESCRIPTION" + debt.getId());
                            if (debt.isNotify()){
                                buttons.put("Отключить уведомления", "DISABLE_NOTIFICATIONS" + debt.getId());
                            } else {
                                buttons.put("Включить уведомления", "DISABLE_NOTIFICATIONS" + debt.getId());
                            }
                        }

                        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboard(buttons, 2);

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(String.valueOf(chatId));
                        sendMessage.setText(String.valueOf(message));
                        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

                        executeMessage(sendMessage);
                    }


                 chatStates.remove(chatId);
                }
                //Изменение суммы долга
                case CHANGE_DEBT_SUM -> {
                    int debtSum;
                    try {
                        debtSum = Integer.parseInt(messageText);
                    } catch (NumberFormatException e) {
                        if (isEnglish) {
                            sendMessage(chatId, BotMessagesEN.ENTER_SUM_OF_DEBT_AS_A_NUMBER);
                        }
                        else {
                            sendMessage(chatId, BotMessagesRU.ENTER_SUM_OF_DEBT_AS_A_NUMBER);
                        }
                        break;
                    }
                    Debt cachedDebt = debtCache.get(chatId);
                    cachedDebt.setDebtAmount(debtSum);
                    debtService.addNewDebt(cachedDebt);
                    if (isEnglish) {
                        sendMessage(chatId, BotMessagesEN.DEBT_SUM_EDITED);
                    }
                    else {
                        sendMessage(chatId, BotMessagesRU.DEBT_SUM_EDITED);
                    }
                    chatStates.put(chatId, ChatState.AWAITING_COMMAND);
                    firstKeyboardMessage(chatId);
                }

                // Изменение описания долга
                case CHANGE_DEBT_DESCRIPTION -> {
                    Debt cachedDebt = debtCache.get(chatId);
                    if (messageText.equals("-")) {
                        cachedDebt.setDescription(null);
                    } else {
                        cachedDebt.setDescription(messageText);
                    }
                    debtService.addNewDebt(cachedDebt);
                    if (isEnglish) {
                        sendMessage(chatId, BotMessagesEN.DEBT_DESCRIBTION_EDITED);
                    }
                    else {
                        sendMessage(chatId, BotMessagesRU.DEBT_DESCRIBTION_EDITED);
                    }

                    chatStates.put(chatId, ChatState.AWAITING_COMMAND);
                    firstKeyboardMessage(chatId);
                }
            }
        }

        private SendMessage addCancelButton(long chatId, String messageText){
            HashMap<String, String> buttons = new HashMap<>();
            if(isEnglish){
                buttons.put("Cancel", "CANCEL");
            }else {
                buttons.put("Отмена", "CANCEL");
            }

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(messageText);
            message.setReplyMarkup(getInlineKeyboard(buttons, 1));

            return message;
        }
        private void executeMessage(SendMessage message){
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        private void sendMessage ( long chatId, String textToSend){
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(textToSend);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        public void startCommandReceived ( long chatId, String name){

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            String answer = "Hi, " + name + " choose language";
            message.setText(answer);


            HashMap<String, String> ruEngButtons = new HashMap<>();
            String ruButtonText = "Russian";
            String ruButtonCallbackData = "RUSSIAN_BUTTON";
            ruEngButtons.put(ruButtonText, ruButtonCallbackData);

            String engButtonText = "English";
            String engButtonCallbackData = "ENGLISH_BUTTON";
            ruEngButtons.put(engButtonText, engButtonCallbackData);
            InlineKeyboardMarkup keyboardRusEng = getInlineKeyboard(ruEngButtons, 2);


            message.setReplyMarkup(keyboardRusEng);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            chatStates.put(chatId, ChatState.AWAITING_COMMAND);
        }


        /**
         * @return клавиатуру с кнопками под сообщением
         */
        private static InlineKeyboardMarkup getInlineKeyboard (HashMap < String, String > buttons,int maxButtonsPerRow){
            List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();
            List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
            List<String> buttonNames = new ArrayList<>(buttons.keySet());
            int cnt = 0;

            for (int i = 0; i < buttons.size(); i++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(buttonNames.get(i));
                button.setCallbackData(buttons.get(buttonNames.get(i)));
                buttonsRow.add(button);
                cnt++;

                if (cnt == maxButtonsPerRow) {
                    buttonRows.add(buttonsRow);
                    buttonsRow = new ArrayList<>();
                    cnt = 0;
                }
            }

            if (!buttonsRow.isEmpty()) {
                buttonRows.add(buttonsRow);
            }
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(buttonRows);
            return inlineKeyboardMarkup;
        }


        public void buttonRussianPressed ( long chatId, long messageId){
            EditMessageText message = new EditMessageText();
            message.setChatId(String.valueOf(chatId));
            message.setMessageId((int) messageId);
            message.setText(BotMessagesRU.BOT_FUNCTIONALITY_DESCRIPTION);

            isEnglish = false;

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            firstKeyboardMessage(chatId);
        }

        private void firstKeyboardMessage ( long chatId){
            SendMessage firstKeyboardMessage = new SendMessage();
            firstKeyboardMessage.setChatId(String.valueOf(chatId));

            List<String> buttonsText = new ArrayList<>();
            if (isEnglish) {
                firstKeyboardMessage.setText(BotMessagesEN.CHOOSE_BUTTON);
                buttonsText.add("Add debt");
                buttonsText.add("List of debts");
            } else {
                firstKeyboardMessage.setText(BotMessagesRU.CHOOSE_BUTTON);
                buttonsText.add("Добавить долг");
                buttonsText.add("Список долгов");
            }

            ReplyKeyboardMarkup keyboardMarkup = getReplyKeyboardMarkup(buttonsText, 2);
            firstKeyboardMessage.setReplyMarkup(keyboardMarkup);

            try {
                execute(firstKeyboardMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        private ReplyKeyboardMarkup getReplyKeyboardMarkup (List < String > buttons,int maxButtonPerRow){
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);

            List<KeyboardRow> keyboardRows = new ArrayList<>();
            KeyboardRow buttonsRow = new KeyboardRow();

            int cnt = 0;
            for (int i = 0; i < buttons.size(); i++) {
                buttonsRow.add(buttons.get(i));
                cnt++;

                if (cnt == maxButtonPerRow) {
                    keyboardRows.add(buttonsRow);
                    buttonsRow = new KeyboardRow();
                    cnt = 0;
                }
            }

            if (!buttonsRow.isEmpty()) {
                keyboardRows.add(buttonsRow);
            }

            keyboardMarkup.setKeyboard(keyboardRows);
            return keyboardMarkup;
        }


        public void buttonEnglishPressed ( long chatId, long messageId){
            EditMessageText message = new EditMessageText();
            message.setChatId(String.valueOf(chatId));
            message.setMessageId((int) messageId);
            message.setText(BotMessagesEN.BOT_FUNCTIONALITY_DESCRIPTION);

            isEnglish = true;

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            firstKeyboardMessage(chatId);
        }




}
