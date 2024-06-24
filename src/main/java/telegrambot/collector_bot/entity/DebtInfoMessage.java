package telegrambot.collector_bot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class DebtInfoMessage {
    private String debtorUsername;
    private int debtSum;
    private String debtDescription;
}
