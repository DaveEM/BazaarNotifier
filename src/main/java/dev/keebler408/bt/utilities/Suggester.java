package dev.keebler408.bt.utilities;

import dev.keebler408.bt.BazaarTools;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public class Suggester {

  public static void basic() {
    try {
      if (BazaarTools.validApiKey || BazaarTools.apiKeyDisabled) {
        JSONObject bazaarData = BazaarTools.bazaarDataRaw;
        Iterator<String> bazaarKeys = bazaarData.keys();
        JSONArray bazaarDataFormatted = new JSONArray();

        while (bazaarKeys.hasNext()) {
          String key = bazaarKeys.next();
          JSONObject product = bazaarData.getJSONObject(key);

          if (!BazaarTools.bazaarConversions.has(key)) {
            BazaarTools.bazaarConversions.put(key, key);
            BazaarTools.bazaarConversionsReversed.put(key, key);
          }
          JSONObject currentProduct = new JSONObject()
              .put("productId", BazaarTools.bazaarConversions.getString(key))
              .put("sellOfferPrice",
                  (product.getJSONArray("buy_summary").length() > 0
                      && product.getJSONArray("sell_summary").length() > 0) ?
                      product.getJSONArray("buy_summary").getJSONObject(0)
                          .getDouble("pricePerUnit") : 0)
              .put("buyOrderPrice",
                  (product.getJSONArray("sell_summary").length() > 0
                      && product.getJSONArray("buy_summary").length() > 0) ?
                      product.getJSONArray("sell_summary").getJSONObject(0)
                          .getDouble("pricePerUnit") : 0)
              .put("sellCount",
                  product.getJSONObject("quick_status")
                      .getLong("buyMovingWeek"))
              .put("buyCount",
                  product.getJSONObject("quick_status")
                      .getLong("sellMovingWeek"));

          double diff = currentProduct.getDouble("sellOfferPrice") * .99d - currentProduct
              .getDouble("buyOrderPrice");
          double profitFlowPerMinute =
              (currentProduct.getLong("sellCount") + currentProduct.getLong("buyCount") == 0) ? 0 :
                  ((currentProduct.getLong("sellCount") * currentProduct.getLong("buyCount")) / (
                      10080d
                          * (currentProduct.getLong("sellCount") + currentProduct
                          .getLong("buyCount"))))
                      * diff;
          bazaarDataFormatted.put(currentProduct.put("profitFlowPerMinute", profitFlowPerMinute));
        }

        bazaarDataFormatted = Utils.sortJSONArray(bazaarDataFormatted, "profitFlowPerMinute");

        JSONObject bazaarCache = new JSONObject();
        bazaarDataFormatted.forEach((data) -> {
          JSONObject jsonData = (JSONObject) data;
          bazaarCache.put(jsonData.getString("productId").toLowerCase(), data);
        });

        BazaarTools.bazaarCache = bazaarCache;
        BazaarTools.bazaarDataFormatted = bazaarDataFormatted;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
