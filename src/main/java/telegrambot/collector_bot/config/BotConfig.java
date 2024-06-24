package telegrambot.collector_bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
@Data
public class BotConfig {
    @Value("CollectorMoneyBot")
    private String botName;

    @Value("")
    private String token;
}
