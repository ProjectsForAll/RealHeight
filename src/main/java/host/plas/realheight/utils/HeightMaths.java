package host.plas.realheight.utils;

public class HeightMaths {
    public static final double MAX_HEIGHT = 9.0; // 9 FT
    public static final double MIN_HEIGHT = 1.0;  // 1 FT

    public static final double M_TO_CM = 100.0; // Meters to Centimeters
    public static final double CM_TO_M = 0.01; // Centimeters to Meters

    public static final double M_TO_FT = 3.28084; // Meters to Feet
    public static final double FT_TO_M = 0.3048; // Feet to M
    public static final double CM_TO_FT = (CM_TO_M * M_TO_FT); // Centimeters to Feet
    public static final double FT_TO_CM = (FT_TO_M * M_TO_CM); // Feet to Centimeters

    public static final double BLOCK_HEIGHT_IN_M = 1.0; // Block height in meters
    public static final double DEFAULT_PLAYER_HEIGHT_IN_BLOCKS = 1.8; // Default player height in blocks
    public static final double DEFAULT_PLAYER_HEIGHT_IN_M = (DEFAULT_PLAYER_HEIGHT_IN_BLOCKS * BLOCK_HEIGHT_IN_M); // Default player height in meters
    public static final double DEFAULT_PLAYER_HEIGHT_IN_CM = (DEFAULT_PLAYER_HEIGHT_IN_M * M_TO_CM); // Default player height in centimeters
    public static final double DEFAULT_PLAYER_HEIGHT_IN_FT = (DEFAULT_PLAYER_HEIGHT_IN_M * M_TO_FT); // Default player height in feet

    public static final double DEFAULT_SCALE = 1.0; // Default scale
    public static final double BLOCK_TO_DEFAULT_SCALE_RATIO = (DEFAULT_SCALE / DEFAULT_PLAYER_HEIGHT_IN_BLOCKS); // Block to scale ratio
    public static final double M_TO_DEFAULT_SCALE_RATIO = (DEFAULT_SCALE / DEFAULT_PLAYER_HEIGHT_IN_M); // Meters to scale ratio
    public static final double CM_TO_DEFAULT_SCALE_RATIO = (DEFAULT_SCALE / DEFAULT_PLAYER_HEIGHT_IN_CM); // Centimeters to scale ratio
    public static final double FT_TO_DEFAULT_SCALE_RATIO = (DEFAULT_SCALE / DEFAULT_PLAYER_HEIGHT_IN_FT); // Feet to scale ratio

    /**
     * Returns the a clamped (within min and max) height in cm.
     * @param cm The height in cm to clamp.
     * @return The clamped height in cm.
     */
    public static double clampHeightInCm(double cm) {
        double minCm = MIN_HEIGHT * FT_TO_CM;
        double maxCm = MAX_HEIGHT * FT_TO_CM;

        if (cm < minCm) {
            return minCm;
        } else if (cm > maxCm) {
            return maxCm;
        } else {
            return cm;
        }
    }

    /**
     * Returns a clamped (within min and max) height in ft.
     * @param ft The height in ft to clamp.
     * @return The clamped height in ft.
     */
    public static double clampHeightInFt(double ft) {
        double ftAsCm = ft * FT_TO_CM;
        double clampedCm = clampHeightInCm(ftAsCm);
        return clampedCm * CM_TO_FT;
    }

    /**
     * Returns the scale of the correct height for the player to be set to.
     *
     * For example, if the player is 1.8m tall (1.8 blocks), the scale will be 1.0, and thus this will return 1.0.
     * @param cm The height in centimeters.
     * @return The scale.
     */
    public static double getScaleOfCm(double cm) {
        double clampedCm = clampHeightInCm(cm);
        return clampedCm * CM_TO_DEFAULT_SCALE_RATIO;
    }

    /**
     * Returns the scale of the correct height for the player to be set to.
     * @param ft The height in feet.
     * @return The scale.
     */
    public static double getScaleOfFt(double ft) {
        return getScaleOfCm(ft * FT_TO_CM);
    }

    /**
     * Returns the scale of the correct height for the player to be set to.
     * @param ft The feet portion of the height.
     * @param inches The inches portion of the height.
     * @return The scale.
     */
    public static double getScaleOfFtAndInches(double ft, double inches) {
        double totalFt = ft + (inches / 12.0);
        return getScaleOfFt(totalFt);
    }

    /**
     * Returns the height in cm for a given scale.
     * @param scale The scale.
     */
    public static double getCmOfScale(double scale) {
        double cm = scale / CM_TO_DEFAULT_SCALE_RATIO;

        // Truncate to 2 decimal places
        cm = Math.floor(cm * 100.0) / 100.0;

        return clampHeightInCm(cm);
    }

    /**
     * Returns the height in ft for a given scale.
     * @param scale The scale.
     */
    public static double getFtOfScale(double scale) {
        double cm = getCmOfScale(scale);
        double ft = cm * CM_TO_FT;

        // Truncate to 2 decimal places
        ft = Math.floor(ft * 100.0) / 100.0;

        return ft;
    }
}