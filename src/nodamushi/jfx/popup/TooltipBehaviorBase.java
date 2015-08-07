package nodamushi.jfx.popup;

import static java.lang.Math.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * 変更可能なDurationのプロパティとオフセットのプロパティを持つTooltipBehaviorの基本実装
 * @author nodamushi
 *
 * @param <P>
 */
public abstract class TooltipBehaviorBase<P extends PopupWindow>
extends SinglePopupBehavior<P>{


  @Override
  protected void show(final P p ,final Node hover ,double x ,double y){
    //あんまり意味は分かってないけど
    //とりあえずjavafx.scene.control.Tooltipからほぼ引用

    final NodeOrientation nodeOrientation = hover.getEffectiveNodeOrientation();
    p.getScene().setNodeOrientation(nodeOrientation);
    if (nodeOrientation == NodeOrientation.RIGHT_TO_LEFT) {
        x -= p.getWidth();
    }
    final Window owner = getWindow(hover);
    final double ox =getXOffset();
    final double oy = getYOffset();
    p.show(owner, floor(x+ox), floor(y+oy));

    if ((y+TOOLTIP_YOFFSET) > p.getAnchorY()) {
        p.hide();
        y -= p.getHeight();
        p.show(owner, floor(x+ox), floor(y));
    }
  }



  //-------------------------------------------
  //    Properties
  //-------------------------------------------


  /**
   * ポップアップさせるまでの時間
   * @return
   */
  public final ObjectProperty<Duration> openDurationProperty(){
    if (openDurationProperty == null) {
      openDurationProperty = new SimpleObjectProperty<>(this, "openDuration", DEFAULT_OPEN_DURATION);
    }
    return openDurationProperty;
  }

  public final Duration getOpenDuration(){
    return openDurationProperty == null ? DEFAULT_OPEN_DURATION : openDurationProperty.get();
  }

  public final void setOpenDuration(final Duration value){
    openDurationProperty().set(value);
  }

  private ObjectProperty<Duration> openDurationProperty;



  /**
   * ポップアップ後、ウィンドウが表示になっている時間
   * @return
   */
  public final ObjectProperty<Duration> hideDurationProperty(){
    if (hideDurationProperty == null) {
      hideDurationProperty = new SimpleObjectProperty<>(this, "hideDuration", DEFAULT_HIDE_DURATION);
    }
    return hideDurationProperty;
  }

  public final Duration getHideDuration(){
    return hideDurationProperty == null ? DEFAULT_HIDE_DURATION : hideDurationProperty.get();
  }

  public final void setHideDuration(final Duration value){
    hideDurationProperty().set(value);
  }

  private ObjectProperty<Duration> hideDurationProperty;


  /**
   * ポップアップが表示テイルときに、ポップアップが表示の原因となったNodeからマウスが放れた後に、
   * ウィンドウが表示になっている時間
   * @return
   */
  public final ObjectProperty<Duration> leftDurationProperty(){
    if (leftDurationProperty == null) {
      leftDurationProperty = new SimpleObjectProperty<>(this, "leftDuration", DEFAULT_LEFT_DURATION);
    }
    return leftDurationProperty;
  }

  public final Duration getLeftDuration(){
    return leftDurationProperty == null ? DEFAULT_LEFT_DURATION : leftDurationProperty.get();
  }

  public final void setLeftDuration(final Duration value){
    leftDurationProperty().set(value);
  }

  private ObjectProperty<Duration> leftDurationProperty;



  /**
   * 表示時のオフセット
   * @return
   */
  public final DoubleProperty xOffsetProperty(){
    if (xOffsetProperty == null) {
      xOffsetProperty = new SimpleDoubleProperty(this, "xOffset", TOOLTIP_XOFFSET);
    }
    return xOffsetProperty;
  }

  public final double getXOffset(){
    return xOffsetProperty == null ? TOOLTIP_XOFFSET : xOffsetProperty.get();
  }

  public final void setXOffset(final double value){
    xOffsetProperty().set(value);
  }

  private DoubleProperty xOffsetProperty;


  /**
   * 表示時のオフセット
   * @return
   */
  public final DoubleProperty yOffsetProperty(){
    if (yOffsetProperty == null) {
      yOffsetProperty = new SimpleDoubleProperty(this, "yOffset", TOOLTIP_YOFFSET);
    }
    return yOffsetProperty;
  }

  public final double getYOffset(){
    return yOffsetProperty == null ? TOOLTIP_YOFFSET : yOffsetProperty.get();
  }

  public final void setYOffset(final double value){
    yOffsetProperty().set(value);
  }

  private DoubleProperty yOffsetProperty;

  protected static final Duration
  DEFAULT_OPEN_DURATION=new Duration(1000),
  DEFAULT_HIDE_DURATION=new Duration(5000),
  DEFAULT_LEFT_DURATION=new Duration(200);
  protected static final double TOOLTIP_XOFFSET = 10;
  protected static final double TOOLTIP_YOFFSET = 7;
}
