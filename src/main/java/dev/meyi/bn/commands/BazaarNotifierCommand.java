package dev.meyi.bn.commands;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class BazaarNotifierCommand extends CommandBase {

  @Override
  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }

  @Override
  public String getCommandName() {
    return "bazaarnotifier";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "/bazaarnotifier [subcommand]";
  }

  @Override
  public List<String> getCommandAliases() {
    return new ArrayList<String>() {
      {
        add("bn");
      }
    };
  }

  @Override
  public void processCommand(ICommandSender ics, String[] args) {
    if (ics instanceof EntityPlayer && args.length >= 1) {
      EntityPlayer player = (EntityPlayer) ics;
      String command = args[0].toLowerCase(Locale.ROOT);
      switch (command) {
        case "api":
          doApiCommand(player, args);
          break;
        case "discord":
          doDiscordCommand(player, args);
          break;
        case "dump":
          doDumpCommand(player, args);
          break;
        case "find":
          doFindCommand(player, args);
          break;
        case "reset":
          doResetCommand(player, args);
          break;
        case "toggle":
          doToggleCommand(player, args);
          break;
        case "__force":
          doForceRenderTestModeCommand(player, args);
          break;
        default:
          if (args.length > 0) {
            player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "The command you just tried to do doesn't exist. Do /bn"));
          } else {
            showUsage(player);
          }
      }

    }
  }

  private void showUsage(EntityPlayer player) {
    StringBuilder sb = new StringBuilder();
    sb.append(BazaarNotifier.prefix + "\n");
    sb.append(EnumChatFormatting.RED + "/bn dump\n");
    sb.append(EnumChatFormatting.RED + "/bn reset (orders)\n");
    sb.append(EnumChatFormatting.RED + "/bn api (key)\n\n");
    sb.append(EnumChatFormatting.RED + "/bn toggle\n");
    sb.append(EnumChatFormatting.RED + "/bn find (item)\n");
    sb.append(EnumChatFormatting.RED + "/bn discord");
    sb.append(BazaarNotifier.prefix);
    player.addChatMessage(new ChatComponentText(sb.toString()));
  }

  private void doDiscordCommand(EntityPlayer player, String[] args) {
    ChatComponentText discordLink = new ChatComponentText(
        EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD
            + "[DISCORD LINK]");
    discordLink
        .setChatStyle(discordLink.getChatStyle().setChatClickEvent(new ClickEvent(
            Action.OPEN_URL,
            "https://discord.com/invite/wjpJSVSwvD")));
    ChatComponentText supportLink = new ChatComponentText(
        EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD
            + "[DISCORD LINK]");
    supportLink
        .setChatStyle(supportLink.getChatStyle().setChatClickEvent(new ClickEvent(
            Action.OPEN_URL,
            "https://patreon.com/meyi")));

    player.addChatMessage(new ChatComponentText(
        BazaarNotifier.prefix + "\n" + EnumChatFormatting.GREEN + "Join the discord server: ")
        .appendSibling(discordLink).appendSibling(
            new ChatComponentText("\n" + EnumChatFormatting.GREEN + "If you want, you can support my work: ")
                .appendSibling(supportLink))
        .appendSibling(new ChatComponentText("\n" + BazaarNotifier.prefix)));
  }

  private void doForceRenderTestModeCommand(EntityPlayer player, String[] args) {
    BazaarNotifier.forceRenderTestMode ^= true;
    player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
        + "This command is intended for testing purposes only, use it at your own peril. Forced rendering has been turned "
        + EnumChatFormatting.DARK_RED + (BazaarNotifier.forceRenderTestMode ? "on" : "off")));
  }

  private void doFindCommand(EntityPlayer player, String[] args) {
    if (args.length == 1) {
      player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
          + "Use the following format: /bn find (item)"));
      return;
    }

    if (BazaarNotifier.bazaarCache.length() == 0) {
      player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Please wait a moment for the mod to get bazaar information"));
      return;
    }

    String item = String.join(" ", args).substring(5).toLowerCase();
    if (BazaarNotifier.bazaarCache.has(item)) {
      JSONObject data = BazaarNotifier.bazaarCache.getJSONObject(item);
      StringBuilder sb = new StringBuilder();
      sb.append(BazaarNotifier.prefix + "\n");
      sb.append(EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD.toString() +
              WordUtils.capitalize(item) + "\n");
      sb.append(EnumChatFormatting.DARK_RED + "Buy Order: " + EnumChatFormatting.RED +
              BazaarNotifier.df.format(data.getDouble("buyOrderPrice")) + "\n");
      sb.append(EnumChatFormatting.DARK_RED + "Sell Offer: " + EnumChatFormatting.RED +
              BazaarNotifier.df.format(data.getDouble("sellOfferPrice")) + "\n");
      sb.append(EnumChatFormatting.DARK_RED + "Estimated Profit: " + EnumChatFormatting.RED +
              BazaarNotifier.df.format(data.getDouble("profitFlowPerMinute")) + "\n");
      sb.append(BazaarNotifier.prefix);
      player.addChatMessage(new ChatComponentText(sb.toString()));
    } else {
      player.addChatMessage(new ChatComponentText(
          BazaarNotifier.prefix + EnumChatFormatting.RED
              + "Please provide a valid item to find"));
    }
  }

  private void doResetCommand(EntityPlayer player, String[] args) {
    if (args.length == 1) {
      BazaarNotifier.resetMod();
      player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
          + "All module locations have been reset and the order list has been emptied"));
    } else if (args.length == 2 && args[1].equalsIgnoreCase("orders")) {
      BazaarNotifier.orders = Defaults.DEFAULT_ORDERS_LAYOUT();
      player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
          + "Your orders have been cleared"));
    }
  }

  private void doDumpCommand(EntityPlayer player, String[] args) {
    if (args.length > 1) {
      player.addChatMessage(new ChatComponentText("The dump command does not have parameters"));
      return;
    }

    // TODO: DaveEM - understand why println was here and if it can be deleted
    //System.out.println(BazaarNotifier.orders);
    player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
        + "Orders dumped to the log file"));
  }

  private void doApiCommand(EntityPlayer player, String[] args)
  {
    if (args.length != 2) {
      player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Run /bn api (key) to set your api key. Do /api if you need to get your api key."));
      BazaarNotifier.validApiKey = false;
      return;
    }

    BazaarNotifier.apiKey = args[1];
    try {
      if (Utils.validateApiKey()) {
        player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "Your api key has been set."));
        BazaarNotifier.apiKey = "";
        BazaarNotifier.validApiKey = true;
        BazaarNotifier.activeBazaar = true;
      } else {
        player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "Your api key is invalid. Please run /api new to get a fresh api key & use that in /bn api (key)"));
        BazaarNotifier.validApiKey = false;
      }
    } catch (IOException e) {
      player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "An error occurred when trying to set your api key. Please re-run the command to try again."));
      BazaarNotifier.validApiKey = false;
      e.printStackTrace();
    }
  }

  private void doToggleCommand(EntityPlayer player, String[] args)
  {
    if (args.length > 1) {
      player.addChatMessage(new ChatComponentText("The toggle command does not have parameters"));
      return;
    }

    if (BazaarNotifier.apiKey.equals("") && !BazaarNotifier.apiKeyDisabled) {
      player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Run /bn api (key) to set your api key. Do /api if you need to get your api key"));
      return;
    }

    BazaarNotifier.orders = new JSONArray();
    BazaarNotifier.activeBazaar ^= true;
    player.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + EnumChatFormatting.RED + "The mod has been toggled "
                    + EnumChatFormatting.DARK_RED + (BazaarNotifier.activeBazaar ? "on" : "off")));
  }
}