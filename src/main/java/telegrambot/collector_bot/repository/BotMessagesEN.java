package telegrambot.collector_bot.repository;

public interface BotMessagesEN {
    String BOT_FUNCTIONALITY_DESCRIPTION = """
    This debt collector bot helps manage debts and sends reminders to debtors. 
    You can send the bot debtor information, including their username, 
    the amount of debt, and a description. After that, the bot will periodically remind 
    the debtor to repay the debt.

    Additionally, you can manage your list of debts through this bot. 
    It's a convenient tool for maintaining financial discipline and ensuring timely repayment.
    """;

    String SEND_DEBT_DESCRIBTION = "Send a description of the debt (if there is none, then '-')";
    String SEND_DEBT_SUM = "Send the amount of debt";
    String DEBT_DELETED = "Debt deleted successfully";
    String SEND_USERNAME = "Send debtor's @username";
    String ENTER_SUM_OF_DEBT_AS_A_NUMBER = "Enter the amount of debt as a number";
    String CHOOSE_CURRENCY = "Choose a currency";
    String CHOOSE_CURRENCY_FROM_BUTTONS = "Please select the currency by clicking on one of the buttons.";
    String ADDED_DEBT = "You have successfully added debt!";
    String ADD_DEBT = "Firstly, add debt";
    String DEBT_SUM_EDITED = "Debt sum successfully edited";
    String DEBT_DESCRIBTION_EDITED = "Debt describtion successfully edited";
    String CHOOSE_BUTTON = "select an action:";
    String INFO_ABOUT_DEBT = """
            %d Должник: %s
               Сумма: %d
               Валюта: %s
               Описание: %s
            """;
}
