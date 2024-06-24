package telegrambot.collector_bot.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import telegrambot.collector_bot.entity.DebtOwner;
import telegrambot.collector_bot.repository.entities.DebtOwnerRepository;

@Service
@AllArgsConstructor
public class DebtOwnerService {

    private final DebtOwnerRepository debtOwnerRepository;

    public DebtOwner findByUsername(String telegramUsername){
        try {
            return debtOwnerRepository.findByTelegramUsername(telegramUsername);
        } catch (Exception e){
            return null;
        }
    }

    public void addNewDebtOwner(DebtOwner debtOwner) {
        debtOwnerRepository.save(debtOwner);
    }
}
