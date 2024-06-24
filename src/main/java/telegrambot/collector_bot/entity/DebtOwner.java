package telegrambot.collector_bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class DebtOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    private String telegramUsername;
    private Boolean isEnglish;

    @OneToMany(mappedBy = "debtOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Debt> debts;

    public DebtOwner(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }
}