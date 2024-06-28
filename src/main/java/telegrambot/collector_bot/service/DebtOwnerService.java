package telegrambot.collector_bot.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import telegrambot.collector_bot.entity.DebtOwner;
import telegrambot.collector_bot.repository.entities.DebtOwnerRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DebtOwnerService {

    private final DebtOwnerRepository debtOwnerRepository;

    public DebtOwner findByUsername(String telegramUsername){
        try {
            return debtOwnerRepository.findByUsername(telegramUsername);
        } catch (Exception e){
            return null;
        }
    }

    public void addNewDebtOwner(DebtOwner debtOwner) {
        debtOwnerRepository.save(debtOwner);
    }
    public long countAllUsers(){
        return debtOwnerRepository.count();
    }

    public List<DebtOwner> getAllDebtOwners() {
        return debtOwnerRepository.findAll();
    }
}
