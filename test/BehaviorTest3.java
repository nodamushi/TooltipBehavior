import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import nodamushi.jfx.popup.SinglePopupBehavior.BehaviorGroup;
import nodamushi.jfx.popup.TooltipBehavior;

/**
 * 複数のTooltipBehaviorを運用するテスト
 * @author nodamushi
 *
 */
public class BehaviorTest3 extends Application{


  public static void main(final String[] args){
    launch(args);
  }

  @Override
  public void start(final Stage primaryStage) throws Exception{
    final int w = 200;
    final int l = 5;
    final int size = w/l;
    final GridPane g = new GridPane();
    g.setPrefSize(w, w);

    final TooltipBehavior behavior1 = new TooltipBehavior();
    //マウスが乗ってから0.1秒後に表示
    behavior1.setOpenDuration(new Duration(100));
    //ずっと表示
    behavior1.setHideDuration(Duration.INDEFINITE);
    //マウスが放れてから3秒後に非表示
    behavior1.setLeftDuration(new Duration(3000));

    final TooltipBehavior behavior2 = new TooltipBehavior();
    //マウスが乗ってから1秒後に表示
    behavior2.setOpenDuration(new Duration(1000));
    //1秒で消える
    behavior2.setHideDuration(new Duration(1000));
    //マウスが放れてから3秒後に非表示
    behavior2.setLeftDuration(new Duration(3000));

    //BehaviorGroupに登録すると、一つのBehaviorGroupに登録された
    //Behaviorの中では、ポップアップするTooltipが常に一つになります
    final BehaviorGroup group = new BehaviorGroup();

    //↓をコメントアウトすると二つのTooltipが表示されるようになるはずです
    group.addAll(behavior1,behavior2);

    for(int y=0;y<l;y++) {
      TooltipBehavior behavior = (y&1)==0?behavior1:behavior2;

      for(int x=0;x<l;x++){
        final Rectangle r = createNode(size);
        g.add(r, x, y);
        //色をツールチップで表示する
        final Tooltip tooltip = new Tooltip(r.getFill().toString());

        behavior.install(r,tooltip);

        //隣り合うタイルのBehaviorを変える
        behavior = behavior==behavior1?behavior2:behavior1;
      }
    }


    primaryStage.setScene(new Scene(g));
    primaryStage.show();
  }

  public static Rectangle createNode(final double size){
    final Rectangle r = new Rectangle(size, size);
    final Color c = new Color(Math.random(), Math.random(), Math.random(), 1);
    r.setFill(c);
    return r;
  }
}
