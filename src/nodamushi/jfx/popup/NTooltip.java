package nodamushi.jfx.popup;
import static java.lang.Math.*;
import static javafx.css.StyleConverter.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;
import javafx.util.Duration;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.EnumConverter;
import com.sun.javafx.css.converters.SizeConverter;
import com.sun.javafx.css.converters.StringConverter;

/**
 * ついでに起動時間とかCSSで指定できるTooltipもどきを作ってみた。<br/>
 * 著作権とかライセンスとかやばそう。
 * @author nodamushi
 *
 */
public class NTooltip extends PopupControl{

  private static final double TOOLTIP_DEFAULT_XOFFSET=10;
  private static final double TOOLTIP_DEFAULT_YOFFSET=7;


  public NTooltip(){this(null);}

  public NTooltip(final String text){
    super();
    if(text!=null) {
      setText(text);
    }
    final Parent rootNode = bridge.getParent();
    bridge = new CSSBridge();
    if (rootNode instanceof Group) {
      ((Group) rootNode).getChildren().setAll(bridge);
    }else if (rootNode instanceof Pane) {
      ((Pane) rootNode).getChildren().setAll(bridge);
    }else{
      throw new IllegalStateException(
          "The content of the Popup can't be accessed");
    }
    getStyleClass().setAll("tooltip");
  }


  @Override
  protected Skin<?> createDefaultSkin(){
    return new NTooltipSkin(this);
  }

  public static void install(final Node node,final NTooltip t){
    TOOLTIP_BEHAVIOR.install(node, t);
  }

  public static void uninstall(final Node node){
    TOOLTIP_BEHAVIOR.uninstall(node);
  }

  //---------------------------------------------------------
  //       Properties                                           ------
  //------------------------------------------------------------------------


  /**
   * 表示まで待機する時間
   * @return
   */
  public final ObjectProperty<Duration> openDurationProperty(){
    return openDurationProperty;
  }

  public final Duration getOpenDuration(){
    return openDurationProperty.get();
  }

  public final void setOpenDuration(final Duration value){
    openDurationProperty().set(value);
  }

  private final StyleableObjectProperty<Duration> openDurationProperty =
      new SimpleStyleableObjectProperty<>(OPEN_TIME_CMD, this, "openDuration");


  /**
   * 表示時間
   * @return
   */
  public final ObjectProperty<Duration> hideDurationProperty(){
    return hideDurationProperty;
  }

  public final Duration getHideDuration(){
    return hideDurationProperty.get();
  }

  public final void setHideDuration(final Duration value){
    hideDurationProperty().set(value);
  }

  private final StyleableObjectProperty<Duration> hideDurationProperty =
      new SimpleStyleableObjectProperty<>(HIDE_TIME_CMD, this, "hideDuration");



  /**
   * マウスが出てから非表示にするまでの時間
   * @return
   */
  public final ObjectProperty<Duration> leftDurationProperty(){
    return leftDurationProperty;
  }

  public final Duration getLeftDuration(){
    return leftDurationProperty.get();
  }

  public final void setLeftDuration(final Duration value){
    leftDurationProperty().set(value);
  }

  private final StyleableObjectProperty<Duration> leftDurationProperty =
      new SimpleStyleableObjectProperty<>(LEFT_TIME_CMD, this, "leftDuration");



  /**
   *
   * @return
   */
  public final BooleanProperty popupNonFocusProperty(){
    return popupNonFocusProperty;
  }

  public final boolean isPopupNonFocus(){
    return popupNonFocusProperty.get();
  }

  public final void setPopupNonFocus(final boolean value){
    popupNonFocusProperty().set(value);
  }

  private final StyleableBooleanProperty popupNonFocusProperty=
      new SimpleStyleableBooleanProperty(POPUP_NONFOCUS_CMD,this,"popupNonFocus",false);


  /**
   *
   * @return
   */
  public final BooleanProperty hideOnExitProperty(){
    return hideOnExitProperty;
  }

  public final boolean isHideOnExit(){
    return hideOnExitProperty.get();
  }

