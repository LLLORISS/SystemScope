package nm.sc.systemscope.adapters;

import com.google.gson.*;
import javafx.scene.chart.XYChart;

import java.lang.reflect.Type;

public class XYChartDataAdapter implements JsonSerializer<XYChart.Data<String, Number>>, JsonDeserializer<XYChart.Data<String, Number>> {
    @Override
    public JsonElement serialize(XYChart.Data<String, Number> data, Type typeOfSrc, JsonSerializationContext context){
        JsonObject obj = new JsonObject();
        obj.addProperty("x", data.getXValue());
        obj.addProperty("y", data.getYValue());
        return obj;
    }

    @Override
    public XYChart.Data<String, Number> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String xValue = jsonObject.get("x").getAsString();
        double yValue = jsonObject.get("y").getAsDouble();
        return new XYChart.Data<>(xValue, yValue);
    }
}
