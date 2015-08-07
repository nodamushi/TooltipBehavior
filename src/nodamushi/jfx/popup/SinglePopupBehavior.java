package nodamushi.jfx.popup;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Tooltipなどのように、常に最大で一つのポップアップしか表示されないような
 * 挙動を定義する。<br/>
 * 基本的な動作は javafx.scene.control.Tooltip.TooltipBehavior
 * と同様になるようにしてある。<br/><br/>
 *
 * 複数のSinglePopupBehaviorの間でも最大で一つのポップアップしか表示させたくない
 * 場合は、BehaviorGroupを作成し、登録する。
 *
 * @author nodamushi
 *
 * @param <P>
 */
public abstract class SinglePopupBehavior<P extends PopupWindow>{

  /**初期化をする。最初にinstallが実行されたときに呼び出される*/
  protected void initialize(){
    if(!initialized){
      initialized = true;
      open = new Timeline();
      hide = new Timeline();
      left = new Timeline();

      open.setOnFinished(this::openAction);
      hide.setOnFinished(this::hideAction);
      left.setOnFinished(this::leftAction);

      move = this::mouseMove;
      exit = this::mouseExited;
      press= this::mousePressed;
    }
  }
  /**
   * Nodeにマウスがホバーしたとき、ポップアップするようインストールする。<br/>
   * nに複数回インストールした際の動作は保証しない。
   * @param n
   * @param p
   */
  public void install(final Node n,final P p){
    if(n == null || p == null) {
      return;
    }
    if(!initialized){
      initialize();
    }
    n.addEventHandler(MouseEvent.MOUSE_MOVED, move);
    n.addEventHandler(MouseEvent.MOUSE_EXITED, exit);
    n.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
    storePopup(n, p);
  }
  /**
   * インストールした内容を削除する
   * @param n
   */
  public void uninstall(final Node n){
    if(n == null || !initialized) {
      return;
    }
    n.removeEventHandler(MouseEvent.MOUSE_MOVED, move);
    n.removeEventHandler(MouseEvent.MOUSE_EXITED, exit);
    n.removeEventHandler(MouseEvent.MOUSE_PRESSED, press);
    final P p = getPopup(n);
    if(p!=null && getVisiblePopup() == p && getVisibleNode()==n){
      p.hide();
      setVisible(null, null);
      stopHideTimer();
      stopLeftTimer();
    }
    storePopup(n, null);
  }

  //-----------------------------------------------
  //         Hook
  //-----------------------------------------------
  /**
   * Popupが表示可能かどうかの判断を行うBiPredicateを設定する
   * @param p
   */
  public void setDisplayableChecker(final BiPredicate<? super P, ? super Node> p){
    this.visibityc = p;
  }

  public BiPredicate<? super P, ? super Node> getDisplayableChecker(){
    return visibityc;
  }

  protected boolean checkDisplayable(final P p,final Node hover){
    final BiPredicate<? super P, ? super Node> b = getDisplayableChecker();
    return b == null ? true: b.test(p, hover);
  }
  /**
   * pを表示することが可能かどうか判断する
   * @param p
   * @param hover
   * @return
   */
  protected boolean isDisplayable(final P p,final Node hover){
    final Window w = getWindow(hover);
    return w != null && isWindowHierarchyVisible(hover)
        && (isPopupOnNonFocusWindow(p,hover)|| hasFocus(w)) && checkDisplayable(p, hover);
  }

  protected boolean isPopupOnNonFocusWindow(final P p,final Node hover){
    return isPopupOnNonFocusWindow();
  }

  /**
   * 表示する前に、ポップアップの内容を更新するBiConsumerを登録する
   * @param updater
   */
  public void setPopupUpdater(final BiConsumer<? super P, ? super Node> updater){
    this.update = updater;
  }

  public BiConsumer<? super P, ? super Node> getPopupUpdater(){
    return update;
  }
  /**
   * popupUpdaterが登録されていれば、更新を行う
   * @param p
   * @param node
   */
  protected void updatePopup(final P p,final Node node){
    final BiConsumer<? super P, ? super Node> u = getPopupUpdater();
    if(u!=null){
      u.accept(p, node);
    }
  }

  //-----------------------------------------------
  //         MouseEvent
  //-----------------------------------------------

  public final double getLastMouseX(){return x;}
  public final double getLastMouseY(){return y;}
  protected final void setMousePosition(final MouseEvent e){
    x = e.getScreenX();
    y = e.getScreenY();
  }