  public final void setHideOnExit(final boolean value){
    hideOnExitProperty().set(value);
  }

  private final StyleableBooleanProperty hideOnExitProperty=
      new SimpleStyleableBooleanProperty(HIDE_ON_EXIT_CMD,this,"hideOnExit",false);



  /**
   *
   * @return
   */
  public final DoubleProperty xOffsetProperty(){
    return xOffsetProperty;
  }

  public final double getXOffset(){
    return xOffsetProperty.get();
  }

  public final void setXOffset(final double value){
    xOffsetProperty().set(value);
  }

  private final StyleableDoubleProperty xOffsetProperty=
      new SimpleStyleableDoubleProperty(XOFFSET_CMD,this,"xOffset",TOOLTIP_DEFAULT_XOFFSET);

  /**
   *
   * @return
   */
  public final DoubleProperty yOffsetProperty(){
    return yOffsetProperty;
  }

  public final double getYOffset(){
    return yOffsetProperty.get();
  }

  public final void setYOffset(final double value){
    yOffsetProperty().set(value);
  }

  private final StyleableDoubleProperty yOffsetProperty=
      new SimpleStyleableDoubleProperty(YOFFSET_CMD,this,"yOffset",TOOLTIP_DEFAULT_YOFFSET);

  //---------------------------------------------------------
  //       CSS Meta Data                                        ------
  //------------------------------------------------------------------------
  private static final CssMetaData<CSSBridge, Duration> OPEN_TIME_CMD=
      new CssMetaData<CSSBridge, Duration>("wait-time",
          getDurationConverter()){

    @Override
    public boolean isSettable(final CSSBridge styleable){
      return !styleable.tooltip.openDurationProperty.isBound();
    }
    @Override
    public StyleableProperty<Duration> getStyleableProperty(final CSSBridge styleable){
      return styleable.tooltip.openDurationProperty;
    }
  };

  private static final CssMetaData<CSSBridge, Duration> HIDE_TIME_CMD=
      new CssMetaData<CSSBridge, Duration>("visible-time",
          getDurationConverter()){

    @Override
    public boolean isSettable(final CSSBridge styleable){
      return !styleable.tooltip.hideDurationProperty.isBound();
    }
    @Override
    public StyleableProperty<Duration> getStyleableProperty(final CSSBridge styleable){
      return styleable.tooltip.hideDurationProperty;
    }
  };


  private static final CssMetaData<CSSBridge, Duration> LEFT_TIME_CMD=
      new CssMetaData<CSSBridge, Duration>("closing-time",
          getDurationConverter()){

    @Override
    public boolean isSettable(final CSSBridge styleable){
      return !styleable.tooltip.leftDurationProperty.isBound();
    }
    @Override
    public StyleableProperty<Duration> getStyleableProperty(final CSSBridge styleable){
      return styleable.tooltip.leftDurationProperty;
    }
  };

  private static final CssMetaData<CSSBridge, Boolean> POPUP_NONFOCUS_CMD=
      new CssMetaData<CSSBridge, Boolean>("popup-nonfocus",
          getBooleanConverter(),false){

    @Override
    public boolean isSettable(final CSSBridge styleable){
      return !styleable.tooltip.popupNonFocusProperty.isBound();
    }

    @Override
    public StyleableProperty<Boolean> getStyleableProperty(
        final CSSBridge styleable){
      return styleable.tooltip.popupNonFocusProperty;
    }

  };


  private static final CssMetaData<CSSBridge, Boolean> HIDE_ON_EXIT_CMD=
      new CssMetaData<CSSBridge, Boolean>("hide-on-exit",
          getBooleanConverter(),false){

    @Override
    public boolean isSettable(final CSSBridge styleable){
      return !styleable.tooltip.hideOnExitProperty.isBound();
    }

    @Override
    public StyleableProperty<Boolean> getStyleableProperty(
        final CSSBridge styleable){
      return styleable.tooltip.hideOnExitProperty;
    }

  };

