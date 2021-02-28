package dev.keebler408.bt.commands;

import dev.keebler408.bt.BazaarTools;
import dev.keebler408.bt.utilities.Defaults;
import dev.keebler408.bt.utilities.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class BazaarToolsCommand extends CommandBase {

  @Override
  public List<String> getCommandAliases() {
    return new ArrayList<String>() {
      {
        add("bn");
      }
    };
  }

  @Override
  public String getCommandName() {
    return "bazaartools";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "/bazaartools [subcommand]";
  }

  @Override
  public void processCommand(ICommandSender ics, String[] args) {
    if (ics instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) ics;
      if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
        if (!BazaarTools.apiKey.equals("") || BazaarTools.apiKeyDisabled) {
          BazaarTools.orders = new JSONArray();
          BazaarTools.activeBazaar ^= true;
          player.addChatMessage(new ChatComponentText(
              BazaarTools.prefix + EnumChatFormatting.RED + "The mod has been toggled "
                  + EnumChatFormatting.DARK_RED + (BazaarTools.activeBazaar ? "on" : "off")));
        } else {
          player.addChatMessage(new ChatComponentText(
              BazaarTools.prefix + EnumChatFormatting.RED
                  + "Run /bn api (key) to set your api key. Do /api if you need to get your api key"));
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("api")) {
        if (args.length == 2) {
          BazaarTools.apiKey = args[1];
          try {
            if (Utils.validateApiKey()) {
              player.addChatMessage(new ChatComponentText(
                  BazaarTools.prefix + EnumChatFormatting.RED
                      + "Your api key has been set."));
              BazaarTools.apiKey = "";
              BazaarTools.validApiKey = true;
              BazaarTools.activeBazaar = true;
            } else {
              player.addChatMessage(new ChatComponentText(
                  BazaarTools.prefix + EnumChatFormatting.RED
                      + "Your api key is invalid. Please run /api new to get a fresh api key & use that in /bn api (key)"));
              BazaarTools.validApiKey = false;
            }
          } catch (IOException e) {
            player.addChatMessage(new ChatComponentText(
                BazaarTools.prefix + EnumChatFormatting.RED
                    + "An error occurred when trying to set your api key. Please re-run the command to try again."));
            BazaarTools.validApiKey = false;
            e.printStackTrace();
          }
        } else {
          player.addChatMessage(new ChatComponentText(
              BazaarTools.prefix + EnumChatFormatting.RED
                  + "Run /bn api (key) to set your api key. Do /api if you need to get your api key."));
          BazaarTools.validApiKey = false;
        }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("dump")) {
        System.out.println(BazaarTools.orders);
        player.addChatMessage(new ChatComponentText(BazaarTools.prefix + EnumChatFormatting.RED
            + "Orders dumped to the log file"));
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("reset")) {
        if (args.length == 1) {
          BazaarTools.resetMod();
          player.addChatMessage(new ChatComponentText(BazaarTools.prefix + EnumChatFormatting.RED
              + "All module locations have been reset and the order list has been emptied"));
        } else if (args.length == 2 && args[1].equalsIgnoreCase("orders")) {
          BazaarTools.orders = Defaults.DEFAULT_ORDERS_LAYOUT();
          player.addChatMessage(new ChatComponentText(BazaarTools.prefix + EnumChatFormatting.RED
              + "Your orders have been cleared"));
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("find")) {
        if (args.length == 1) {
          player.addChatMessage(new ChatComponentText(BazaarTools.prefix + EnumChatFormatting.RED
              + "Use the following format: /bn find (item)"));
        } else {
          String item = String.join(" ", args).substring(5).toLowerCase();
          if (BazaarTools.bazaarCache.has(item)) {
            JSONObject data = BazaarTools.bazaarCache.getJSONObject(item);
            player.addChatMessage(new ChatComponentText(BazaarTools.prefix + "\n" +
                EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + WordUtils.capitalize(item)
                + "\n"
                + EnumChatFormatting.DARK_RED + "Buy Order: " + EnumChatFormatting.RED +
                BazaarTools.df.format(data.getDouble("buyOrderPrice")) + "\n"
                + EnumChatFormatting.DARK_RED + "Sell Offer: "
                + EnumChatFormatting.RED +
                BazaarTools.df.format(data.getDouble("sellOfferPrice")) + "\n"
                + EnumChatFormatting.DARK_RED
                + "Estimated Profit: " + EnumChatFormatting.RED +
                BazaarTools.df.format(data.getDouble("profitFlowPerMinute")) + "\n"
                + BazaarTools.prefix
            ));
          } else if (BazaarTools.bazaarCache.length() == 0) {
            player.addChatMessage(new ChatComponentText(
                BazaarTools.prefix + EnumChatFormatting.RED
                    + "Please wait a moment for the mod to get bazaar information"));
          } else {
            player.addChatMessage(new ChatComponentText(
                BazaarTools.prefix + EnumChatFormatting.RED
                    + "Please provide a valid item to find."));
          }
        }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("__force")) {
        BazaarTools.forceRender ^= true;
        player.addChatMessage(new ChatComponentText(BazaarTools.prefix + EnumChatFormatting.RED
            + "This command is intended for testing purposes only, use it at your own peril. Forced rendering has been turned "
            + EnumChatFormatting.DARK_RED + (BazaarTools.forceRender ? "on" : "off")));
      } else if (args.length > 0) {
        player.addChatMessage(new ChatComponentText(BazaarTools.prefix + EnumChatFormatting.RED
            + "The command you just tried to do doesn't exist. Do /bn"));
      } else {
        player.addChatMessage(new ChatComponentText(BazaarTools.prefix + "\n" +
            EnumChatFormatting.RED + "/bn dump\n" + EnumChatFormatting.RED + "/bn reset orders\n"
            + EnumChatFormatting.RED + "/bn api (key)\n\n" + EnumChatFormatting.RED + "/bn toggle\n"
            + EnumChatFormatting.RED + "/bn find (item)\n"
            + BazaarTools.prefix
        ));
      }
    }
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }
}