  protected void mouseMove(final MouseEvent e){
    setMousePosition(e);
    final Object source = e.getSource();
    if(!(source instanceof Node)){
      return;
    }
    final Node hover = (Node)source;
    final P p =getPopup(hover);
    if(p == null) {
      return;
    }
    final P v = getVisiblePopup();
    final boolean othershow = isOtherBehaviorsShowing();
    if(othershow || v!=null){
      if((othershow || (p!=v || hover != getVisibleNode()))
          && isDisplayable(p, hover)){
        if(v!=null){
          v.hide();
        }
        stopLeftTimer();
        stopOpenTimer();
        final double x = getLastMouseX();
        final double y = getLastMouseY();
        updatePopup(p, hover);
        show(p, hover, x, y);
        killOtherBehaviors();
        setVisible(p, hover);
        runHideTimer(p, hover);
      }
    }else{
      stopLeftTimer();
      stopHideTimer();
      setActivate(p, hover);
      runOpenTimer(p, hover);
    }

  }

  protected void mouseExited(final MouseEvent e){
    setMousePosition(e);
    final P v = getVisiblePopup();
    if(v!=null){
      if(isHideOnExit(v,getVisibleNode())){
        kill();
      }else{
        stopOpenTimer();
        stopHideTimer();
        runLeftTimer(v, getVisibleNode());
      }
    }else{
      stopOpenTimer();
      stopLeftTimer();
      stopHideTimer();
      setActivate(null, null);
    }
  }

  protected boolean isHideOnExit(final P p,final Node node){
    return isHideOnExit();
  }

  protected void mousePressed(final MouseEvent e){
    setMousePosition(e);
    kill();
  }



  //-----------------------------------------------
  //         Timer
  //-----------------------------------------------
  protected void openAction(final ActionEvent e){
    final P p = getActivatePopup();
    final Node n = getHoverNode();
    setActivate(null, null);
    if(isDisplayable(p, n)){
      stopLeftTimer();
      stopOpenTimer();
      final double x = getLastMouseX();
      final double y = getLastMouseY();
      updatePopup(p, n);
      show(p, n, x, y);
      killOtherBehaviors();
      setVisible(p, n);
      stopLeftTimer();
      runHideTimer(p, n);
    }
  }

  protected void hideAction(final ActionEvent e){
    final P p = getVisiblePopup();
    if(p!=null){
      p.hide();
    }
    setVisible(null, null);
    stopOpenTimer();
    stopLeftTimer();
  }

  protected void leftAction(final ActionEvent e){
    final P p = getVisiblePopup();
    if(p!=null){
      p.hide();
    }
    setVisible(null, null);
    stopOpenTimer();
    stopHideTimer();
  }

  private static boolean isStopped(final Timeline t){
    return t.getStatus() == Status.STOPPED;
  }
  private static boolean isRunning(final Timeline t){
    return t.getStatus() == Status.RUNNING;
  }
  protected final boolean isOpenRunning(){return isRunning(open);}
  protected final boolean isHideRunning(){return isRunning(hide);}
  protected final boolean isLeftRunning(){return isRunning(left);}
  protected final boolean isOpenStopped(){return isStopped(open);}
  protected final boolean isHideStopped(){return isStopped(hide);}
  protected final boolean isLeftStopped(){return isStopped(left);}

  private static void runTimer(final Duration d,final Timeline t){
    if(!isStopped(t)) {
      t.stop();
    }
    final ObservableList<KeyFrame> list = t.getKeyFrames();
    if(d!=null){
      if(list.isEmpty()){
        list.add(new KeyFrame(d));
      }else if(!list.get(0).getTime().equals(d)){
        list.setAll(new KeyFrame(d));
      }
    }else if(d == null && list.isEmpty()){
      list.add(new KeyFrame(Duration.INDEFINITE));
    }
    t.playFromStart();
  }
  /**
   * 対象に見合ったDurationを計算し、{@link SinglePopupBehavior#runOpenTimer(Duration)}
   * を呼び出す
   * @param p
   */
  protected abstract void runOpenTimer(P p,Node hover);
  /**渡されたDurationの間待機し、Popupを表示するタイマーを起動する*/
  protected final void runOpenTimer(final Duration d){
    runTimer(d,open);
  }
  protected final void stopOpenTimer(){open.stop();}
  /**
   * 対象に見合ったDurationを計算し、{@link SinglePopupBehavior#runHideTimer(Duration)}
   * を呼び出す
   * @param p
   */
  protected abstract void runHideTimer(P p,Node hover);
  /**
   * 渡されたDurationの間待機し、Popupを非表示にするタイマーを起動する。<br/>
   * runOpenTimerがイベントを発行した後に使われる<br/>
   */
  protected final void runHideTimer(final Duration d){
    runTimer(d,hide);
  }
  protected final void stopHideTimer(){hide.stop();}

