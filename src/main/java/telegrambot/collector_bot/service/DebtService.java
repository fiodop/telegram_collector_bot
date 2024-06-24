package telegrambot.collector_bot.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import telegrambot.collector_bot.entity.Debt;
import telegrambot.collector_bot.entity.DebtOwner;
import telegrambot.collector_bot.repository.entities.DebtRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DebtService {
    DebtRepository debtRepository;
    public void addNewDebt(Debt debt) {
        debtRepository.save(debt);
    }

    public List<Debt> getAllDebts(DebtOwner debtOwner) {
         return debtRepository.findAllByDebtOwner(debtOwner);
    }

    public void deleteDebtById(long debtId) {
        debtRepository.deleteById((int) debtId);
    }

    public Debt getDebtById(long debtId){
        return debtRepository.getDebtById(debtId);

    }
}
