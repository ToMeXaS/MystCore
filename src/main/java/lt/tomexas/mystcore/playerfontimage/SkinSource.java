package lt.tomexas.mystcore.playerfontimage;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.Objects;

/**
 * Abstract class to manage SkinSources
 */
public abstract class SkinSource {

    private final SkinSourceEnum skinSource;

    private final boolean hasUsernameSupport;

    private final boolean useUUIDWhenRetrieve;

    /**
     * Create a new SkinSource.
     *
     * @param skinSource The SkinSource.
     * @param hasUsernameSupport If it has the support of requesting the player's head by name.
     * @param useUUIDWhenRetrieve If it uses the UUID to request the head.
     */
    public SkinSource(SkinSourceEnum skinSource, boolean hasUsernameSupport, boolean useUUIDWhenRetrieve) {
        this.skinSource = skinSource;
        this.hasUsernameSupport = hasUsernameSupport;
        this.useUUIDWhenRetrieve = useUUIDWhenRetrieve;
    }


    /**
     * Create a new SkinSource.
     * Put {@code useUUIDWhenRetrieve} to {@code true}.
     *
     * @param skinSource The SkinSource.
     * @param hasUsernameSupport If it has the support of requesting the player's head by name.
     */
    public SkinSource(SkinSourceEnum skinSource, boolean hasUsernameSupport) {
        this.skinSource = skinSource;
        this.hasUsernameSupport = hasUsernameSupport;
        this.useUUIDWhenRetrieve = true;
    }


    /**
     * Retrieves a 8x8 grid of pixels representing a players head, using the player's UUID and specified options.
     *
     * @param player     The Player object representing the player whose head is to be retrieved.
     * @param overlay    A boolean value indicating whether to apply overlay on the players head.
     *                   Supported sources include MOJANG, MINOTAR, and CRAFATAR.
     * @return           An array of BaseComponents representing the player's head.
     *                   Each BaseComponent represents a single pixel, forming a 8x8 grid of pixels.
     */
    abstract public BaseComponent[] getHead(OfflinePlayer player, boolean overlay);

    abstract public BaseComponent[] getBust(OfflinePlayer player, boolean overlay);


    /**
     * After obtaining the 8x8 grid in hex form, transform it into BaseComponent[].
     *
     * @param hexColors The 8x8 grid in hex form.
     * @return The 8x8 grid in BaseComponent[].
     */
    public BaseComponent[] toComponent(String[] hexColors, int size, String font) {
        int expectedLength = size * size;

        // Validate the retrieved colors array
        if (hexColors == null || hexColors.length < expectedLength) {
            throw new IllegalArgumentException("Hex colors must have at least " + expectedLength + " elements.");
        }

        TextComponent[][] components = new TextComponent[size][size];

        for (int i = 0; i < expectedLength; i++) {
            int row = i / size;
            int col = i % size;
            char unicodeChar = (char) ('\uF000' + (i % size) + 1);
            char negativeSpacer = (expectedLength == 64) ? '\uF102' : '\uF103';
            TextComponent component = new TextComponent();

            // Determine the character and styling based on the position of the pixel within the 8x8 grid
            if (i % size == (size - 1) && i != (expectedLength - 1)) {
                component.setText(unicodeChar + Character.toString('\uF101'));
            } else if (i == (expectedLength - 1)) {
                component.setText(Character.toString(unicodeChar));
            } else {
                component.setText(unicodeChar + Character.toString(negativeSpacer));
            }

            // Set the color of the TextComponent based on the corresponding hexadecimal color
            component.setColor(ChatColor.of(hexColors[i]));
            components[row][col] = component;
        }

        TextComponent defaultFont = new TextComponent();
        defaultFont.setText("");
        defaultFont.setFont(font);

        // Construct the array of BaseComponents representing the player's head by appending the TextComponents

        return new ComponentBuilder()
                .append(Arrays.stream(components)
                        .flatMap(Arrays::stream)
                        .peek(textComponent -> {
                            if (textComponent != null)
                                textComponent.setFont(font);
                        })
                        .filter(Objects::nonNull)
                        .toArray(TextComponent[]::new))
                .append(defaultFont)
                .create(); // Return the array of BaseComponents representing the players head
    }

    public SkinSourceEnum getSkinSource() {
        return skinSource;
    }

    public boolean hasUsernameSupport() {
        return hasUsernameSupport;
    }

    public boolean useUUIDWhenRetrieve() {
        return useUUIDWhenRetrieve;
    }
}
