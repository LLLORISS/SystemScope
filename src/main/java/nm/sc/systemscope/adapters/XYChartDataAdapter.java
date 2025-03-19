package nm.sc.systemscope.adapters;

import com.google.gson.*;
import javafx.scene.chart.XYChart;

import java.lang.reflect.Type;

/**
 * Adapter for serializing and deserializing {@link XYChart.Data} objects to JSON format.
 * Used with the Gson library to convert data between {@link XYChart.Data} and JSON.
 */
public class XYChartDataAdapter implements JsonSerializer<XYChart.Data<String, Number>>, JsonDeserializer<XYChart.Data<String, Number>> {
    /**
     * Serializes the {@link XYChart.Data} object to JSON.

     * @param data The {@link XYChart.Data} object to serialize.
     * @param typeOfSrc Serialization source type.
     * @param context Gson serialization context.
     * @return JSON object containing X and Y data.
     */
    @Override
    public JsonElement serialize(XYChart.Data<String, Number> data, Type typeOfSrc, JsonSerializationContext context){
        JsonObject obj = new JsonObject();
        obj.addProperty("x", data.getXValue());
        obj.addProperty("y", data.getYValue());
        return obj;
    }

    /**
     * Deserializes the JSON object to {@link XYChart.Data}.
     *
     * @param json JSON element containing X and Y data.
     * @param typeOfT The type to deserialize to.
     * @param context Gson deserialization context.
     * @return The {@link XYChart.Data} object with the extracted X and Y values.
     * @throws JsonParseException If the JSON has the wrong format or the required data is missing.
     */
    @Override
    public XYChart.Data<String, Number> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String xValue = jsonObject.get("x").getAsString();
        double yValue = jsonObject.get("y").getAsDouble();
        return new XYChart.Data<>(xValue, yValue);
    }
}
