package lt.tomexas.mystcore.data.enums;

import java.util.regex.Pattern;

public enum Permissions {
    BYPASS_STAMINA("mystcore.bypass.stamina"),
    STAMINA_SPRINT_DRAIN("mystcore.stamina.sprint.drain\\.(\\d+(?:\\.\\d+)?)"),
    STAMINA_ATTACK_DRAIN("mystcore.stamina.attack.drain\\.(\\d+(?:\\.\\d+)?)"),
    STAMINA_BLOCKBREAK_DRAIN("mystcore.stamina.blockbreak.drain\\.(\\d+(?:\\.\\d+)?)");

    private final String perm;
    private final Pattern pattern;

    Permissions(String perm) {
        this.perm = perm;
        this.pattern = Pattern.compile(perm);
    }

    public String asString() {
        return perm;
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }

    public Pattern asPattern() {
        return pattern;
    }
}