  private static final CssMetaData<CSSBridge, Number> XOFFSET_CMD=
      new CssMetaData<CSSBridge, Number>("offset-x",
          getSizeConverter(),TOOLTIP_DEFAULT_XOFFSET){

    @Override
    public boolean isSettable(final CSSBridge styleable){
      return !styleable.tooltip.xOffsetProperty.isBound();
    }

    @Override
    public StyleableProperty<Number> getStyleableProperty(final CSSBridge styleable){
      return styleable.tooltip.xOffsetProperty;
    }
  };

  private static final CssMetaData<CSSBridge, Number> YOFFSET_CMD=
      new CssMetaData<CSSBridge, Number>("offset-y",
          getSizeConverter(),TOOLTIP_DEFAULT_XOFFSET){

    @Override
    public boolean isSettable(final CSSBridge styleable){
      return !styleable.tooltip.yOffsetProperty.isBound();
    }

    @Override
    public StyleableProperty<Number> getStyleableProperty(final CSSBridge styleable){
      return styleable.tooltip.yOffsetProperty;
    }
  };







  protected class CSSBridge extends PopupControl.CSSBridge{
    final NTooltip tooltip = NTooltip.this;
    public CSSBridge(){
      setAccessibleRole(AccessibleRole.TOOLTIP);
    }
  }

  private static final NTooltipBehavior TOOLTIP_BEHAVIOR=new NTooltipBehavior();



  private static class NTooltipBehavior extends TooltipBehaviorBase<NTooltip>{

    @Override
    protected void setActivate(final NTooltip p ,final Node hover){
      final NTooltip old = getActivatePopup();
      if(old!=p){
        if(old!=null){
          old.setActivated(false);
        }
        if(p!=null){
          p.setActivated(true);
        }
      }
      super.setActivate(p, hover);
    }

    @Override
    protected boolean isPopupOnNonFocusWindow(final NTooltip p ,final Node hover){
      return p.isPopupNonFocus();
    }

    @Override
    protected boolean isHideOnExit(final NTooltip p ,final Node node){
      return p.isHideOnExit();
    }
    @Override
    protected void runOpenTimer(final NTooltip p ,final Node hover){
      Duration d = p.getOpenDuration();
      if(d==null){
        d = getOpenDuration();
      }
      runOpenTimer(d==null?DEFAULT_OPEN_DURATION:d);
    }

    @Override
    protected void runHideTimer(final NTooltip p ,final Node hover){
      Duration d = p.getHideDuration();
      if(d==null){
        d = getHideDuration();
      }
      runHideTimer(d==null?DEFAULT_HIDE_DURATION:d);
    }

    @Override
    protected void runLeftTimer(final NTooltip p ,final Node hover){
      Duration d = p.getLeftDuration();
      if(d==null){
        d = getLeftDuration();
      }
      runLeftTimer(d==null?DEFAULT_LEFT_DURATION:d);
    }

    @Override
    protected void show(final NTooltip p ,final Node hover ,double x ,double y){
      //あんまり意味は分かってないけど
      //とりあえずjavafx.scene.control.Tooltipからほぼ引用

      final NodeOrientation nodeOrientation = hover.getEffectiveNodeOrientation();
      p.getScene().setNodeOrientation(nodeOrientation);
      if (nodeOrientation == NodeOrientation.RIGHT_TO_LEFT) {
        x -= p.getWidth();
      }
      final Window owner = getWindow(hover);
      final double ox =p.getXOffset();
      final double oy =p.getYOffset();
      p.show(owner, floor(x+ox), floor(y+oy));

      if ((y+TOOLTIP_YOFFSET) > p.getAnchorY()) {
        p.hide();
        y -= p.getHeight();
        p.show(owner, floor(x+ox), floor(y));
      }
    }

  }








  //----------------------------------------------------------
  // Tooltipから完全にコピー  著作権は大丈夫か？
  //----------------------------------------------------------


