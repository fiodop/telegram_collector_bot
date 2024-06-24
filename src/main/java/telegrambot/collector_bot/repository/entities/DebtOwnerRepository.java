package telegrambot.collector_bot.repository.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import telegrambot.collector_bot.entity.DebtOwner;

public interface DebtOwnerRepository extends JpaRepository<DebtOwner, Integer> {
    DebtOwner findByTelegramUsername(String telegramUsername);

}
