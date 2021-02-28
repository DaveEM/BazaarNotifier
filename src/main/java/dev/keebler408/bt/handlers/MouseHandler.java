package dev.keebler408.bt.handlers;

import dev.keebler408.bt.BazaarTools;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MouseHandler {

  int tick = 0;
  boolean inPageFlip = false;

  @SubscribeEvent
  public void mouseActionCheck(TickEvent e) {
    if (e.phase == TickEvent.Phase.START) {
      if (BazaarTools.inBazaar) {
        BazaarTools.modules.movementCheck();
        if (tick == 8 && !inPageFlip) { // 2.5 times per second
          inPageFlip = true;
          BazaarTools.modules.pageFlipCheck();
          tick = 0;
          inPageFlip = false;
        }
        tick++;
      } else {
        tick = 0;
      }
    }
  }
}
