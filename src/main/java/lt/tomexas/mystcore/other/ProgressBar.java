package lt.tomexas.mystcore.other;

public class ProgressBar {

    public static String createAndReturn(double progress, double maxProgress) {
        double percentage = progress / maxProgress * 100.0;
        StringBuilder progressBar = new StringBuilder();
        double percentPerChar = 100.0 / 15; // Adjusted for 15 characters
        char character = '▍';

        for (int i = 0; i < 15; ++i) {
            double progressPassBar = percentPerChar * (i + 1);
            if (percentage >= progressPassBar) {
                progressBar.append("§c").append(character);
            } else {
                progressBar.append("§7").append(character);
            }
        }

        // Build the final string
        return progressBar.toString();
    }
}
