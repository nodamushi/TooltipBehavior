import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import nodamushi.jfx.popup.TooltipBehavior;

/**
 * 一つのTooltipを使い回して表示するテスト
 * @author nodamushi
 *
 */
public class BehaviorTest2 extends Application{


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

    final TooltipBehavior behavior = new TooltipBehavior();
    //マウスが乗ってから0.1秒後に表示
    behavior.setOpenDuration(new Duration(100));
    //ずっと表示
    behavior.setHideDuration(Duration.INDEFINITE);
    //マウスが放れてから0.3秒後に非表示
    behavior.setLeftDuration(new Duration(300));

    //表示前にTooltipの内容をColorの値に更新させる
    behavior.setPopupUpdater((final Tooltip t,final Node n)->{
      if(n instanceof Rectangle){
        t.setText(((Rectangle)n).getFill().toString());
      }
    });

    for(int y=0;y<l;y++) {
      for(int x=0;x<l;x++){
        final Rectangle r = createNode(size);
        g.add(r, x, y);

        //インストール Tooltipは一つのインスタンスを使い回す
        behavior.install(r);
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
