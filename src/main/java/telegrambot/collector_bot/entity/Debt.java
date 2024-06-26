package telegrambot.collector_bot.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity(name = "Debts")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    private String username;
    private int debtAmount;
    private String currency;
    private String description;
    private boolean notify;
    private ZonedDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private DebtOwner debtOwner;

    public Debt(String username, int debtAmount, String currency) {
        if (username.isEmpty()) {
            throw new IllegalStateException("username не может быть пустым");
        }

        if (debtAmount <= 0) {
            throw new IllegalStateException("Долг не может быть меньше 1 рубля");
        }

        this.username = username;
        this.debtAmount = debtAmount;
        this.currency = currency;
        this.dateTime = ZonedDateTime.now();
    }
}