package lt.tomexas.mystcore.submodules.resources.data.trees;

public record Skill(String type, int level, double experience, double health, double stamina) {

    // Serialize the Skill object to a string
    public String serialize() {
        return type + "/" + level + "/" + experience + "/" + health + "/" + stamina;
    }

    // Deserialize a string back into a Skill object
    public static Skill deserialize(String serializedSkill) {
        if (serializedSkill == null || serializedSkill.isEmpty()) {
            throw new IllegalArgumentException("Invalid serialized skill format");
        }

        String[] parts = serializedSkill.split("/");
        // Check if the serialized string has the correct number of parts
        if (parts.length != 5) {
            throw new IllegalArgumentException("Serialized skill must have 5 parts");
        }

        String type = parts[0];
        int level = Integer.parseInt(parts[1]);
        double experience = Double.parseDouble(parts[2]);
        double health = Double.parseDouble(parts[3]);
        double stamina = Double.parseDouble(parts[4]);

        return new Skill(type, level, experience, health, stamina);
    }
}
