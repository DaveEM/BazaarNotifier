package dev.keebler408.bt.modules;

public enum ModuleName {
  SUGGESTION, BANK, NOTIFICATION;

  public Module returnDefaultModule() {
    switch (this) {
      case SUGGESTION:
        return new SuggestionModule();
      case BANK:
        return new BankModule();
      case NOTIFICATION:
        return new NotificationModule();
    }
    return null;
  }
}