  /**
   * The text to display in the tooltip. If the text is set to null, an empty
   * string will be displayed, despite the value being null.
   */
  public final StringProperty textProperty() { return text; }
  public final void setText(final String value) {
    if (isShowing() && value != null && !value.equals(getText())) {
      //Dynamic tooltip content is location-dependant.
      //Chromium trick.
      setAnchorX(TOOLTIP_BEHAVIOR.getLastMouseX());
      setAnchorY(TOOLTIP_BEHAVIOR.getLastMouseY());
    }
    textProperty().setValue(value);
  }
  public final String getText() { return text.getValue() == null ? "" : text.getValue(); }
  private final StringProperty text = new SimpleStringProperty(this, "text", "");

  /**
   * Specifies the behavior for lines of text <em>when text is multiline</em>.
   * Unlike {@link #contentDisplayProperty() contentDisplay} which affects the
   * graphic and text, this setting only affects multiple lines of text
   * relative to the text bounds.
   */
  public final ObjectProperty<TextAlignment> textAlignmentProperty() {
    return textAlignment;
  }
  public final void setTextAlignment(final TextAlignment value) {
    textAlignmentProperty().setValue(value);
  }
  public final TextAlignment getTextAlignment() {
    return textAlignmentProperty().getValue();
  }
  private final ObjectProperty<TextAlignment> textAlignment =
      new SimpleStyleableObjectProperty<>(TEXT_ALIGNMENT, this, "textAlignment", TextAlignment.LEFT);

  /**
   * Specifies the behavior to use if the text of the {@code Tooltip}
   * exceeds the available space for rendering the text.
   */
  public final ObjectProperty<OverrunStyle> textOverrunProperty() {
    return textOverrun;
  }
  public final void setTextOverrun(final OverrunStyle value) {
    textOverrunProperty().setValue(value);
  }
  public final OverrunStyle getTextOverrun() {
    return textOverrunProperty().getValue();
  }
  private final ObjectProperty<OverrunStyle> textOverrun =
      new SimpleStyleableObjectProperty<OverrunStyle>(TEXT_OVERRUN, this, "textOverrun", OverrunStyle.ELLIPSIS);

  /**
   * If a run of text exceeds the width of the Tooltip, then this variable
   * indicates whether the text should wrap onto another line.
   */
  public final BooleanProperty wrapTextProperty() {
    return wrapText;
  }
  public final void setWrapText(final boolean value) {
    wrapTextProperty().setValue(value);
  }
  public final boolean isWrapText() {
    return wrapTextProperty().getValue();
  }
  private final BooleanProperty wrapText =
      new SimpleStyleableBooleanProperty(WRAP_TEXT, this, "wrapText", false);


  /**
   * The default font to use for text in the Tooltip. If the Tooltip's text is
   * rich text then this font may or may not be used depending on the font
   * information embedded in the rich text, but in any case where a default
   * font is required, this font will be used.
   */
  public final ObjectProperty<Font> fontProperty() {
    return font;
  }
  public final void setFont(final Font value) {
    fontProperty().setValue(value);
  }
  public final Font getFont() {
    return fontProperty().getValue();
  }
  private final ObjectProperty<Font> font = new StyleableObjectProperty<Font>(Font.getDefault()) {
    private boolean fontSetByCss = false;

    @Override public void applyStyle(final StyleOrigin newOrigin, final Font value) {
      // RT-20727 - if CSS is setting the font, then make sure invalidate doesn't call impl_reapplyCSS
      try {
        // super.applyStyle calls set which might throw if value is bound.
        // Have to make sure fontSetByCss is reset.
        fontSetByCss = true;
        super.applyStyle(newOrigin, value);
      } catch(final Exception e) {
        throw e;
      } finally {
        fontSetByCss = false;
      }
    }

    @Override public void set(final Font value) {
      final Font oldValue = get();
      if (value != null ? !value.equals(oldValue) : oldValue != null) {
        super.set(value);
      }
    }

    @Override protected void invalidated() {
      // RT-20727 - if font is changed by calling setFont, then
      // css might need to be reapplied since font size affects
      // calculated values for styles with relative values
      if(fontSetByCss == false) {
        NTooltip.this.bridge.impl_reapplyCSS();
      }
    }

    @Override public CssMetaData<CSSBridge,Font> getCssMetaData() {
      return FONT;
    }

    @Override public Object getBean() {
      return NTooltip.this;
    }

    @Override public String getName() {
      return "font";
    }
  };

