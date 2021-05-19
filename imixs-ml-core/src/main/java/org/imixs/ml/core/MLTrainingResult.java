package org.imixs.ml.core;

/**
 * Describes a training result object
 * <ul>
 * <li>qualityLevel - computed by the TrainingDataBuilder
 * <li>data - result form the ML Rest Service
 * </ul>
 * 
 * @author rsoika
 *
 */
public class MLTrainingResult {

    int qualityLevel;
    String data;

    public MLTrainingResult(int qualityLevel, String data) {
        super();
        this.qualityLevel = qualityLevel;
        this.data = data;
    }

    public int getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(int qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
