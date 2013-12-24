package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.model.composite.dump.DumpContext;
import jetbrains.jetpad.model.composite.dump.Dumper;
import jetbrains.jetpad.model.composite.dump.Printer;

public class ViewDumpers {
  public static void dump(View v) {
    new Dumper<View>().dump(v);
  }

  public static void dumpValidationStatus(View v) {
    new Dumper<View>(new Printer<View>() {
      @Override
      public void print(DumpContext ctx, View item) {
        ctx.println(item.toString() + "[" + (item.valid().get() ? "+" : "-") + "]");
      }
    }).dump(v);
  }
}
