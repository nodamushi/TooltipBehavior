package nodamushi.jfx.popup;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
/**
 * com.sun.javafx.scene.control.skin.TooltipSkinのまんまコピペ<br/>
 * 著作権とかライセンスは大丈夫か？
 * @author nodamushi
 *
 */
public class NTooltipSkin implements Skin<NTooltip>{
  protected NTooltip tooltip;
  protected Label tipLabel;

  public NTooltipSkin(final NTooltip t){
    tooltip = t;
    tipLabel = new Label();
    tipLabel.contentDisplayProperty().bind(t.contentDisplayProperty());
    tipLabel.fontProperty().bind(t.fontProperty());
    tipLabel.graphicProperty().bind(t.graphicProperty());
    tipLabel.graphicTextGapProperty().bind(t.graphicTextGapProperty());
    tipLabel.textAlignmentProperty().bind(t.textAlignmentProperty());
    tipLabel.textOverrunProperty().bind(t.textOverrunProperty());
    tipLabel.textProperty().bind(t.textProperty());
    tipLabel.wrapTextProperty().bind(t.wrapTextProperty());
    tipLabel.minWidthProperty().bind(t.minWidthProperty());
    tipLabel.prefWidthProperty().bind(t.prefWidthProperty());
    tipLabel.maxWidthProperty().bind(t.maxWidthProperty());
    tipLabel.minHeightProperty().bind(t.minHeightProperty());
    tipLabel.prefHeightProperty().bind(t.prefHeightProperty());
    tipLabel.maxHeightProperty().bind(t.maxHeightProperty());


    tipLabel.getStyleClass().setAll(t.getStyleClass());
    tipLabel.setStyle(t.getStyle());
    tipLabel.setId(t.getId());
  }

  @Override
  public NTooltip getSkinnable(){
    return tooltip;
  }

  @Override
  public Node getNode(){
    return tipLabel;
  }

  @Override
  public void dispose(){
    tooltip = null;
    tipLabel = null;
  }

}
