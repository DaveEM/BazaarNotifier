package dev.keebler408.bt.modules;

import dev.keebler408.bt.BazaarTools;
import dev.keebler408.bt.utilities.ColorUtils;
import dev.keebler408.bt.utilities.Defaults;
import dev.keebler408.bt.utilities.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

public class NotificationModule extends Module {

  public NotificationModule() {
    super();
  }

  public NotificationModule(JSONObject module) {
    super(module);
  }

  @Override
  protected void draw() {
    // add extra space after "Buy" so it lines up with sell

    List<LinkedHashMap<String, Color>> items = new ArrayList<>();

    if (BazaarTools.orders.length() != 0) {

      int size = Math.min(shift + 10, BazaarTools.orders.length());

      for (int i = shift; i < size; i++) {
        JSONObject currentOrder = BazaarTools.orders.getJSONObject(i);
        LinkedHashMap<String, Color> message = new LinkedHashMap<>();

        Color typeSpecificColor = currentOrder.getBoolean("goodOrder") ? new Color(0x55FF55)
            : currentOrder.getString("type").equals("buy") ? new Color(0xFF55FF)
                : new Color(0x55FFFF);

        String notification = currentOrder.getBoolean("goodOrder") ? "BEST" :
            currentOrder.getBoolean("matchedOrder") ? "MATCHED" : "OUTDATED";
        message.put(WordUtils.capitalizeFully(currentOrder.getString("type")), typeSpecificColor);
        message.put(" - ", new Color(0xAAAAAA));
        message.put(notification + " ", new Color(0xFFFF55));
        message.put("(", new Color(0xAAAAAA));
        message.put(BazaarTools.dfNoDecimal.format(currentOrder.getInt("startAmount")),
            typeSpecificColor);
        message.put("x ", new Color(0xAAAAAA));
        message.put(currentOrder.getString("product"), typeSpecificColor);
        message.put(", ", new Color(0xAAAAAA));
        message.put(BazaarTools.df.format(currentOrder.getDouble("pricePerUnit")),
            typeSpecificColor);
        message.put(")", new Color(0xAAAAAA));
        items.add(message);
      }

      int longestXString = ColorUtils.drawColorfulParagraph(items, x, y);
      boundsX = x + longestXString;
    } else {
      Utils.drawCenteredString("No orders found", x + 100,
          y + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 10 + 16) / 2, 0xAAAAAA, 1F);
      boundsX = x + 200;
    }
    boundsY = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 10 + 18;
  }

  @Override
  protected void reset() {
    x = Defaults.NOTIFICATION_MODULE_X;
    y = Defaults.NOTIFICATION_MODULE_Y;
  }

  @Override
  protected String name() {
    return ModuleName.NOTIFICATION.name();
  }


  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return BazaarTools.orders.length() - 10;
  }

}