  /**
   * An optional icon for the Tooltip. This can be positioned relative to the
   * text by using the {@link #contentDisplayProperty() content display}
   * property.
   * The node specified for this variable cannot appear elsewhere in the
   * scene graph, otherwise the {@code IllegalArgumentException} is thrown.
   * See the class description of {@link javafx.scene.Node Node} for more detail.
   */
  public final ObjectProperty<Node> graphicProperty() {
    return graphic;
  }
  public final void setGraphic(final Node value) {
    graphicProperty().setValue(value);
  }
  public final Node getGraphic() {
    return graphicProperty().getValue();
  }
  private final ObjectProperty<Node> graphic = new StyleableObjectProperty<Node>() {
    // The graphic is styleable by css, but it is the
    // imageUrlProperty that handles the style value.
    @Override public CssMetaData getCssMetaData() {
      return GRAPHIC;
    }

    @Override public Object getBean() {
      return NTooltip.this;
    }

    @Override public String getName() {
      return "graphic";
    }

  };

  private StyleableStringProperty imageUrlProperty() {
    if (imageUrl == null) {
      imageUrl = new StyleableStringProperty() {
        // If imageUrlProperty is invalidated, this is the origin of the style that
        // triggered the invalidation. This is used in the invaildated() method where the
        // value of super.getStyleOrigin() is not valid until after the call to set(v) returns,
        // by which time invalidated will have been called.
        // This value is initialized to USER in case someone calls set on the imageUrlProperty, which
        // is possible:
        //     CssMetaData metaData = ((StyleableProperty)labeled.graphicProperty()).getCssMetaData();
        //     StyleableProperty prop = metaData.getStyleableProperty(labeled);
        //     prop.set(someUrl);
        //
        // TODO: Note that prop != labeled, which violates the contract between StyleableProperty and CssMetaData.
        StyleOrigin origin = StyleOrigin.USER;

        @Override public void applyStyle(final StyleOrigin origin, final String v) {

          this.origin = origin;

          // Don't want applyStyle to throw an exception which would leave this.origin set to the wrong value
          if (graphic == null || graphic.isBound() == false) {
            super.applyStyle(origin, v);
          }

          // Origin is only valid for this invocation of applyStyle, so reset it to USER in case someone calls set.
          this.origin = StyleOrigin.USER;
        }

        @Override protected void invalidated() {

          // need to call super.get() here since get() is overridden to return the graphicProperty's value
          final String url = super.get();

          if (url == null) {
            ((StyleableProperty<Node>)graphicProperty()).applyStyle(origin, null);
          } else {
            // RT-34466 - if graphic's url is the same as this property's value, then don't overwrite.
            final Node graphicNode = NTooltip.this.getGraphic();
            if (graphicNode instanceof ImageView) {
              final ImageView imageView = (ImageView)graphicNode;
              final Image image = imageView.getImage();
              if (image != null) {
                final String imageViewUrl = image.impl_getUrl();
                if (url.equals(imageViewUrl)) {
                  return;
                }
              }

            }

            final Image img = StyleManager.getInstance().getCachedImage(url);

            if (img != null) {
              // Note that it is tempting to try to re-use existing ImageView simply by setting
              // the image on the current ImageView, if there is one. This would effectively change
              // the image, but not the ImageView which means that no graphicProperty listeners would
              // be notified. This is probably not what we want.

              // Have to call applyStyle on graphicProperty so that the graphicProperty's
              // origin matches the imageUrlProperty's origin.
              ((StyleableProperty<Node>)graphicProperty()).applyStyle(origin, new ImageView(img));
            }
          }
        }

        @Override public String get() {
          // The value of the imageUrlProperty is that of the graphicProperty.
          // Return the value in a way that doesn't expand the graphicProperty.
          final Node graphic = getGraphic();
          if (graphic instanceof ImageView) {
            final Image image = ((ImageView)graphic).getImage();
            if (image != null) {
              return image.impl_getUrl();
            }
          }
          return null;
        }

        @Override public StyleOrigin getStyleOrigin() {
          // The origin of the imageUrlProperty is that of the graphicProperty.
          // Return the origin in a way that doesn't expand the graphicProperty.
          return graphic != null ? ((StyleableProperty<Node>)graphic).getStyleOrigin() : null;
        }

        @Override public Object getBean() {
          return NTooltip.this;
        }

        @Override public String getName() {
          return "imageUrl";
        }

        @Override public CssMetaData<CSSBridge,String> getCssMetaData() {
          return GRAPHIC;
        }
      };
    }
    return imageUrl;
  }

