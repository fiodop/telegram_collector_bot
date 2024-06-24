package telegrambot.collector_bot.repository.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import telegrambot.collector_bot.entity.Debt;
import telegrambot.collector_bot.entity.DebtOwner;

import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Integer> {
    List<Debt> findAllByDebtOwner(DebtOwner debtOwner);
    void deleteByUsername(String username);
    Debt getDebtById(long debtId);
}
