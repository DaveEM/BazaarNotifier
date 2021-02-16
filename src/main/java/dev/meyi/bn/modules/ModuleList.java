package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.BazaarNotifierConfig;
import dev.meyi.bn.utilities.Utils;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ModuleList extends ArrayList<Module> {

  Module movingModule = null;

  public ModuleList(JSONObject config) {
    JSONArray modules = config.getJSONArray(BazaarNotifierConfig.MODULES_CONFIG_NAME);

    for (Object m : modules) {
      JSONObject module = (JSONObject) m;
      switch (ModuleName.valueOf(module.getString("name"))) {
        case SUGGESTION:
          add(new SuggestionModule(module));
          break;
        case BANK:
          add(new BankModule(module));
          break;
        case NOTIFICATION:
          add(new NotificationModule(module));
          break;
        default:
          throw new IllegalStateException(
              "Unexpected value: " + ModuleName.valueOf(module.getString("name")));
      }
    }
  }

  public void drawAllModules() {
    for (Module m : this) {
      m.draw();
    }
  }

  public void drawAllOutlines() {
    for (Module m : this) {
      m.drawBounds();
    }
  }

  /**
   * Checks if the left mouse button has been pressed within the bounds of any module and handles mouse
   * movement for that module until the left mouse button is no longer down.
   * NOTE: If the left mouse button is first pressed outside a module then moved into a module's bounds then
   *       future mouse movement will result in the movement of that module.
   */
  public void movementCheck() {
    if (Mouse.isButtonDown(0)) {
      if (movingModule == null) {
        for (Module m : this) {
          if (m.inMovementBox()) {
            m.needsToMove = true;
          }
        }
        for (Module m : this) {
          if (movingModule != null) {
            m.needsToMove = false;
          } else if (m.needsToMove) {
            movingModule = m;
          }
        }
      }
      if (movingModule != null) {
        movingModule.handleMovement();
      }
    } else {
      if (movingModule != null) {
        movingModule.needsToMove = false;
        movingModule.moving = false;
      }
      movingModule = null;
    }
  }

  public void pageFlipCheck() {
    if (Mouse.isButtonDown(1) && !Mouse.isButtonDown(0)) {
      for (Module m : this) {
        if (m.inMovementBox() && m.getMaxShift() > 0) {
          if (Keyboard.isKeyDown(17) && !Keyboard.isKeyDown(31)) {
            if (m.shift != 0) {
              m.shift--;
            }
          } else if (Keyboard.isKeyDown(31) && !Keyboard.isKeyDown(17)) {
            if (m.shift != m.getMaxShift()) {
              m.shift++;
            }
          }
          break;
        }
      }
    }
  }

  public void resetAll() {
    for (Module m : this) {
      m.reset();
    }
  }

  public JSONArray getCurrentModuleConfigs() {
    JSONArray moduleConfigs = new JSONArray();
    for (Module m : this) {
      moduleConfigs.put(m.getCurrentModuleConfig());
    }

    return moduleConfigs;
  }
}
