package nodamushi.jfx.popup;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Tooltipを対象にするPopupBehavior
 * @author nodamushi
 *
 */
public class TooltipBehavior extends TooltipBehaviorBase<Tooltip>{

  public static TooltipBehavior createManagedInstance(){
    final TooltipBehavior t = new TooltipBehavior();
    manageVisible(t);
    return t;
  }

  public static TooltipBehavior createManagedInstance(
      final Duration open,final Duration hide,final Duration left){
    final TooltipBehavior t = new TooltipBehavior();
    if(open!=null){
      t.setOpenDuration(open);
    }
    if(hide!=null){
      t.setHideDuration(hide);
    }
    if(left!=null){
      t.setLeftDuration(left);
    }
    manageVisible(t);
    return t;
  }

  @Override
  protected void setActivate(final Tooltip p ,final Node hover){
    final Tooltip old = getActivatePopup();
    if(old!=p){
      if(old!=null){
        setActivated(old, Boolean.FALSE);
      }
      if(p!=null){
        setActivated(old, Boolean.TRUE);
      }
    }
    super.setActivate(p, hover);
  }


  @Override
  protected void runOpenTimer(final Tooltip p ,final Node hover){
    final Duration d = getOpenDuration();
    runOpenTimer(d==null?DEFAULT_OPEN_DURATION:d);
  }

  @Override
  protected void runHideTimer(final Tooltip p ,final Node hover){
    final Duration d = getHideDuration();
    runHideTimer(d==null?DEFAULT_HIDE_DURATION:d);
  }

  @Override
  protected void runLeftTimer(final Tooltip p ,final Node hover){
    final Duration d = getLeftDuration();
    runLeftTimer(d==null?DEFAULT_LEFT_DURATION:d);
  }

  //-------------------------------------------
  //    一つのTooltipを使い回す
  //-------------------------------------------

  public void setDefautlTooltip(final Tooltip t){
    defaultTooltip = t;
  }

  public Tooltip getDefaultTooltip(){
    if(defaultTooltip == null){
      defaultTooltip = new Tooltip();
    }
    return defaultTooltip;
  }

  private Tooltip defaultTooltip;

  /**
   * {@link TooltipBehavior#getDefaultTooltip()}で得られる
   * Tooltipを用いてインストールする
   * @param node
   */
  public void install(final Node node){
    final Tooltip t = getDefaultTooltip();
    install(node,t);
  }

}