  /**
   * 対象に見合ったDurationを計算し、{@link SinglePopupBehavior#runLeftTimer(Duration)}
   * を呼び出す
   * @param p
   */
  protected abstract void runLeftTimer(P p,Node hover);
  /**
   * 渡されたDurationの間待機し、Popupを非表示にするタイマーを起動する<br/>
   * マウスがNodeから離れた後に使う
   */
  protected final void runLeftTimer(final Duration d){
    runTimer(d,left);
  }
  protected final void stopLeftTimer(){left.stop();}



  /**
   * 表示しているポップアップを非表示にし、全てのタイマーを止める
   */
  public void kill(){
    final P v = getVisiblePopup();
    if(v!=null){
      v.hide();
    }
    setVisible(null, null);
    setActivate(null, null);
    stopOpenTimer();
    stopHideTimer();
    stopLeftTimer();
  }


  //-----------------------------------------------
  //         Node
  //-----------------------------------------------

  protected abstract void show(P p,Node hover,double anchorX,double anchorY);

  /**
   * nodeからpを取り出せるように保存します。<br/>
   * 既にnodeに他のpが割り当てられている場合は、上書きします<br/>
   * また、p=nullの場合はnodeに関する情報を削除します。<br/>
   * デフォルト実装では{@link Node#getProperties()}を用います。
   * @param node
   * @param p
   */
  protected void storePopup(final Node node,final P p){
    if(p == null){
      node.getProperties().remove(PROPERTY_KEY);
    }else{
      node.getProperties().put(PROPERTY_KEY, p);
    }
  }

  /**
   * nodeからPopupを取り出します。<br/>
   * デフォルト実装は型安全ではありません。
   * @param node
   * @return
   */
  @SuppressWarnings("unchecked")
  protected P getPopup(final Node node){
    final Object o = node.getProperties().get(PROPERTY_KEY);
    if(o instanceof PopupWindow){
      return (P)o;
    }
    return null;
  }

  /** ActivatePopupに関連づけられたNode */
  protected Node getHoverNode(){return hover;}
  /** VisiblePopupに関連づけられたNode */
  protected Node getVisibleNode(){return vinode;}
  /** 表示しようと待機中のPopup*/
  protected P getActivatePopup(){return activate;}
  /** 現在表示中のPopup*/
  protected P getVisiblePopup(){return visible;}
  /** 表示待機中のPopupを設定する*/
  protected void setActivate(final P p,final Node hover){
    this.activate=p;this.hover = hover;
  }
  /** 表示中のPopupを設定する */
  protected void setVisible(final P p,final Node node){
    this.visible=p;this.vinode = node;
  }
  /**表示中のPopupがあるかどうか*/
  public boolean isShowing(){return getVisiblePopup()!=null;}




  //-----------------------------------------------
  //         Group
  //-----------------------------------------------
  protected final void killOtherBehaviors(){
    if(groups==null) {
      return;
    }
    for(final BehaviorGroup g:groups){
      g.killOthers(this);
    }
  }

  protected final boolean isOtherBehaviorsShowing(){
    if(groups==null) {
      return false;
    }
    for(final BehaviorGroup g:groups){
      if(g.isShowing(this)) {
        return true;
      }
    }
    return false;
  }

  private void addGroup(final BehaviorGroup g){
    if(groups == null){
      groups = new ArrayList<>(1);
    }
    groups.add(g);
  }

