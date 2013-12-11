/*
 * Copyright 2012-2013 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.mps.diagram.contentDemo.mapper;

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.view.CellView;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.mps.diagram.contentDemo.model.Content;
import jetbrains.mps.diagram.contentDemo.model.ContentItem;
import jetbrains.mps.diagram.dataflow.mapper.DiagramMapper;
import jetbrains.mps.diagram.dataflow.mapper.RootDiagramMapper;
import jetbrains.mps.diagram.dataflow.model.Block;
import jetbrains.mps.diagram.dataflow.model.Diagram;

import java.util.ArrayList;
import java.util.List;

public class ContentRootMapper extends Mapper<Diagram, ViewContainer> {
  private Content myContent;

  private VerticalView myView = new VerticalView();

  private ContentProperties myProperties;
  private List<Runnable> myViewAdders = new ArrayList<Runnable>();

  public ContentRootMapper(Diagram diagram, Content content) {
    super(diagram, new ViewContainer());
    myContent = content;

    myView = new VerticalView();
    getTarget().root().children().add(myView);

    RootDiagramMapper.initRoot(diagram, getTarget());
  }



  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    Property<String> text = createCell("test", null);
    Property<Integer> x = new ValueProperty<Integer>(75);
    createCell("75", new IntegerHandler(x));
    Property<Integer> y = new ValueProperty<Integer>(75);
    createCell("75", new IntegerHandler(y));
    final Property<Integer> num = new ValueProperty<Integer>(2);
    createCell("2", new IntegerHandler(num));

    myProperties = new ContentProperties(x, y);

    conf.add(Synchronizers.forProperties(text, myContent.name));
    conf.add(Synchronizers.forProperty(num, new Runnable() {
      @Override
      public void run() {
        Integer value = num.get();
        int size = myContent.items.size();
        if (value < size) {
          for (int i = 0; i < size - value; i++) {
            myContent.items.remove(0);
          }
        }
        if (value > size) {
          for (int i = 0; i < value - size; i++) {
            myContent.items.add(new ContentItem());
          }
        }
      }
    }));

    conf.add(Synchronizers.forConstantRole(this, getSource(), myView.children(), new MapperFactory<Diagram, View>() {
      @Override
      public Mapper<? extends Diagram, ? extends View> createMapper(Diagram source) {
        return new DiagramMapper(source, new MapperFactory<Block, View>() {
          @Override
          public Mapper<? extends Block, ? extends View> createMapper(Block source) {
            return new BlockWithContentMapper(source, myContent, myProperties);
          }
        });
      }
    }));

    conf.add(Synchronizers.forRegistration(new Supplier<Registration>() {
      @Override
      public Registration get() {
        for (Runnable r: myViewAdders) {
          r.run();
        }
        return new Registration() {
          @Override
          public void remove() {
          }
        };
      }
    }));
  }

  private Property<String> createCell(String value, final Handler<String> handler) {
    final CellView cell = new CellView(new GroupView());
    final TextCell text = new TextCell();
    text.text().set(value);
    text.addTrait(TextEditing.textEditing());
    cell.cell.set(text);

    if (handler != null) {
      cell.addTrait(new ViewTraitBuilder().on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
        @Override
        public void handle(View view, KeyEvent e) {
          if (e.is(Key.ENTER)) {
            handler.handle(text.text().get());
          }
        }
      }).build());
    }

    myViewAdders.add(new Runnable() {
      @Override
      public void run() {
        myView.children().add(cell);
      }
    });

    return text.text();
  }

  static class ContentProperties {
    final Property<Integer> x;
    final Property<Integer> y;

    ContentProperties(Property<Integer> x, Property<Integer> y) {
      this.x = x;
      this.y = y;
    }
  }


  private static class IntegerHandler implements Handler<String> {
    private Property<Integer> myProperty;

    private IntegerHandler(Property<Integer> property) {
      myProperty = property;
    }

    @Override
    public void handle(String item) {
      myProperty.set(Integer.parseInt(item));
    }
  }
}