package telegrambot.collector_bot.repository;

public interface BotMessagesRU {
    String BOT_FUNCTIONALITY_DESCRIPTION = """
    Этот бот-коллектор помогает в управлении долгами и напоминает о них должникам.
    Вы можете отправить боту данные должника, включая его пользовательское имя (username),
    сумму долга и описание. После этого бот будет периодически напоминать должнику
    о необходимости погашения долга.

    Кроме того, вы можете управлять списком долгов через этого бота.
    Это удобный инструмент для поддержания финансовой дисциплины и своевременного возврата средств.
    """;

    String SEND_DEBT_DESCRIBTION = "Отправьте описание долга (если его нет, то '-')";
    String SEND_DEBT_SUM = "Отправьте сумму долга";
    String DEBT_DELETED = "Долг успешно удален";
    String SEND_USERNAME = "Отправьте @username должника";
    String ENTER_SUM_OF_DEBT_AS_A_NUMBER = "Введите сумму долга в виде числа";
    String CHOOSE_CURRENCY = "Выберите валюту";


    String CHOOSE_CURRENCY_FROM_BUTTONS = "Пожалуйста, выберите валюту, нажав на одну из кнопок.";
    String ADDED_DEBT = "Вы успешно добавили долг!";
    String ADD_DEBT = "Сначала добавьте долг";
    String DEBT_SUM_EDITED = "Сумма долга успешно изменена";
    String DEBT_DESCRIBTION_EDITED = "Описание долга успешно изменено";
    String CHOOSE_BUTTON = "Выберите действие:";
    String INFO_ABOUT_DEBT = """
                            %d. Должник: %s
                               Сумма: %d
                               Валюта: %s
                               Описание: %s
                            """;
}
