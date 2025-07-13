package lt.tomexas.mystcore.submodules.stats.stamina.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaminaConfig {
    private boolean sprintEnabled = true;
    private double sprintCost = 5;
    private boolean blockBreakEnabled = true;
    private double blockBreakCost = 1;
    private boolean attackEnabled = true;
    private double attackCost = 2;
}