  private StyleableStringProperty imageUrl = null;

  /**
   * Specifies the positioning of the graphic relative to the text.
   */
  public final ObjectProperty<ContentDisplay> contentDisplayProperty() {
    return contentDisplay;
  }
  public final void setContentDisplay(final ContentDisplay value) {
    contentDisplayProperty().setValue(value);
  }
  public final ContentDisplay getContentDisplay() {
    return contentDisplayProperty().getValue();
  }
  private final ObjectProperty<ContentDisplay> contentDisplay =
      new SimpleStyleableObjectProperty<>(CONTENT_DISPLAY, this, "contentDisplay", ContentDisplay.LEFT);

  /**
   * The amount of space between the graphic and text
   */
  public final DoubleProperty graphicTextGapProperty() {
    return graphicTextGap;
  }
  public final void setGraphicTextGap(final double value) {
    graphicTextGapProperty().setValue(value);
  }
  public final double getGraphicTextGap() {
    return graphicTextGapProperty().getValue();
  }
  private final DoubleProperty graphicTextGap =
      new SimpleStyleableDoubleProperty(GRAPHIC_TEXT_GAP, this, "graphicTextGap", 4d);

  /**
   * Typically, the tooltip is "activated" when the mouse moves over a Control.
   * There is usually some delay between when the Tooltip becomes "activated"
   * and when it is actually shown. The details (such as the amount of delay, etc)
   * is left to the Skin implementation.
   */
  private final ReadOnlyBooleanWrapper activated = new ReadOnlyBooleanWrapper(this, "activated");
  final void setActivated(final boolean value) { activated.set(value); }
  public final boolean isActivated() { return activated.get(); }
  public final ReadOnlyBooleanProperty activatedProperty() { return activated.getReadOnlyProperty(); }




  /***************************************************************************
   *                                                                         *
   *                         Stylesheet Handling                             *
   *                                                                         *
   **************************************************************************/


  private static final CssMetaData<CSSBridge,Font> FONT =
      new FontCssMetaData<CSSBridge>("-fx-font", Font.getDefault()) {

    @Override
    public boolean isSettable(final CSSBridge cssBridge) {
      return !cssBridge.tooltip.fontProperty().isBound();
    }

    @Override
    public StyleableProperty<Font> getStyleableProperty(final CSSBridge cssBridge) {
      return (StyleableProperty<Font>)cssBridge.tooltip.fontProperty();
    }
  };

  private static final CssMetaData<CSSBridge,TextAlignment> TEXT_ALIGNMENT =
      new CssMetaData<CSSBridge,TextAlignment>("-fx-text-alignment",
          new EnumConverter<TextAlignment>(TextAlignment.class),
          TextAlignment.LEFT) {

    @Override
    public boolean isSettable(final CSSBridge cssBridge) {
      return !cssBridge.tooltip.textAlignmentProperty().isBound();
    }

    @Override
    public StyleableProperty<TextAlignment> getStyleableProperty(final CSSBridge cssBridge) {
      return (StyleableProperty<TextAlignment>)cssBridge.tooltip.textAlignmentProperty();
    }
  };

  private static final CssMetaData<CSSBridge,OverrunStyle> TEXT_OVERRUN =
      new CssMetaData<CSSBridge,OverrunStyle>("-fx-text-overrun",
          new EnumConverter<OverrunStyle>(OverrunStyle.class),
          OverrunStyle.ELLIPSIS) {

    @Override
    public boolean isSettable(final CSSBridge cssBridge) {
      return !cssBridge.tooltip.textOverrunProperty().isBound();
    }

    @Override
    public StyleableProperty<OverrunStyle> getStyleableProperty(final CSSBridge cssBridge) {
      return (StyleableProperty<OverrunStyle>)cssBridge.tooltip.textOverrunProperty();
    }
  };

