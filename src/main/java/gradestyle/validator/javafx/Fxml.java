package gradestyle.validator.javafx;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class Fxml extends DefaultHandler {
  private String controller;

  private List<String> ids = new ArrayList<>();

  private List<String> actions = new ArrayList<>();

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (this.controller == null) {
      this.controller = attributes.getValue("fx:controller");
    }

    String id = attributes.getValue("fx:id");

    if (id != null) {
      ids.add(id);
    }

    String action = attributes.getValue("onAction");

    if (action != null) {
      actions.add(action.startsWith("#") ? action.substring(1) : action);
    }
  }

  public String getController() {
    return controller;
  }

  public List<String> getIds() {
    return ids;
  }

  public List<String> getActions() {
    return actions;
  }
}
