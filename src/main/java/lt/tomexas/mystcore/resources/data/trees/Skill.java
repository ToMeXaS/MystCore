package lt.tomexas.mystcore.resources.data.trees;

public record Skill(int level, double experience, double health, double stamina) {

    // Serialize the Skill object to a string
    public String serialize() {
        return level + "/" + experience + "/" + health + "/" + stamina;
    }

    // Deserialize a string back into a Skill object
    public static Skill deserialize(String serializedSkill) {
        if (serializedSkill == null || serializedSkill.isEmpty()) {
            throw new IllegalArgumentException("Invalid serialized skill format");
        }

        String[] parts = serializedSkill.split("/");
        // Check if the serialized string has the correct number of parts
        if (parts.length != 4) {
            throw new IllegalArgumentException("Serialized skill must have 4 parts");
        }

        int level = Integer.parseInt(parts[0]);
        double experience = Double.parseDouble(parts[1]);
        double health = Double.parseDouble(parts[2]);
        double stamina = Double.parseDouble(parts[3]);

        return new Skill(level, experience, health, stamina);
    }
}