  private static final CssMetaData<CSSBridge,Boolean> WRAP_TEXT =
      new CssMetaData<CSSBridge,Boolean>("-fx-wrap-text",
          BooleanConverter.getInstance(), Boolean.FALSE) {

    @Override
    public boolean isSettable(final CSSBridge cssBridge) {
      return !cssBridge.tooltip.wrapTextProperty().isBound();
    }

    @Override
    public StyleableProperty<Boolean> getStyleableProperty(final CSSBridge cssBridge) {
      return (StyleableProperty<Boolean>)cssBridge.tooltip.wrapTextProperty();
    }
  };

  private static final CssMetaData<CSSBridge,String> GRAPHIC =
      new CssMetaData<CSSBridge,String>("-fx-graphic",
          StringConverter.getInstance()) {

    @Override
    public boolean isSettable(final CSSBridge cssBridge) {
      return !cssBridge.tooltip.graphicProperty().isBound();
    }

    @Override
    public StyleableProperty<String> getStyleableProperty(final CSSBridge cssBridge) {
      return cssBridge.tooltip.imageUrlProperty();
    }
  };

  private static final CssMetaData<CSSBridge,ContentDisplay> CONTENT_DISPLAY =
      new CssMetaData<CSSBridge,ContentDisplay>("-fx-content-display",
          new EnumConverter<ContentDisplay>(ContentDisplay.class),
          ContentDisplay.LEFT) {

    @Override
    public boolean isSettable(final CSSBridge cssBridge) {
      return !cssBridge.tooltip.contentDisplayProperty().isBound();
    }

    @Override
    public StyleableProperty<ContentDisplay> getStyleableProperty(final CSSBridge cssBridge) {
      return (StyleableProperty<ContentDisplay>)cssBridge.tooltip.contentDisplayProperty();
    }
  };

  private static final CssMetaData<CSSBridge,Number> GRAPHIC_TEXT_GAP =
      new CssMetaData<CSSBridge,Number>("-fx-graphic-text-gap",
          SizeConverter.getInstance(), 4.0) {

    @Override
    public boolean isSettable(final CSSBridge cssBridge) {
      return !cssBridge.tooltip.graphicTextGapProperty().isBound();
    }

    @Override
    public StyleableProperty<Number> getStyleableProperty(final CSSBridge cssBridge) {
      return (StyleableProperty<Number>)cssBridge.tooltip.graphicTextGapProperty();
    }
  };



  //---------------------------------------------------------
  //       CSS Meta Data List                                    ------
  //------------------------------------------------------------------------

  private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
  static{
    final ArrayList<CssMetaData<? extends Styleable, ?>> list =
        new ArrayList<>(PopupControl.getClassCssMetaData());
    list.add(OPEN_TIME_CMD);
    list.add(HIDE_TIME_CMD);
    list.add(LEFT_TIME_CMD);
    list.add(POPUP_NONFOCUS_CMD);
    list.add(HIDE_ON_EXIT_CMD);
    list.add(XOFFSET_CMD);
    list.add(YOFFSET_CMD);
    list.add(FONT);
    list.add(TEXT_ALIGNMENT);
    list.add(TEXT_OVERRUN);
    list.add(WRAP_TEXT);
    list.add(GRAPHIC);
    list.add(CONTENT_DISPLAY);
    list.add(GRAPHIC_TEXT_GAP);
    list.trimToSize();
    STYLEABLES =Collections.unmodifiableList(list);
  }
  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
    return STYLEABLES;
  }
  @Override
  public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
    return getClassCssMetaData();
  }

  @Override
  public Styleable getStyleableParent() {
    if(TOOLTIP_BEHAVIOR==null||TOOLTIP_BEHAVIOR.getHoverNode()==null) {
      return super.getStyleableParent();
    }
    return TOOLTIP_BEHAVIOR.getHoverNode();
  }
}
