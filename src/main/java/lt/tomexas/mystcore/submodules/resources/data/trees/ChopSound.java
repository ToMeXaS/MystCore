package lt.tomexas.mystcore.submodules.resources.data.trees;

public record ChopSound(String type, float volume, float pitch) {

    public String serialize() {
        return type + "/" + volume + "/" + pitch;
    }

    public static ChopSound deserialize(String serializedChopSound) {
        if (serializedChopSound == null || serializedChopSound.isEmpty()) {
            throw new IllegalArgumentException("Invalid serialized skill format");
        }

        String[] parts = serializedChopSound.split("/");
        // Check if the serialized string has the correct number of parts
        if (parts.length != 3) {
            throw new IllegalArgumentException("Serialized skill must have 3 parts");
        }

        String type = parts[0];
        float volume = Float.parseFloat(parts[1]);
        float pitch = Float.parseFloat(parts[2]);

        return new ChopSound(type, volume, pitch);
    }
}