  private void removeGroup(final BehaviorGroup g){
    if(groups == null) {
      return;
    }
    groups.remove(g);
    if(groups.isEmpty()){
      groups = null;
    }
  }
  public List<BehaviorGroup> getGroups(){
    if(groups == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(groups);
  }

  /**
   * PopupBehaviorの集合。<br/>
   * このグループ内では一つだけポップアップを表示するように
   * PopupBehaviorは動作する<br/>
   * @author nodamushi
   *
   */
  public static class BehaviorGroup{
    private List<WeakReference<SinglePopupBehavior<?>>> behaviors;

    public BehaviorGroup(){}
    public BehaviorGroup(final SinglePopupBehavior<?>... behaviors){
      addAll(behaviors);
    }

    private void killOthers(final SinglePopupBehavior<?> source){
      if(behaviors==null) {return;}
      for(final WeakReference<SinglePopupBehavior<?>> r:behaviors){
        final SinglePopupBehavior<?> b = r.get();
        if(b!=null && b!=source) {
          b.kill();
        }
      }
    }
    private boolean isShowing(final SinglePopupBehavior<?> source){
      if(behaviors==null) {return false;}
      for(final WeakReference<SinglePopupBehavior<?>> r:behaviors){
        final SinglePopupBehavior<?> b = r.get();
        if(b!=null && b!= source && b.isShowing()) {
          return true;
        }
      }
      return false;
    }


    public boolean contains(final SinglePopupBehavior<?> behaivor){
      if(behaivor==null || behaviors==null) {
        return false;
      }
      for(final WeakReference<SinglePopupBehavior<?>> r:behaviors){
        final SinglePopupBehavior<?> b = r.get();
        if(behaivor.equals(b)) {
          return true;
        }
      }
      return false;
    }

    /**このグループ内で表示中のPopupBehaivorが存在するかどうか*/
    public boolean isShowing(){
      if(behaviors==null) {return false;}
      for(final WeakReference<SinglePopupBehavior<?>> r:behaviors){
        final SinglePopupBehavior<?> b = r.get();
        if(b!=null && b.isShowing()) {
          return true;
        }
      }
      return false;
    }

    public void add(final SinglePopupBehavior<?> behavior){
      if(behavior ==null || contains(behavior)) {
        return;
      }
      if(behaviors == null){
        behaviors = new ArrayList<>();
      }else if(!behaviors.isEmpty()){
        clean();
      }
      final WeakReference<SinglePopupBehavior<?>> w = new WeakReference<>(behavior);
      behaviors.add(w);
      behavior.addGroup(this);
    }

    public void remove(final SinglePopupBehavior<?> behavior){
      if(behavior ==null || behaviors==null) {return;}
      final Iterator<WeakReference<SinglePopupBehavior<?>>> i = behaviors.iterator();
      while(i.hasNext()) {
        final WeakReference<SinglePopupBehavior<?>> w = i.next();
        final SinglePopupBehavior<?> b = w.get();
        if(b == null ) {
          i.remove();
        }else if(b.equals(behavior)){
          i.remove();
          b.removeGroup(this);
          break;
        }
      }
      while(i.hasNext()) {
        if(i.next().get()==null) {
          i.remove();
        }
      }
      if(behaviors.isEmpty()){
        behaviors = null;
      }
    }

    public void removeAll(final Collection<SinglePopupBehavior<?>> behaviors){
      if(this.behaviors ==null || behaviors==null) {return;}
      for(final SinglePopupBehavior<?> r:behaviors){
        if(r == null) {
          continue;
        }
        for (final Iterator<WeakReference<SinglePopupBehavior<?>>> i = this.behaviors.iterator();
            i.hasNext();) {
          final WeakReference<SinglePopupBehavior<?>> w = i.next();
          final SinglePopupBehavior<?> b = w.get();
          if(b != null && b.equals(r)) {
            i.remove();
            b.removeGroup(this);
            break;
          }
        }
      }
      clean();
      if(this.behaviors.isEmpty()){
        this.behaviors = null;
      }
    }

    public void removeAll(final SinglePopupBehavior<?>... behaviors){
      if(behaviors==null) {
        return;
      }
      removeAll(Arrays.asList(behaviors));
    }

    public void addAll(final SinglePopupBehavior<?>... behaviors){
      if(behaviors == null) {
        return;
      }
      addAll(Arrays.asList(behaviors));
    }

    public void addAll(final Collection<SinglePopupBehavior<?>> behaviors){
      if(behaviors == null) {
        return;
      }
      if(this.behaviors == null){
        this.behaviors = new ArrayList<>(behaviors.size());
      }
      for(final SinglePopupBehavior<?> b:behaviors){add(b);}
    }

    private void clean(){
      for (final Iterator<WeakReference<SinglePopupBehavior<?>>> i = behaviors.iterator();
          i.hasNext();) {
        final WeakReference<SinglePopupBehavior<?>> w1 = i.next();
        if(w1.get() == null) {
          i.remove();
        }
      }
    }

  }

  private static final BehaviorGroup GROUP = new BehaviorGroup();

  public static void manageVisible(final SinglePopupBehavior<?> behavior){
    GROUP.add(behavior);
  }

  public static void manageVisible(final SinglePopupBehavior<?>... behaviors){
    GROUP.addAll(behaviors);
  }

  public static void unmanageVisible(final SinglePopupBehavior<?> behavior){
    GROUP.remove(behavior);
  }

  public static void unmanageVisible(final SinglePopupBehavior<?>... behaviors){
    GROUP.removeAll(behaviors);
  }

  public static BehaviorGroup getStaticGroup(){
    return GROUP;
  }

  //-----------------------------------------------
  //         Utility for sub classes
  //-----------------------------------------------

  private static Method SET_ACTIVATED;
  private static boolean SET_ACTICATED_LOADED=false;
  private static void loadSetActivated(){
    Method m;
    try{
      m = Tooltip.class.getDeclaredMethod("setActivated", boolean.class);
      m.setAccessible(true);
    }catch(final Exception e){m=null;}
    SET_ACTIVATED = m;
    SET_ACTICATED_LOADED =true;
  }
  /** Reflectionを用いてTooltip.setActivatedを呼び出す*/
  protected static void setActivated(final Tooltip t,final Boolean b){
    if(SET_ACTIVATED==null){
      if(SET_ACTICATED_LOADED){
        return;
      }else{
        loadSetActivated();
        if(SET_ACTIVATED==null){return;}
      }
    }
    try{
      SET_ACTIVATED.invoke(t, b);
    }catch(final Exception e){}
  }
  /**親ウィンドウを取得する
   * @param n Node.enable {@code null}*/
  protected static Window getWindow(final Node n){
    if(n==null) {
      return null;
    }
    final Scene s = n.getScene();
    return s==null? null: s.getWindow();
  }
  /** ウィンドウがフォーカスを持っているかどうか
   * @param w Window.enable {@code null}*/
  protected static boolean hasFocus(final Window w){
    return w == null? false:w.isFocused();
  }
  /** Nodeがウィンドウ内で可視かどうか。<br/>
   * javafx.scene.control.Tooltipからほぼ丸々コピペ*/
  protected static boolean isWindowHierarchyVisible(final Node node){
    if(node == null || !node.isVisible()) {
      return false;
    }
    boolean treeVisible = true;
    Parent parent = node.getParent();
    while (parent != null && treeVisible) {
      treeVisible = parent.isVisible();
      parent = parent.getParent();
    }
    return treeVisible;
  }



  //-----------------------------------------------
  //         Field
  //-----------------------------------------------
  private boolean initialized = false;
  private Timeline open,hide,left;
  private P activate,visible;
  private Node hover,vinode;
  private List<BehaviorGroup> groups;
  private double x,y;
  private EventHandler<MouseEvent> move,exit,press;
  private BiPredicate<? super P, ? super Node> visibityc;
  private BiConsumer<? super P, ? super Node> update;
  protected static final String PROPERTY_KEY="nodamushi.jfx.popup.PopupBehavior.PROPERTY_KEY";


  /**
   * フォーカスがないウィンドウでもポップアップするかどうか<br/>
   * デフォルトはfalse
   * @return
   */
  public final BooleanProperty popupOnNonFocusWindowProperty(){
    if (popupOnNonFocusWindowProperty == null) {
      popupOnNonFocusWindowProperty = new SimpleBooleanProperty(this, "popupOnNonFocusWindow", false);
    }
    return popupOnNonFocusWindowProperty;
  }

  public final boolean isPopupOnNonFocusWindow(){
    return popupOnNonFocusWindowProperty == null ? false : popupOnNonFocusWindowProperty.get();
  }

  public final void setPopupOnNonFocusWindow(final boolean value){
    popupOnNonFocusWindowProperty().set(value);
  }

  private BooleanProperty popupOnNonFocusWindowProperty;

  /**
   *
   * @return
   */
  public final BooleanProperty hideOnExitProperty(){
    if (hideOnExitProperty == null) {
      hideOnExitProperty = new SimpleBooleanProperty(this, "hideOnExit", false);
    }
    return hideOnExitProperty;
  }

  public final boolean isHideOnExit(){
    return hideOnExitProperty == null ? false : hideOnExitProperty.get();
  }

  public final void setHideOnExit(final boolean value){
    hideOnExitProperty().set(value);
  }

  private BooleanProperty hideOnExitProperty;

}
